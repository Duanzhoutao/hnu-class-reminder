package com.hainanu.signinassistant.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hainanu.signinassistant.domain.model.ImportedTimetableBundle
import com.hainanu.signinassistant.domain.usecase.ImportTimetableUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ImportUiState(
    val isLoading: Boolean = false,
    val previewBundle: ImportedTimetableBundle? = null,
    val errorMessage: String? = null,
    val importCompleted: Boolean = false,
)

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val importTimetableUseCase: ImportTimetableUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun previewImport(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = ImportUiState(isLoading = true)
            runCatching { importTimetableUseCase.preview(uri) }
                .onSuccess { bundle ->
                    _uiState.value = ImportUiState(previewBundle = bundle)
                }
                .onFailure { throwable ->
                    _uiState.value = ImportUiState(errorMessage = throwable.message ?: "导入失败")
                }
        }
    }

    fun confirmImport() {
        val bundle = _uiState.value.previewBundle ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching { importTimetableUseCase.confirmImport(bundle) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, importCompleted = true)
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "保存课表失败",
                    )
                }
        }
    }
}
