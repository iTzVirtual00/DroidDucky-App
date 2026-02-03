package me.itzvirtual.droidducky.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.itzvirtual.droidducky.data.Script
import me.itzvirtual.droidducky.data.ScriptRepository
import me.itzvirtual.droidducky.hid.DuckyScriptExecutor
import me.itzvirtual.droidducky.hid.HidKeyboard
import java.io.File

data class MainUiState(
    val devicePath: String = "/dev/hidg0",
    val deviceExists: Boolean = false,
    val deviceError: String? = null,
    val scripts: List<Script> = emptyList(),
    val isLoading: Boolean = false,
    val isExecuting: Boolean = false,
    val executingScriptId: String? = null,
    val executionProgress: Pair<Int, Int>? = null, // (current, total)
    val executionError: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ScriptRepository(application)
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    private var executionJob: Job? = null
    
    init {
        loadDevicePath()
        refreshDeviceStatus()
        loadScripts()
    }
    
    private fun loadDevicePath() {
        val path = repository.getDevicePath()
        _uiState.value = _uiState.value.copy(devicePath = path)
    }
    
    fun refreshDeviceStatus() {
        viewModelScope.launch {
            val path = _uiState.value.devicePath
            val file = File(path)
            val exists = file.exists()
            val error = if (!exists) {
                "Device not found at $path"
            } else if (!file.canRead() || !file.canWrite()) {
                "No read/write permission for $path"
            } else {
                null
            }
            _uiState.value = _uiState.value.copy(
                deviceExists = exists && error == null,
                deviceError = error
            )
        }
    }
    
    fun updateDevicePath(newPath: String) {
        repository.setDevicePath(newPath)
        _uiState.value = _uiState.value.copy(devicePath = newPath)
        refreshDeviceStatus()
    }
    
    fun loadScripts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val scripts = repository.getScripts()
            _uiState.value = _uiState.value.copy(
                scripts = scripts,
                isLoading = false
            )
        }
    }
    
    fun addScript(name: String) {
        val script = Script(name = name)
        repository.addScript(script)
        loadScripts()
    }
    
    fun deleteScript(scriptId: String) {
        repository.deleteScript(scriptId)
        loadScripts()
    }
    
    fun clearExecutionError() {
        _uiState.value = _uiState.value.copy(executionError = null)
    }
    
    fun stopExecution() {
        executionJob?.cancel()
        executionJob = null
        _uiState.value = _uiState.value.copy(
            isExecuting = false,
            executingScriptId = null,
            executionProgress = null
        )
    }
    
    fun playScript(script: Script, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        if (_uiState.value.isExecuting) {
            onComplete(false, "Already executing a script")
            return
        }
        
        if (!_uiState.value.deviceExists) {
            onComplete(false, "HID device not available")
            return
        }
        
        if (script.content.isBlank()) {
            onComplete(false, "Script is empty")
            return
        }
        
        executionJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExecuting = true,
                executingScriptId = script.id,
                executionProgress = Pair(0, 1),
                executionError = null
            )
            
            try {
                val keyboard = HidKeyboard(_uiState.value.devicePath)
                val executor = DuckyScriptExecutor(keyboard)
                
                val result = executor.execute(
                    script = script.content,
                    onProgress = { current, total ->
                        _uiState.value = _uiState.value.copy(
                            executionProgress = Pair(current, total)
                        )
                    }
                )
                
                _uiState.value = _uiState.value.copy(
                    isExecuting = false,
                    executingScriptId = null,
                    executionProgress = null,
                    executionError = result.error
                )
                
                onComplete(result.success, result.error)
                
            } catch (e: Exception) {
                val errorMsg = "Execution failed: ${e.message}"
                _uiState.value = _uiState.value.copy(
                    isExecuting = false,
                    executingScriptId = null,
                    executionProgress = null,
                    executionError = errorMsg
                )
                onComplete(false, errorMsg)
            }
        }
    }
}
