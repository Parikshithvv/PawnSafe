package com.pawnsafe.usecase

import com.pawnsafe.core.utils.InterestCalculator
import com.pawnsafe.data.local.dao.InterestRateDao
import com.pawnsafe.data.local.entity.InterestRateEntity
import com.pawnsafe.domain.usecase.redemption.CalculateInterestUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.math.abs

class CalculateInterestUseCaseTest {

    private lateinit var interestRateDao: InterestRateDao
    private lateinit var useCase: CalculateInterestUseCase

    @Before
    fun setUp() {
        interestRateDao = mock()
        useCase = CalculateInterestUseCase(interestRateDao)
    }

    // ── Formula: interest = principal * (rate/100) / 30 * days ──────────────

    @Test
    fun `calculate interest with rate from dao`() = runTest {
        whenever(interestRateDao.getRateForDate("2024-01-01")).thenReturn(
            InterestRateEntity(id = 1, rate = 1.16, effectiveFrom = "2024-01-01")
        )

        val result = useCase(
            principal     = 10000.0,
            pledgeDateIso = "2024-01-01",
            returnDateIso = "2024-02-01"   // 32 days inclusive
        )

        // Expected: 10000 * (1.16/100) / 30 * 32 = 123.73...
        val expected = 10000.0 * (1.16 / 100.0) / 30.0 * 32
        assertNear(expected, result.interestRs + result.interestPs / 100.0)
        assertEquals(32, result.numberOfDays)
        assertEquals(1.16, result.rate, 0.001)
    }

    @Test
    fun `falls back to default rate 1_16 when dao returns null`() = runTest {
        whenever(interestRateDao.getRateForDate("2024-01-01")).thenReturn(null)

        val result = useCase(
            principal     = 5000.0,
            pledgeDateIso = "2024-01-01",
            returnDateIso = "2024-01-31"   // 31 days inclusive
        )

        val expected = 5000.0 * (1.16 / 100.0) / 30.0 * 31
        assertNear(expected, result.interestRs + result.interestPs / 100.0)
        assertEquals(1.16, result.rate, 0.001)
    }

    @Test
    fun `total equals principal plus interest`() = runTest {
        whenever(interestRateDao.getRateForDate("2024-03-01")).thenReturn(
            InterestRateEntity(id = 1, rate = 1.16, effectiveFrom = "2024-01-01")
        )

        val result = useCase(
            principal     = 20000.0,
            pledgeDateIso = "2024-03-01",
            returnDateIso = "2024-06-01"
        )

        assertNear(result.principal + result.interestRs + result.interestPs / 100.0, result.totalAmount)
    }

    @Test
    fun `single day pledge has correct interest`() = runTest {
        whenever(interestRateDao.getRateForDate("2024-01-01")).thenReturn(
            InterestRateEntity(id = 1, rate = 1.16, effectiveFrom = "2024-01-01")
        )

        val result = useCase(
            principal     = 1000.0,
            pledgeDateIso = "2024-01-01",
            returnDateIso = "2024-01-01"   // same day = 1 day inclusive
        )

        assertEquals(1, result.numberOfDays)
        val expected = 1000.0 * (1.16 / 100.0) / 30.0 * 1
        assertNear(expected, result.interestRs + result.interestPs / 100.0)
    }

    @Test
    fun `365 day pledge overdue boundary`() = runTest {
        whenever(interestRateDao.getRateForDate("2023-01-01")).thenReturn(
            InterestRateEntity(id = 1, rate = 1.16, effectiveFrom = "2023-01-01")
        )

        val result = useCase(
            principal     = 15000.0,
            pledgeDateIso = "2023-01-01",
            returnDateIso = "2024-01-01"   // 366 days inclusive
        )

        assertEquals(366, result.numberOfDays)
        val expected = 15000.0 * (1.16 / 100.0) / 30.0 * 366
        assertNear(expected, result.interestRs + result.interestPs / 100.0)
    }

    // Helper — float equality within 0.01
    private fun assertNear(expected: Double, actual: Double) {
        assert(abs(expected - actual) < 0.01) {
            "Expected ~$expected but was $actual"
        }
    }
}