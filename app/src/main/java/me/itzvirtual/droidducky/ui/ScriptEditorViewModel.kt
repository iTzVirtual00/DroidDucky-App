package me.itzvirtual.droidducky.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.itzvirtual.droidducky.data.Script
import me.itzvirtual.droidducky.data.ScriptRepository

data class ScriptEditorUiState(
    val script: Script? = null,
    val content: String = "",
    val isSaved: Boolean = true,
    val isLoading: Boolean = false
)

class ScriptEditorViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ScriptRepository(application)
    
    private val _uiState = MutableStateFlow(ScriptEditorUiState())
    val uiState: StateFlow<ScriptEditorUiState> = _uiState.asStateFlow()
    
    fun loadScript(scriptId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val script = repository.getScript(scriptId)
            _uiState.value = _uiState.value.copy(
                script = script,
                content = script?.content ?: "",
                isLoading = false,
                isSaved = true
            )
        }
    }
    
    fun updateContent(newContent: String) {
        _uiState.value = _uiState.value.copy(
            content = newContent,
            isSaved = false
        )
    }
    
    fun saveScript() {
        viewModelScope.launch {
            val currentScript = _uiState.value.script ?: return@launch
            val updatedScript = currentScript.copy(content = _uiState.value.content)
            repository.updateScript(updatedScript)
            _uiState.value = _uiState.value.copy(
                script = updatedScript,
                isSaved = true
            )
        }
    }
}
