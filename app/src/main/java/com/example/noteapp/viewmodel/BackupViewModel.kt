package com.example.noteapp.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.utils.BackupUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BackupViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()
    
    fun createBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBackupInProgress = true)
            
            val result = BackupUtils.createBackup(context, uri)
            
            _uiState.value = _uiState.value.copy(
                isBackupInProgress = false,
                backupResult = result
            )
        }
    }
    
    fun validateBackupFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            val metadata = BackupUtils.validateBackupFile(context, uri)
            
            if (metadata != null) {
                _uiState.value = _uiState.value.copy(
                    backupMetadata = metadata,
                    pendingRestoreUri = uri,
                    showRestoreConfirmDialog = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    restoreResult = BackupUtils.RestoreResult(
                        success = false,
                        message = "无效的备份文件"
                    )
                )
            }
        }
    }
    
    fun restoreFromBackup(context: Context, replaceExisting: Boolean) {
        val uri = _uiState.value.pendingRestoreUri ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRestoreInProgress = true,
                showRestoreConfirmDialog = false
            )
            
            val result = BackupUtils.restoreFromBackup(context, uri, replaceExisting)
            
            _uiState.value = _uiState.value.copy(
                isRestoreInProgress = false,
                restoreResult = result,
                backupMetadata = null,
                pendingRestoreUri = null
            )
        }
    }
    
    fun dismissRestoreDialog() {
        _uiState.value = _uiState.value.copy(
            showRestoreConfirmDialog = false,
            backupMetadata = null,
            pendingRestoreUri = null
        )
    }
    
    fun clearResults() {
        _uiState.value = _uiState.value.copy(
            backupResult = null,
            restoreResult = null
        )
    }
}

data class BackupUiState(
    val isBackupInProgress: Boolean = false,
    val isRestoreInProgress: Boolean = false,
    val backupResult: BackupUtils.BackupResult? = null,
    val restoreResult: BackupUtils.RestoreResult? = null,
    val showRestoreConfirmDialog: Boolean = false,
    val pendingRestoreUri: Uri? = null,
    val backupMetadata: BackupUtils.BackupMetadata? = null
)