package com.pawnsafe.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawnsafe.data.local.dao.InterestRateDao
import com.pawnsafe.data.local.entity.InterestRateEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class InterestRateUIState {
    object Idle : InterestRateUIState()
    object Loading : InterestRateUIState()
    data class Success(val message: String) : InterestRateUIState()
    data class Error(val message: String) : InterestRateUIState()
}

@HiltViewModel
class InterestRateViewModel @Inject constructor(
    private val interestRateDao: InterestRateDao
) : ViewModel() {

    val allRates: StateFlow<List<InterestRateEntity>> =
        interestRateDao.getAllRates()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow<InterestRateUIState>(InterestRateUIState.Idle)
    val uiState: StateFlow<InterestRateUIState> = _uiState.asStateFlow()

    fun addNewRate(rate: Double, effectiveFrom: String, note: String) {
        if (rate <= 0.0) {
            _uiState.value = InterestRateUIState.Error("Rate must be greater than 0")
            return
        }
        viewModelScope.launch {
            _uiState.value = InterestRateUIState.Loading
            try {
                // close current active rate one day before new one starts
                val prevDay = getPrevDay(effectiveFrom)
                interestRateDao.closeCurrentRate(prevDay)

                interestRateDao.insertRate(
                    InterestRateEntity(
                        rate = rate,
                        effectiveFrom = effectiveFrom,
                        effectiveTo = null,
                        note = note.ifBlank { null },
                        createdAt = System.currentTimeMillis()
                    )
                )
                _uiState.value = InterestRateUIState.Success("New rate added successfully")
            } catch (e: Exception) {
                _uiState.value = InterestRateUIState.Error(e.message ?: "Failed to add rate")
            }
        }
    }

    fun resetState() {
        _uiState.value = InterestRateUIState.Idle
    }

    private fun getPrevDay(date: String): String {
        // date format: yyyy-MM-dd
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val cal = java.util.Calendar.getInstance()
            cal.time = sdf.parse(date)!!
            cal.add(java.util.Calendar.DAY_OF_MONTH, -1)
            sdf.format(cal.time)
        } catch (e: Exception) {
            date
        }
    }
}