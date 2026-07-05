package com.pawnsafe.presentation.redemption

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawnsafe.core.utils.DateUtils
import com.pawnsafe.domain.model.Redemption
import com.pawnsafe.domain.usecase.redemption.AddRedemptionUseCase
import com.pawnsafe.domain.usecase.redemption.CalculateInterestUseCase
import com.pawnsafe.domain.usecase.redemption.GetAllRedemptionsUseCase
import com.pawnsafe.domain.repository.IPledgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RedemptionViewModel @Inject constructor(
    private val getAllRedemptions: GetAllRedemptionsUseCase,
    private val addRedemption:    AddRedemptionUseCase,
    private val calculateInterest: CalculateInterestUseCase,
    private val pledgeRepository:  IPledgeRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<RedemptionUIState>(RedemptionUIState.Idle)
    val listState: StateFlow<RedemptionUIState> = _listState

    private val _formState = MutableStateFlow<RedemptionFormState>(RedemptionFormState.Idle)
    val formState: StateFlow<RedemptionFormState> = _formState

    // Pre-filled pledge data when coming from pledge card Redeem button
    private val _prefillState = MutableStateFlow<PledgePrefill?>(null)
    val prefillState: StateFlow<PledgePrefill?> = _prefillState

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            _listState.value = RedemptionUIState.Loading
            getAllRedemptions()
                .catch { e -> _listState.value = RedemptionUIState.Error(e.message ?: "Error") }
                .collectLatest { list -> _listState.value = RedemptionUIState.Success(list) }
        }
    }

    /** Load pledge by ID — called when navigating from pledge card Redeem button */
    fun loadPledgeById(pledgeId: Int) {
        if (pledgeId == 0) return
        viewModelScope.launch {
            val pledge = pledgeRepository.getPledgeById(pledgeId) ?: return@launch
            val principal = pledge.loanAmountRs.toDoubleOrNull() ?: 0.0
            _prefillState.value = PledgePrefill(
                pledgeId    = pledge.id,
                ticketNo    = pledge.ticketNo,
                customerName = pledge.name,
                address     = listOfNotNull(pledge.place, pledge.taluk).joinToString(", "),
                pledgeDate  = pledge.date,
                principalRs = principal
            )
        }
    }

    fun clearPrefill() { _prefillState.value = null }

    /** Called when user types ticket no manually */
    fun lookupPledge(
        ticketNo: String,
        onResult: (pledgeId: Int, name: String, pledgeDateIso: String, principalRs: Double) -> Unit
    ) {
        viewModelScope.launch {
            val pledge = pledgeRepository.getByTicketNo(ticketNo)
            if (pledge != null) {
                onResult(pledge.id, pledge.name, pledge.date, pledge.loanAmountRs.toDoubleOrNull() ?: 0.0)
            } else {
                _formState.value = RedemptionFormState.Error("Ticket '$ticketNo' not found")
            }
        }
    }

    fun calculateInterest(principal: Double, pledgeDateIso: String, returnDateIso: String) {
        viewModelScope.launch {
            _formState.value = RedemptionFormState.Loading
            try {
                val result = calculateInterest.invoke(principal, pledgeDateIso, returnDateIso)
                _formState.value = RedemptionFormState.InterestReady(result)
            } catch (e: Exception) {
                _formState.value = RedemptionFormState.Error(e.message ?: "Calculation failed")
            }
        }
    }

    fun saveRedemption(redemption: Redemption) {
        viewModelScope.launch {
            _formState.value = RedemptionFormState.Loading
            try {
                addRedemption(redemption)
                _formState.value = RedemptionFormState.Saved
            } catch (e: Exception) {
                _formState.value = RedemptionFormState.Error(e.message ?: "Save failed")
            }
        }
    }

    fun resetFormState() { _formState.value = RedemptionFormState.Idle }
    fun todayIso(): String = DateUtils.todayIso()
}

data class PledgePrefill(
    val pledgeId:     Int,
    val ticketNo:     String,
    val customerName: String,
    val address:      String,
    val pledgeDate:   String,
    val principalRs:  Double
)