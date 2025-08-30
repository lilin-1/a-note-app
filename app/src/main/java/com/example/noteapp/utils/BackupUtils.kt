package com.example.noteapp.utils

import android.content.Context
import android.net.Uri
import com.example.noteapp.data.NoteDatabase
import com.example.noteapp.data.NoteEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * 数据备份和恢复工具类
 * 支持将所有笔记数据和图片文件打包为ZIP文件，以及从ZIP文件恢复数据
 */
object BackupUtils {
    
    private const val BACKUP_VERSION = "1.0"
    private const val NOTES_JSON_FILE = "notes.json"
    private const val IMAGES_FOLDER = "images/"
    private const val METADATA_FILE = "backup_metadata.json"
    
    /**
     * 备份元数据
     */
    data class BackupMetadata(
        val version: String,
        val timestamp: Long,
        val noteCount: Int,
        val imageCount: Int,
        val appVersion: String = "1.0"
    )
    
    /**
     * 备份结果
     */
    data class BackupResult(
        val success: Boolean,
        val message: String,
        val filePath: String? = null,
        val noteCount: Int = 0,
        val imageCount: Int = 0
    )
    
    /**
     * 恢复结果
     */
    data class RestoreResult(
        val success: Boolean,
        val message: String,
        val noteCount: Int = 0,
        val imageCount: Int = 0,
        val skippedImages: Int = 0
    )
    
