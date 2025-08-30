package com.example.noteapp.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.noteapp.data.NoteImage
import com.example.noteapp.utils.ImageUtils

/**
 * 简单稳定的富文本显示组件
 * 显示文本内容和图片标记，不加载实际图片避免崩溃
 */
@Composable
fun SimpleRichTextDisplay(
    content: String,
    images: List<NoteImage>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 解析并显示混合内容（文本和图片）
        val contentSegments = remember(content, images) {
            parseContentWithImageMarkers(content, images)
        }
        
        contentSegments.forEach { segment ->
            when (segment) {
                is ContentSegment.Text -> {
                    if (segment.text.isNotBlank()) {
                        Text(
                            text = segment.text,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is ContentSegment.Image -> {
                    SafeImageDisplay(
                        image = segment.image,
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
fun SafeImageDisplay(
    image: NoteImage,
    context: Context
) {
    val imageFile = ImageUtils.getImageFile(context, image.fileName)
    var imageLoadError by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
             if (imageFile.exists() && !imageLoadError) {
                 Image(
                     painter = rememberAsyncImagePainter(
                         model = imageFile,
                         onError = { imageLoadError = true }
                     ),
                     contentDescription = image.caption.ifEmpty { "笔记图片" },
                     modifier = Modifier
                         .fillMaxWidth()
                         .heightIn(max = 300.dp)
                         .clip(RoundedCornerShape(8.dp)),
                     contentScale = ContentScale.Crop
                 )
             }
            
            if (!imageFile.exists() || imageLoadError) {
                // 图片加载失败或文件不存在时的占位符
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = "图片加载失败",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (!imageFile.exists()) "图片文件不存在" else "图片加载失败",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = image.fileName,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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

// 内容段落类型
sealed class ContentSegment {
    data class Text(val text: String) : ContentSegment()
    data class Image(val image: NoteImage) : ContentSegment()
}

// 解析包含图片标记的内容
fun parseContentWithImageMarkers(content: String, images: List<NoteImage>): List<ContentSegment> {
    val segments = mutableListOf<ContentSegment>()
    val imageMap = images.associateBy { "[IMG:${it.fileName}]" }
    
    // 使用正则表达式查找所有图片标记
    val imagePattern = Regex("\\[IMG:[^\\]]+\\]")
    val matches = imagePattern.findAll(content).toList()
    
    var lastIndex = 0
    
    matches.forEach { match ->
        // 添加图片标记前的文本
        if (match.range.first > lastIndex) {
            val textContent = content.substring(lastIndex, match.range.first).trim()
            if (textContent.isNotEmpty()) {
                segments.add(ContentSegment.Text(textContent))
            }
        }
        
        // 添加图片
        val imageMarker = match.value
        imageMap[imageMarker]?.let { image ->
            segments.add(ContentSegment.Image(image))
        }
        
        lastIndex = match.range.last + 1
    }
    
    // 添加最后的文本
    if (lastIndex < content.length) {
        val textContent = content.substring(lastIndex).trim()
        if (textContent.isNotEmpty()) {
            segments.add(ContentSegment.Text(textContent))
        }
    }
    
    // 如果没有任何内容，添加原始文本
    if (segments.isEmpty() && content.isNotEmpty()) {
        segments.add(ContentSegment.Text(content))
    }
    
    return segments
}