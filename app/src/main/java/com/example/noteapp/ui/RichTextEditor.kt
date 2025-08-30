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
    val context = LocalContext.current
    var textFieldValue by remember(content) {
        mutableStateOf(TextFieldValue(content))
    }
    var showImagePicker by remember { mutableStateOf(false) }
    var currentCursorPosition by remember { mutableStateOf(0) }
    
    // 图片选择器
    val imagePicker = rememberImagePicker(context) { result ->
        if (result.success) {
            insertImageAtCursor(result.fileName, currentCursorPosition, images, onImagesChange)
        }
        showImagePicker = false
    }
    
    val imagePickerLaunchers = imagePicker.rememberLaunchers()
    
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
                onClick = {
                    currentCursorPosition = textFieldValue.selection.start
                    showImagePicker = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "插入图片",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("插入图片")
            }
        }
        
        // 富文本内容区域
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // 渲染富文本内容（文本和图片混合）
                RichTextContent(
                    content = textFieldValue.text,
                    images = images,
                    onContentChange = { newContent ->
                        textFieldValue = textFieldValue.copy(text = newContent)
                        onContentChange(newContent)
                    },
                    onImageDelete = { imageToDelete ->
                        val updatedImages = images.filter { it.fileName != imageToDelete.fileName }
                        onImagesChange(updatedImages)
                        // 删除图片文件
                        ImageUtils.deleteImageFile(context, imageToDelete.fileName)
                    },
                    onCursorPositionChange = { position ->
                        currentCursorPosition = position
                        textFieldValue = textFieldValue.copy(
                            selection = androidx.compose.ui.text.TextRange(position)
                        )
                    },
                    placeholder = placeholder
                )
            }
        }
    }
    
    // 图片选择对话框
    if (showImagePicker) {
        ImagePickerDialog(
            onDismiss = { showImagePicker = false },
            onCameraClick = {
                imagePickerLaunchers.cameraLauncher()
            },
            onGalleryClick = {
                imagePickerLaunchers.galleryLauncher()
            }
        )
    }
}

@Composable
fun RichTextContent(
    content: String,
    images: List<NoteImage>,
    onContentChange: (String) -> Unit,
    onImageDelete: (NoteImage) -> Unit,
    onCursorPositionChange: (Int) -> Unit,
    placeholder: String
) {
    val context = LocalContext.current
    var textFieldValue by remember(content) {
        mutableStateOf(TextFieldValue(content))
    }
    
    // 将内容按图片位置分割成段落
    val contentSegments = remember(content, images) {
        createContentSegments(content, images)
    }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(contentSegments) { index, segment ->
            when (segment) {
                is ContentSegment.Text -> {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            textFieldValue = newValue
                            onContentChange(newValue.text)
                            onCursorPositionChange(newValue.selection.start)
                        },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 100.dp),
                        decorationBox = { innerTextField ->
                            if (textFieldValue.text.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                }
                is ContentSegment.Image -> {
                    ImageItem(
                        image = segment.image,
                        context = context,
                        onDelete = { onImageDelete(segment.image) }
                    )
                }
            }
        }
    }
}

@Composable
fun ImageItem(
    image: NoteImage,
    context: Context,
    onDelete: () -> Unit
) {
    val imageFile = ImageUtils.getImageFile(context, image.fileName)
    
    if (imageFile.exists()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                Box {
                    Image(
                        painter = rememberAsyncImagePainter(imageFile),
                        contentDescription = image.caption.ifEmpty { "笔记图片" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    // 删除按钮
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除图片",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // 图片说明
                if (image.caption.isNotEmpty()) {
                    Text(
                        text = image.caption,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择图片") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 拍摄照片
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onCameraClick()
                            onDismiss()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "拍摄照片",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "拍摄照片",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // 从相册选择
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onGalleryClick()
                            onDismiss()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "从相册选择",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "从相册选择",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
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

// 内容段落类型
sealed class ContentSegment {
    data class Text(val content: String, val startPosition: Int) : ContentSegment()
    data class Image(val image: NoteImage) : ContentSegment()
}

// 创建内容段落
fun createContentSegments(content: String, images: List<NoteImage>): List<ContentSegment> {
    val segments = mutableListOf<ContentSegment>()
    val sortedImages = images.sortedBy { it.position }
    
    var lastPosition = 0
    
    sortedImages.forEach { image ->
        // 添加图片前的文本
        if (image.position > lastPosition) {
            val textContent = content.substring(lastPosition, minOf(image.position, content.length))
            if (textContent.isNotEmpty()) {
                segments.add(ContentSegment.Text(textContent, lastPosition))
            }
        }
        
        // 添加图片
        segments.add(ContentSegment.Image(image))
        lastPosition = image.position
    }
    
    // 添加最后的文本
    if (lastPosition < content.length) {
        val textContent = content.substring(lastPosition)
        if (textContent.isNotEmpty()) {
            segments.add(ContentSegment.Text(textContent, lastPosition))
        }
    }
    
    // 如果没有任何内容，添加一个空文本段落
    if (segments.isEmpty()) {
        segments.add(ContentSegment.Text(content, 0))
    }
    
    return segments
}

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