package com.pawnsafe.viewmodel

import app.cash.turbine.test
import com.pawnsafe.domain.model.Pledge
import com.pawnsafe.domain.usecase.pledge.AddPledgeUseCase
import com.pawnsafe.domain.usecase.pledge.GetAllPledgesUseCase
import com.pawnsafe.domain.usecase.pledge.SearchPledgeUseCase
import com.pawnsafe.domain.usecase.pledge.UpdatePledgeStatusUseCase
import com.pawnsafe.presentation.pledge.PledgeFormState
import com.pawnsafe.presentation.pledge.PledgeUIState
import com.pawnsafe.presentation.pledge.PledgeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class PledgeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getAllPledges:      GetAllPledgesUseCase
    private lateinit var searchPledge:       SearchPledgeUseCase
    private lateinit var addPledge:          AddPledgeUseCase
    private lateinit var updatePledgeStatus: UpdatePledgeStatusUseCase
    private lateinit var viewModel:          PledgeViewModel

    private val fakePledge = Pledge(
        id           = 1,
        ticketNo     = "001",
        date         = "2024-01-01",
        name         = "Ravi Kumar",
        loanAmountRs = "10000"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getAllPledges      = mock()
        searchPledge       = mock()
        addPledge          = mock()
        updatePledgeStatus = mock()

        whenever(getAllPledges()).thenReturn(flowOf(listOf(fakePledge)))

        viewModel = PledgeViewModel(
            getAllPledges      = getAllPledges,
            searchPledge       = searchPledge,
            addPledge          = addPledge,
            updatePledgeStatus = updatePledgeStatus
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load emits Success with pledge list`() = runTest {
        viewModel.listState.test {
            // First emission may be Loading or Success depending on timing
            val emissions = mutableListOf<PledgeUIState>()
            emissions.add(awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            // Drain remaining
            while (true) {
                val item = expectMostRecentItem()
                emissions.add(item)
                break
            }
            val success = emissions.filterIsInstance<PledgeUIState.Success>().firstOrNull()
            assertTrue("Expected a Success state", success != null)
            assertEquals(1, success!!.data.size)
            assertEquals("Ravi Kumar", success.data[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search delegates to SearchPledgeUseCase`() = runTest {
        whenever(searchPledge("Ravi")).thenReturn(flowOf(listOf(fakePledge)))

        viewModel.search("Ravi")
        testDispatcher.scheduler.advanceUntilIdle()

        verify(searchPledge).invoke("Ravi")
    }

    @Test
    fun `savePledge emits Saved on success`() = runTest {
        whenever(addPledge(any())).thenReturn(1L)

        viewModel.formState.test {
            awaitItem() // Idle
            viewModel.savePledge(fakePledge)
            testDispatcher.scheduler.advanceUntilIdle()

            val states = mutableListOf<PledgeFormState>()
            states.add(awaitItem())
            states.add(awaitItem())

            assertTrue(states.any { it is PledgeFormState.Saved })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `savePledge emits Error when addPledge throws`() = runTest {
        whenever(addPledge(any())).thenThrow(RuntimeException("DB error"))

        viewModel.formState.test {
            awaitItem() // Idle
            viewModel.savePledge(fakePledge)
            testDispatcher.scheduler.advanceUntilIdle()

            val states = mutableListOf<PledgeFormState>()
            states.add(awaitItem())
            states.add(awaitItem())

            val error = states.filterIsInstance<PledgeFormState.Error>().firstOrNull()
            assertTrue("Expected Error state", error != null)
            assertEquals("DB error", error!!.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setOcrResult updates ocrResult state`() = runTest {
        val fields = mapOf("ticketNo" to "042", "name" to "Sita Devi")
        viewModel.setOcrResult(fields)
        assertEquals(fields, viewModel.ocrResult.value)
    }

    @Test
    fun `resetFormState sets formState back to Idle`() = runTest {
        whenever(addPledge(any())).thenReturn(1L)
        viewModel.savePledge(fakePledge)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.resetFormState()
        assertEquals(PledgeFormState.Idle, viewModel.formState.value)
    }
}