package com.example.noteapp.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.noteapp.data.NoteImage
import com.example.noteapp.utils.ImagePicker
import com.example.noteapp.utils.ImagePickerResult
import com.example.noteapp.utils.ImageUtils
import com.example.noteapp.utils.rememberImagePicker
import java.io.File
import java.util.*

/**
 * 富文本编辑器组件
 * 支持文本编辑和图片插入
 */
@Composable
fun RichTextEditor(
    content: String,
    images: List<NoteImage>,
    onContentChange: (String) -> Unit,
    onImagesChange: (List<NoteImage>) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "请输入内容..."
) {
    // 暂时移除所有图片功能，只保留文本编辑，确保稳定性
    OutlinedTextField(
        value = content,
        onValueChange = onContentChange,
        label = { Text("内容") },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp),
        placeholder = { Text(placeholder) },
        maxLines = Int.MAX_VALUE
    )
}

// 移除复杂的RichTextContent组件，避免崩溃

// 移除复杂的ImageItem组件

// 移除复杂的ImagePickerDialog组件

// 移除复杂的内容分割逻辑

@Composable
fun SimpleImageItem(
    image: NoteImage,
    context: Context,
    onDelete: () -> Unit
) {
    val imageFile = ImageUtils.getImageFile(context, image.fileName)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "图片",
                modifier = Modifier.size(20.dp)
            )
            
            Column {
                Text(
                    text = image.fileName,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                if (imageFile.exists()) {
                    Text(
                        text = "${imageFile.length() / 1024}KB",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SimpleImagePickerDialog(
    onDismiss: () -> Unit,
    onImageSelected: (String) -> Unit,
    context: Context
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加图片") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("选择图片来源：")
                
                // 相机拍摄
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // 模拟图片选择，实际应用中需要实现相机功能
                            val fileName = ImageUtils.generateImageFileName()
                            onImageSelected(fileName)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "拍摄照片"
                        )
                        Text("拍摄照片")
                    }
                }
                
                // 相册选择
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // 模拟图片选择，实际应用中需要实现相册选择功能
                            val fileName = ImageUtils.generateImageFileName()
                            onImageSelected(fileName)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "从相册选择"
                        )
                        Text("从相册选择")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

data class ImagePickerLaunchers(
    val galleryLauncher: () -> Unit,
    val cameraLauncher: () -> Unit
)

// 在光标位置插入图片
fun insertImageAtCursor(
    fileName: String,
    cursorPosition: Int,
    currentImages: List<NoteImage>,
    onImagesChange: (List<NoteImage>) -> Unit
) {
    val newImage = NoteImage(
        fileName = fileName,
        position = cursorPosition,
        insertTime = Date()
    )
    
    val updatedImages = currentImages.toMutableList()
    updatedImages.add(newImage)
    
    onImagesChange(updatedImages)
}