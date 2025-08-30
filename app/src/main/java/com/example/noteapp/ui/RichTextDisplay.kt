package com.example.noteapp.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import java.io.File

/**
 * 富文本显示组件
 * 解析文本中的图片标记并显示实际图片
 */
@Composable
fun RichTextDisplay(
    content: String,
    images: List<NoteImage>,
    modifier: Modifier = Modifier
) {
    // 暂时只显示纯文本，避免图片加载导致的崩溃
    Text(
        text = content,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun DisplayImageItem(
    image: NoteImage,
    context: Context
) {
    val imageFile = ImageUtils.getImageFile(context, image.fileName)
    
    if (imageFile.exists()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                Image(
                    painter = rememberAsyncImagePainter(imageFile),
                    contentDescription = image.caption.ifEmpty { "笔记图片" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
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
    } else {
        // 图片文件不存在时显示占位符
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = "图片文件不存在: ${image.fileName}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// 显示段落类型
sealed class DisplaySegment {
    data class Text(val text: String) : DisplaySegment()
    data class Image(val image: NoteImage) : DisplaySegment()
}

/**
 * 解析包含图片标记的文本内容
 * 将文本按图片标记分割，并创建对应的显示段落
 */
fun parseContentWithImages(content: String, images: List<NoteImage>): List<DisplaySegment> {
    val segments = mutableListOf<DisplaySegment>()
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
                segments.add(DisplaySegment.Text(textContent))
            }
        }
        
        // 添加图片
        val imageMarker = match.value
        imageMap[imageMarker]?.let { image ->
            segments.add(DisplaySegment.Image(image))
        }
        
        lastIndex = match.range.last + 1
    }
    
    // 添加最后的文本
    if (lastIndex < content.length) {
        val textContent = content.substring(lastIndex).trim()
        if (textContent.isNotEmpty()) {
            segments.add(DisplaySegment.Text(textContent))
        }
    }
    
    // 如果没有任何内容，添加原始文本
    if (segments.isEmpty() && content.isNotEmpty()) {
        segments.add(DisplaySegment.Text(content))
    }
    
    return segments
}