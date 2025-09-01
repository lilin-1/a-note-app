package com.example.noteapp.model.domain

import com.example.noteapp.data.NoteEntity

/**
 * NoteEntity扩展函数
 * 将数据库实体转换为领域模型
 */
fun NoteEntity.toNote(): Note {
    return Note(
        id = this.id,
        title = this.title,
        content = this.content,
        creationTime = this.creationTime,
        lastEditTime = this.lastEditTime,
        tags = this.tags,
        images = this.images,
        hasImages = this.hasImages
    )
}

/**
 * 批量转换扩展函数
 */
fun List<NoteEntity>.toNotes(): List<Note> {
    return this.map { it.toNote() }
}