package com.example.noteapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.noteapp.data.NoteImage
import com.example.noteapp.utils.ImageUtils
import java.util.*

/**
 * 简单稳定的富文本编辑器
 * 支持基础的图片标记插入功能
 */
@Composable
fun SimpleRichTextEditor(
    content: String,
    images: List<NoteImage>,
    onContentChange: (String) -> Unit,
    onImagesChange: (List<NoteImage>) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "请输入内容..."
) {
    var showAddImageDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 简单的工具栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { showAddImageDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加图片",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加图片")
            }
            
            if (images.isNotEmpty()) {
                Text(
                    text = "已添加 ${images.size} 张图片",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 文本编辑区域
        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            label = { Text("内容") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            placeholder = { Text(placeholder) },
            maxLines = Int.MAX_VALUE
        )
        
        // 图片列表（简单显示）
        if (images.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "图片列表",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    images.forEach { image ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = image.fileName,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            
                            TextButton(
                                onClick = {
                                    val updatedImages = images.filter { it.fileName != image.fileName }
                                    onImagesChange(updatedImages)
                                    // 从内容中移除图片标记
                                    val imageMarker = "[IMG:${image.fileName}]"
                                    val newContent = content.replace(imageMarker, "")
                                    onContentChange(newContent)
                                }
                            ) {
                                Text("删除")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 简单的添加图片对话框
    if (showAddImageDialog) {
        AlertDialog(
            onDismissRequest = { showAddImageDialog = false },
            title = { Text("添加图片") },
            text = {
                Column {
                    Text("选择添加图片的方式：")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = {
                            // 模拟添加图片
                            val fileName = ImageUtils.generateImageFileName()
                            val imageMarker = "[IMG:$fileName]"
                            val newContent = if (content.isBlank()) {
                                imageMarker
                            } else {
                                content + "\n" + imageMarker
                            }
                            onContentChange(newContent)
                            
                            val newImage = NoteImage(
                                fileName = fileName,
                                position = newContent.indexOf(imageMarker),
                                insertTime = Date()
                            )
                            onImagesChange(images + newImage)
                            showAddImageDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("模拟添加图片")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddImageDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}