    /**
     * 创建完整的数据备份
     * @param context 应用上下文
     * @param outputUri 输出文件URI
     * @return 备份结果
     */
    suspend fun createBackup(
        context: Context,
        outputUri: Uri
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            val database = NoteDatabase.getDatabase(context)
            val noteDao = database.noteDao()
            
            // 获取所有笔记数据
            val notes = noteDao.getAllNotesSync()
            
            // 获取所有图片文件
            val imageDirectory = ImageUtils.getImageDirectory(context)
            val imageFiles = imageDirectory.listFiles()?.filter { it.isFile } ?: emptyList()
            
            // 创建备份元数据
            val metadata = BackupMetadata(
                version = BACKUP_VERSION,
                timestamp = System.currentTimeMillis(),
                noteCount = notes.size,
                imageCount = imageFiles.size
            )
            
            // 创建ZIP文件
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                    
                    // 添加元数据文件
                    addMetadataToZip(zipOut, metadata)
                    
                    // 添加笔记数据
                    addNotesToZip(zipOut, notes)
                    
                    // 添加图片文件
                    addImagesToZip(zipOut, imageFiles)
                }
            }
            
            BackupResult(
                success = true,
                message = "备份成功！包含 ${notes.size} 条笔记和 ${imageFiles.size} 张图片",
                filePath = outputUri.toString(),
                noteCount = notes.size,
                imageCount = imageFiles.size
            )
            
        } catch (e: Exception) {
            BackupResult(
                success = false,
                message = "备份失败：${e.message}"
            )
        }
    }
    
    /**
     * 从备份文件恢复数据
     * @param context 应用上下文
     * @param inputUri 备份文件URI
     * @param replaceExisting 是否替换现有数据
     * @return 恢复结果
     */
    suspend fun restoreFromBackup(
        context: Context,
        inputUri: Uri,
        replaceExisting: Boolean = false
    ): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val database = NoteDatabase.getDatabase(context)
            val noteDao = database.noteDao()
            
            var restoredNotes = 0
            var restoredImages = 0
            var skippedImages = 0
            
            context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                    
                    var entry: ZipEntry?
                    var metadata: BackupMetadata? = null
                    var notes: List<NoteEntity>? = null
                    
                    // 读取ZIP文件内容
                    while (zipIn.nextEntry.also { entry = it } != null) {
                        when (entry!!.name) {
                            METADATA_FILE -> {
                                metadata = readMetadataFromZip(zipIn)
                            }
                            NOTES_JSON_FILE -> {
                                notes = readNotesFromZip(zipIn)
                            }
                            else -> {
                                if (entry!!.name.startsWith(IMAGES_FOLDER)) {
                                    val restored = restoreImageFromZip(context, zipIn, entry!!)
                                    if (restored) restoredImages++ else skippedImages++
                                }
                            }
                        }
                        zipIn.closeEntry()
                    }
                    
                    // 验证备份文件
                    if (metadata == null || notes == null) {
                        return@withContext RestoreResult(
                            success = false,
                            message = "无效的备份文件格式"
                        )
                    }
                    
                    // 恢复笔记数据
                    if (replaceExisting) {
                        // 清空现有数据
                        noteDao.deleteAllNotes()
                        // 清空图片目录
                        ImageUtils.getImageDirectory(context).listFiles()?.forEach { it.delete() }
                    }
                    
                    // 插入笔记数据
                    notes.forEach { note ->
                        try {
                            if (replaceExisting || noteDao.getNoteById(note.id) == null) {
                                noteDao.insertNote(note)
                                restoredNotes++
                            }
                        } catch (e: Exception) {
                            // 忽略重复的笔记
                        }
                    }
                }
            }
            
            RestoreResult(
                success = true,
                message = "恢复成功！恢复了 $restoredNotes 条笔记和 $restoredImages 张图片",
                noteCount = restoredNotes,
                imageCount = restoredImages,
                skippedImages = skippedImages
            )
            
        } catch (e: Exception) {
            RestoreResult(
                success = false,
                message = "恢复失败：${e.message}"
            )
        }
    }
    
    /**
     * 生成备份文件名
     */
    fun generateBackupFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        return "noteapp_backup_$timestamp.zip"
    }
    
    /**
     * 验证备份文件
     */
    suspend fun validateBackupFile(
        context: Context,
        inputUri: Uri
    ): BackupMetadata? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                    var entry: ZipEntry?
                    while (zipIn.nextEntry.also { entry = it } != null) {
                        if (entry!!.name == METADATA_FILE) {
                            return@withContext readMetadataFromZip(zipIn)
                        }
                        zipIn.closeEntry()
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
    
    // 私有辅助方法
    
    private fun addMetadataToZip(zipOut: ZipOutputStream, metadata: BackupMetadata) {
        val entry = ZipEntry(METADATA_FILE)
        zipOut.putNextEntry(entry)
        val json = Gson().toJson(metadata)
        zipOut.write(json.toByteArray())
        zipOut.closeEntry()
    }
    
    private fun addNotesToZip(zipOut: ZipOutputStream, notes: List<NoteEntity>) {
        val entry = ZipEntry(NOTES_JSON_FILE)
        zipOut.putNextEntry(entry)
        val json = Gson().toJson(notes)
        zipOut.write(json.toByteArray())
        zipOut.closeEntry()
    }
    
    private fun addImagesToZip(zipOut: ZipOutputStream, imageFiles: List<File>) {
        imageFiles.forEach { file ->
            val entry = ZipEntry(IMAGES_FOLDER + file.name)
            zipOut.putNextEntry(entry)
            
            file.inputStream().use { input ->
                input.copyTo(zipOut)
            }
            
            zipOut.closeEntry()
        }
    }
    
    private fun readMetadataFromZip(zipIn: ZipInputStream): BackupMetadata {
        val json = zipIn.readBytes().toString(Charsets.UTF_8)
        return Gson().fromJson(json, BackupMetadata::class.java)
    }
    
    private fun readNotesFromZip(zipIn: ZipInputStream): List<NoteEntity> {
        val json = zipIn.readBytes().toString(Charsets.UTF_8)
        val listType = object : TypeToken<List<NoteEntity>>() {}.type
        return Gson().fromJson(json, listType)
    }
    
    private fun restoreImageFromZip(
        context: Context,
        zipIn: ZipInputStream,
        entry: ZipEntry
    ): Boolean {
        return try {
            val fileName = entry.name.removePrefix(IMAGES_FOLDER)
            val targetFile = ImageUtils.getImageFile(context, fileName)
            
            // 创建目录（如果不存在）
            targetFile.parentFile?.mkdirs()
            
            targetFile.outputStream().use { output ->
                zipIn.copyTo(output)
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * 扩展函数：为NoteDao添加同步获取所有笔记的方法
 */
suspend fun com.example.noteapp.data.NoteDao.getAllNotesSync(): List<NoteEntity> {
    return withContext(Dispatchers.IO) {
        // 这里需要根据实际的DAO方法来实现
        // 假设已经有getAllNotes()方法返回Flow<List<NoteEntity>>
        // 我们需要一个同步版本
        getAllNotesForBackup()
    }
}