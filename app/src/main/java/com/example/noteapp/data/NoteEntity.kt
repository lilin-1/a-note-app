package com.example.noteapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

@Entity(tableName = "notes")
@TypeConverters(Converters::class)
data class NoteEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val creationTime: Date,
    val lastEditTime: Date,
    val tags: List<String>,
    val images: List<NoteImage> = emptyList(),
    val hasImages: Boolean = false
)

/**
 * 笔记图片数据类
 */
data class NoteImage(
    val fileName: String,        // 图片文件名
    val position: Int,           // 在内容中的位置（字符索引）
    val insertTime: Date,        // 插入时间
    val caption: String = ""     // 图片说明（可选）
)

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
    
    @TypeConverter
    fun fromNoteImageList(value: List<NoteImage>): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toNoteImageList(value: String): List<NoteImage> {
        val listType = object : TypeToken<List<NoteImage>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }
}