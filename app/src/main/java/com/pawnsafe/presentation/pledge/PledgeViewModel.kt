package com.pawnsafe.presentation.pledge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawnsafe.core.utils.DateUtils
import com.pawnsafe.domain.model.Pledge
import com.pawnsafe.domain.usecase.pledge.AddPledgeUseCase
import com.pawnsafe.domain.usecase.pledge.GetAllPledgesUseCase
import com.pawnsafe.domain.usecase.pledge.SearchPledgeUseCase
import com.pawnsafe.domain.usecase.pledge.UpdatePledgeStatusUseCase
import com.pawnsafe.domain.repository.IPledgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerLookupResult(
    val profile: Pledge?,
    val activePledges: List<Pledge>,
    val history: List<Pledge>
)

@HiltViewModel
class PledgeViewModel @Inject constructor(
    private val getAllPledges:      GetAllPledgesUseCase,
    private val searchPledge:       SearchPledgeUseCase,
    private val addPledge:          AddPledgeUseCase,
    private val updatePledgeStatus: UpdatePledgeStatusUseCase,
    private val pledgeRepository:   IPledgeRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<PledgeUIState>(PledgeUIState.Idle)
    val listState: StateFlow<PledgeUIState> = _listState

    private val _formState = MutableStateFlow<PledgeFormState>(PledgeFormState.Idle)
    val formState: StateFlow<PledgeFormState> = _formState

    private val _editingPledge = MutableStateFlow<Pledge?>(null)
    val editingPledge: StateFlow<Pledge?> = _editingPledge

    private val _customerLookup = MutableStateFlow<CustomerLookupResult?>(null)
    val customerLookup: StateFlow<CustomerLookupResult?> = _customerLookup

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter

    init { loadAllPledges() }

    fun loadAllPledges() {
        loadForFilter(_selectedFilter.value)
    }

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
        loadForFilter(filter)
    }

    private fun loadForFilter(filter: String) {
        viewModelScope.launch {
            _listState.value = PledgeUIState.Loading
            getAllPledges()
                .catch { e -> _listState.value = PledgeUIState.Error(e.message ?: "Error") }
                .collectLatest { pledges ->
                    val filtered = if (filter == "ALL") pledges
                                   else pledges.filter { it.status.equals(filter, ignoreCase = true) }
                    _listState.value = PledgeUIState.Success(filtered)
                }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _listState.value = PledgeUIState.Loading
            searchPledge(query)
                .catch { e -> _listState.value = PledgeUIState.Error(e.message ?: "Error") }
                .collectLatest { pledges -> _listState.value = PledgeUIState.Success(pledges) }
        }
    }

    fun loadPledgeForEdit(id: Int) {
        viewModelScope.launch {
            _editingPledge.value = pledgeRepository.getPledgeById(id)
        }
    }

    fun savePledge(pledge: Pledge) {
        viewModelScope.launch {
            _formState.value = PledgeFormState.Loading
            try {
                if (pledge.id == 0) addPledge(pledge)
                else pledgeRepository.updatePledge(pledge)
                _formState.value = PledgeFormState.Saved
            } catch (e: Exception) {
                _formState.value = PledgeFormState.Error(e.message ?: "Save failed")
            }
        }
    }

    fun lookupByPhone(phone: String) {
        if (phone.length != 10) return
        viewModelScope.launch {
            val profile       = pledgeRepository.getLatestByPhone(phone)
            val activePledges = pledgeRepository.getActiveByPhone(phone)
            val history       = pledgeRepository.getAllByPhone(phone)
            _customerLookup.value = CustomerLookupResult(profile, activePledges, history)
        }
    }

    fun clearCustomerLookup() { _customerLookup.value = null }

    fun markRedeemed(id: Int) {
        viewModelScope.launch {
            try { updatePledgeStatus(id, "REDEEMED") } catch (_: Exception) {}
        }
    }

    fun clearEditingPledge() { _editingPledge.value = null }
    fun resetFormState() { _formState.value = PledgeFormState.Idle }
    fun todayIso(): String = DateUtils.todayIso()
}