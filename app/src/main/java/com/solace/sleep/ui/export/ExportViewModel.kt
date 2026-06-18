package com.solace.sleep.ui.export

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solace.sleep.data.preferences.AppPreferences
import com.solace.sleep.domain.usecase.ExportDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

data class ExportUiState(
    val startDate: LocalDate = LocalDate.now().minusDays(30),
    val endDate: LocalDate = LocalDate.now(),
    val format: ExportDataUseCase.ExportFormat = ExportDataUseCase.ExportFormat.CSV,
    val isExporting: Boolean = false,
    val exportedFile: File? = null,
    val error: String? = null
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exportDataUseCase: ExportDataUseCase,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    fun setStartDate(date: LocalDate) { _uiState.value = _uiState.value.copy(startDate = date) }
    fun setEndDate(date: LocalDate) { _uiState.value = _uiState.value.copy(endDate = date) }
    fun setFormat(format: ExportDataUseCase.ExportFormat) {
        _uiState.value = _uiState.value.copy(format = format)
    }

    fun export() {
        viewModelScope.launch {
            val profileId = preferences.activeProfileId.first() ?: return@launch
            _uiState.value = _uiState.value.copy(isExporting = true, error = null)
            try {
                val state = _uiState.value
                val file = exportDataUseCase.export(
                    profileId = profileId,
                    startDate = state.startDate,
                    endDate = state.endDate,
                    format = state.format
                )
                _uiState.value = _uiState.value.copy(isExporting = false, exportedFile = file)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isExporting = false, error = e.message)
            }
        }
    }

    fun clearExport() { _uiState.value = _uiState.value.copy(exportedFile = null) }
}
