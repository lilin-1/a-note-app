package com.example.noteapp.ui.components.note

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteapp.model.domain.Note
import com.example.noteapp.ui.common.UIComponents
import com.example.noteapp.ui.common.DateFormatters
import com.example.noteapp.ui.common.Dimensions

/**
 * 笔记项组件
 */
@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.paddingSmall)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(Dimensions.cornerRadiusMedium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            NoteContent(
                note = note,
                modifier = Modifier.weight(1f)
            )
            
            NoteActions(
                note = note,
                showMenu = showMenu,
                onMenuToggle = { showMenu = !showMenu },
                onDelete = {
                    showMenu = false
                    onDelete()
                }
            )
        }
    }
}

/**
 * 笔记内容组件
 */
@Composable
private fun NoteContent(
    note: Note,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 标题
        Text(
            text = note.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
        
        // 内容
        Text(
            text = note.content,
            fontSize = 14.sp,
            color = Color.Gray,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
        
        // 标签
        UIComponents.TagList(
            tags = note.tags,
            maxVisible = 2
        )
        
        Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
        
        // 时间信息
        NoteTimeInfo(note = note)
    }
}

/**
 * 笔记时间信息组件
 */
@Composable
private fun NoteTimeInfo(note: Note) {
    Column {
        Text(
            text = "创建: ${DateFormatters.fullDateFormatter.format(note.creationTime)}",
            fontSize = 11.sp,
            color = Color.Gray
        )
        if (note.creationTime != note.lastEditTime) {
            Text(
                text = "修改: ${DateFormatters.fullDateFormatter.format(note.lastEditTime)}",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * 笔记操作组件
 */
@Composable
private fun NoteActions(
    note: Note,
    showMenu: Boolean,
    onMenuToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
    ) {
        // 管理按钮
        Box {
            UIComponents.IconButtonWithDescription(
                icon = Icons.Default.MoreVert,
                contentDescription = "管理笔记",
                onClick = onMenuToggle,
                modifier = Modifier.size(Dimensions.iconSizeLarge),
                size = Dimensions.iconSizeMedium
            )
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = onMenuToggle
            ) {
                DropdownMenuItem(
                    text = { Text("删除笔记") },
                    onClick = onDelete,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除"
                        )
                    }
                )
            }
        }
        
        // 图片标识
        if (note.hasImages) {
            NoteImageIndicator()
        }
    }
}

/**
 * 笔记图片指示器组件
 */
@Composable
private fun NoteImageIndicator() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(Dimensions.cornerRadiusSmall)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Photo,
            contentDescription = "包含图片",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimensions.iconSizeMedium)
        )
    }
}