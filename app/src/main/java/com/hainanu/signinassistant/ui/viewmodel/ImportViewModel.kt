package com.hainanu.signinassistant.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hainanu.signinassistant.domain.usecase.ImportTimetableUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ImportUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val importCompleted: Boolean = false,
    val importedFileName: String? = null,
)

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val importTimetableUseCase: ImportTimetableUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = ImportUiState(isLoading = true)
            runCatching {
                val bundle = importTimetableUseCase.preview(uri)
                importTimetableUseCase.confirmImport(bundle)
                bundle
            }.onSuccess { bundle ->
                _uiState.value = ImportUiState(
                    importCompleted = true,
                    importedFileName = bundle.sourceFileName,
                )
            }.onFailure { throwable ->
                _uiState.value = ImportUiState(
                    errorMessage = throwable.message ?: "导入失败，请重新选择课表文件。",
                )
            }
        }
    }
}
