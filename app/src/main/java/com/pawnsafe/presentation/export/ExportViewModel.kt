package com.pawnsafe.presentation.export

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawnsafe.domain.usecase.export.ExportResult
import com.pawnsafe.domain.usecase.export.ExportToExcelUseCase
import com.pawnsafe.domain.usecase.export.ExportType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExportUIState(
    val isLoading: Boolean = false,
    val selectedType: ExportType = ExportType.BOTH,
    val result: ExportResult? = null,
    val errorMessage: String? = null
)

sealed class ExportEvent {
    data class SelectType(val type: ExportType) : ExportEvent()
    data class StartExport(val destinationUri: Uri) : ExportEvent()
    object ClearResult : ExportEvent()
}

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exportToExcelUseCase: ExportToExcelUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUIState())
    val uiState: StateFlow<ExportUIState> = _uiState.asStateFlow()

    fun onEvent(event: ExportEvent) {
        when (event) {
            is ExportEvent.SelectType -> {
                _uiState.update { it.copy(selectedType = event.type) }
            }
            is ExportEvent.StartExport -> {
                export(event.destinationUri)
            }
            is ExportEvent.ClearResult -> {
                _uiState.update { it.copy(result = null, errorMessage = null) }
            }
        }
    }

    private fun export(destinationUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, result = null, errorMessage = null) }
            val result = exportToExcelUseCase(
                exportType = _uiState.value.selectedType,
                destinationUri = destinationUri
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    result = result,
                    errorMessage = if (result is ExportResult.Error) result.message else null
                )
            }
        }
    }
}
