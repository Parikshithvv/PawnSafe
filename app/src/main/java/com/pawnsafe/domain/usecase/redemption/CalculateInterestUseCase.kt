package com.pawnsafe.domain.usecase.redemption

import com.pawnsafe.core.utils.InterestCalculator
import com.pawnsafe.data.local.dao.InterestRateDao
import javax.inject.Inject

class CalculateInterestUseCase @Inject constructor(
    private val interestRateDao: InterestRateDao
) {
    suspend operator fun invoke(
        principal: Double,
        pledgeDateIso: String,
        returnDateIso: String
    ): InterestCalculator.InterestResult {
        // Look up the rate that was active on the pledge date
        val rateEntity = interestRateDao.getRateForDate(pledgeDateIso)
        val rate = rateEntity?.rate ?: 1.16  // fallback to default
        return InterestCalculator.calculate(principal, rate, pledgeDateIso, returnDateIso)
    }
}
