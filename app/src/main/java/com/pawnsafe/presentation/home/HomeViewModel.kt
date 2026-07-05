package com.pawnsafe.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawnsafe.domain.repository.IPledgeRepository
import com.pawnsafe.domain.repository.IRedemptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeStats(
    val totalPledges: Int = 0,
    val activePledges: Int = 0,
    val redeemedPledges: Int = 0,
    val overduePledges: Int = 0,
    val totalRedemptions: Int = 0
)

sealed class HomeUIState {
    object Loading : HomeUIState()
    data class Success(val stats: HomeStats) : HomeUIState()
    data class Error(val message: String) : HomeUIState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pledgeRepository: IPledgeRepository,
    private val redemptionRepository: IRedemptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUIState>(HomeUIState.Loading)
    val uiState: StateFlow<HomeUIState> = _uiState

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            combine(
                pledgeRepository.getAllPledges(),
                redemptionRepository.getAllRedemptions()
            ) { pledges, redemptions ->
                HomeStats(
                    totalPledges      = pledges.size,
                    activePledges     = pledges.count { it.status == "ACTIVE" },
                    redeemedPledges   = pledges.count { it.status == "REDEEMED" },
                    overduePledges    = pledges.count { it.status == "OVERDUE" },
                    totalRedemptions  = redemptions.size
                )
            }
            .catch { e -> _uiState.value = HomeUIState.Error(e.message ?: "Error loading stats") }
            .collect { stats -> _uiState.value = HomeUIState.Success(stats) }
        }
    }
}