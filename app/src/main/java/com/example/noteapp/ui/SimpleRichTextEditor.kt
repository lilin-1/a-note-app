package com.example.noteapp.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
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
    val context = LocalContext.current
    var showAddImageDialog by remember { mutableStateOf(false) }
    var tempCameraFile by remember { mutableStateOf<java.io.File?>(null) }
    
    // 相册选择器
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val fileName = ImageUtils.generateImageFileName()
                val targetFile = ImageUtils.getImageFile(context, fileName)
                
                val success = ImageUtils.compressImage(context, uri, targetFile)
                if (success) {
                    addImageToContent(fileName, content, images, onContentChange, onImagesChange)
                }
            } catch (e: Exception) {
                // 图片处理失败，忽略错误
            }
        }
    }
    
    // 相机拍摄器
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraFile != null) {
            try {
                val fileName = ImageUtils.generateImageFileName()
                val targetFile = ImageUtils.getImageFile(context, fileName)
                val sourceUri = android.net.Uri.fromFile(tempCameraFile)
                
                val compressSuccess = ImageUtils.compressImage(context, sourceUri, targetFile)
                if (compressSuccess) {
                    addImageToContent(fileName, content, images, onContentChange, onImagesChange)
                }
                // 删除临时文件
                tempCameraFile?.delete()
            } catch (e: Exception) {
                // 图片处理失败，忽略错误
            }
        }
        tempCameraFile = null
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 工具栏
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
                                    // 删除图片文件
                                    ImageUtils.deleteImageFile(context, image.fileName)
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
    
    // 图片选择对话框
    if (showAddImageDialog) {
        AlertDialog(
            onDismissRequest = { showAddImageDialog = false },
            title = { Text("添加图片") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("选择图片来源：")
                    
                    // 相机拍摄
                    OutlinedButton(
                        onClick = {
                            try {
                                tempCameraFile = ImageUtils.createTempImageFile(context)
                                val uri = ImageUtils.getFileUri(context, tempCameraFile!!)
                                cameraLauncher.launch(uri)
                                showAddImageDialog = false
                            } catch (e: Exception) {
                                // 相机启动失败，忽略错误
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "拍摄照片",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("拍摄照片")
                    }
                    
                    // 相册选择
                    OutlinedButton(
                        onClick = {
                            try {
                                galleryLauncher.launch("image/*")
                                showAddImageDialog = false
                            } catch (e: Exception) {
                                // 相册启动失败，忽略错误
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "从相册选择",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("从相册选择")
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

// 添加图片到内容的辅助函数
fun addImageToContent(
    fileName: String,
    content: String,
    images: List<NoteImage>,
    onContentChange: (String) -> Unit,
    onImagesChange: (List<NoteImage>) -> Unit
) {
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
}