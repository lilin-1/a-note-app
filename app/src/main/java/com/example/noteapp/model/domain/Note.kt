package com.example.noteapp.model.domain

import com.example.noteapp.data.NoteImage
import java.util.*

/**
 * 笔记领域模型
 * 用于UI层显示的笔记数据结构
 */
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val creationTime: Date,
    val lastEditTime: Date,
    val tags: List<String>,
    val images: List<NoteImage> = emptyList(),
    val hasImages: Boolean = false
)