package com.example.noteapp.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.noteapp.utils.BackupUtils
import com.example.noteapp.viewmodel.BackupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onBack: () -> Unit,
    viewModel: BackupViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // 备份文件创建器
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let {
            viewModel.createBackup(context, it)
        }
    }
    
    // 备份文件选择器
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.validateBackupFile(context, it)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "数据备份与恢复",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 功能说明
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "信息",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "数据备份功能",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• 备份包含所有笔记数据和图片文件\n• 备份文件为ZIP格式，便于存储和传输\n• 恢复时可选择是否替换现有数据",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 备份功能
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "创建备份",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "将所有笔记数据和图片文件打包为一个ZIP文件",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Button(
                        onClick = {
                            val fileName = BackupUtils.generateBackupFileName()
                            backupLauncher.launch(fileName)
                        },
                        enabled = !uiState.isBackupInProgress && !uiState.isRestoreInProgress,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isBackupInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("备份中...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "备份",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("创建备份")
                        }
                    }
                    
                    // 备份结果显示
                    uiState.backupResult?.let { result ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (result.success) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                } else {
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = if (result.success) "备份成功" else "备份失败",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (result.success) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                )
                                Text(
                                    text = result.message,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // 恢复功能
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "恢复数据",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "从备份文件恢复笔记数据和图片文件",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Button(
                        onClick = {
                            restoreLauncher.launch("application/zip")
                        },
                        enabled = !uiState.isBackupInProgress && !uiState.isRestoreInProgress,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isRestoreInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("恢复中...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "恢复",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("选择备份文件")
                        }
                    }
                    
                    // 恢复结果显示
                    uiState.restoreResult?.let { result ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (result.success) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                } else {
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = if (result.success) "恢复成功" else "恢复失败",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (result.success) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                )
                                Text(
                                    text = result.message,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // 注意事项
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "警告",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Column {
                        Text(
                            text = "注意事项",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• 恢复数据前请确保备份文件完整\n• 选择替换现有数据将清空当前所有笔记\n• 建议在恢复前先创建当前数据的备份",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    
    // 恢复确认对话框
    if (uiState.showRestoreConfirmDialog && uiState.backupMetadata != null && uiState.pendingRestoreUri != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.dismissRestoreDialog()
            },
            title = { Text("确认恢复数据") },
            text = {
                Column {
                    Text("备份文件信息：")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 备份时间：${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(uiState.backupMetadata!!.timestamp))}\n" +
                                "• 笔记数量：${uiState.backupMetadata!!.noteCount}\n" +
                                "• 图片数量：${uiState.backupMetadata!!.imageCount}\n" +
                                "• 备份版本：${uiState.backupMetadata!!.version}",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "选择恢复方式：",
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Column {
                    Button(
                        onClick = {
                            viewModel.restoreFromBackup(context, replaceExisting = true)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("替换现有数据")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = {
                            viewModel.restoreFromBackup(context, replaceExisting = false)
                        }
                    ) {
                        Text("合并数据")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissRestoreDialog()
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}