package com.example.noteapp.ui.screens.note

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.noteapp.model.ui.NoteDetailEvent
import com.example.noteapp.ui.SimpleRichTextDisplay
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * 笔记详情Screen
 * 遵循MVVM架构，UI与业务逻辑完全分离
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    viewModel: NoteDetailViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dateFormatter = remember { SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault()) }

    // 加载笔记数据
    LaunchedEffect(noteId) {
        viewModel.onEvent(NoteDetailEvent.LoadNote(noteId))
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "笔记详情",
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
                },
                actions = {
                    if (uiState.note != null) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "编辑"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "未知错误",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            uiState.note != null -> {
                 val note = uiState.note!!
                Column(
                     modifier = Modifier
                         .fillMaxSize()
                         .padding(paddingValues)
                         .padding(16.dp)
                         .verticalScroll(rememberScrollState()),
                     verticalArrangement = Arrangement.spacedBy(16.dp)
                 ) {
                     // 标题
                     Text(
                         text = note.title,
                         fontSize = 24.sp,
                         fontWeight = FontWeight.Bold
                     )
                     
                     // 时间信息
                     Card(
                         modifier = Modifier.fillMaxWidth(),
                         colors = CardDefaults.cardColors(
                             containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                         )
                     ) {
                         Column(
                             modifier = Modifier.padding(12.dp),
                             verticalArrangement = Arrangement.spacedBy(4.dp)
                         ) {
                             Text(
                                 text = "创建时间：${dateFormatter.format(note.creationTime)}",
                                 fontSize = 12.sp,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant
                             )
                             if (note.creationTime != note.lastEditTime) {
                                 Text(
                                     text = "修改时间：${dateFormatter.format(note.lastEditTime)}",
                                     fontSize = 12.sp,
                                     color = MaterialTheme.colorScheme.onSurfaceVariant
                                 )
                             }
                         }
                     }
                     
                     // 标签
                     if (note.tags.isNotEmpty()) {
                         Card(
                             modifier = Modifier.fillMaxWidth()
                         ) {
                             Column(
                                 modifier = Modifier.padding(12.dp),
                                 verticalArrangement = Arrangement.spacedBy(8.dp)
                             ) {
                                 Text(
                                     text = "标签",
                                     fontSize = 14.sp,
                                     fontWeight = FontWeight.Medium
                                 )
                                 
                                 Row(
                                     horizontalArrangement = Arrangement.spacedBy(8.dp)
                                 ) {
                                     note.tags.forEach { tag ->
                                         Text(
                                             text = tag,
                                             fontSize = 12.sp,
                                             color = MaterialTheme.colorScheme.primary,
                                             modifier = Modifier
                                                 .background(
                                                     color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                     shape = RoundedCornerShape(4.dp)
                                                 )
                                                 .padding(horizontal = 8.dp, vertical = 4.dp)
                                         )
                                     }
                                 }
                             }
                         }
                     }
                     
                     // 内容（使用富文本显示组件）
                     Card(
                         modifier = Modifier.fillMaxWidth()
                     ) {
                         Column(
                             modifier = Modifier.padding(12.dp),
                             verticalArrangement = Arrangement.spacedBy(8.dp)
                         ) {
                             Text(
                                 text = "内容",
                                 fontSize = 14.sp,
                                 fontWeight = FontWeight.Medium
                             )
                             
                             SimpleRichTextDisplay(
                                 content = note.content,
                                 images = note.images,
                                 modifier = Modifier.fillMaxWidth()
                             )
                         }
                     }
                 }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}