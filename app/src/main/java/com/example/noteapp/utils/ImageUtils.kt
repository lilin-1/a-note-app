package com.example.noteapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 图片工具类
 * 处理图片选择、压缩、存储等功能
 */
object ImageUtils {
    
    // 图片存储目录
    private const val IMAGE_DIRECTORY = "note_images"
    
    // 图片压缩质量
    private const val COMPRESS_QUALITY = 80
    
    // 最大图片尺寸
    private const val MAX_IMAGE_SIZE = 1024
    
    /**
     * 获取图片存储目录
     */
    fun getImageDirectory(context: Context): File {
        val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory
    }
    
    /**
     * 生成唯一的图片文件名
     */
    fun generateImageFileName(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "IMG_${timeStamp}_${UUID.randomUUID().toString().substring(0, 8)}.jpg"
    }
    
    /**
     * 创建临时图片文件用于相机拍摄
     */
    fun createTempImageFile(context: Context): File {
        val imageDirectory = getImageDirectory(context)
        val fileName = generateImageFileName()
        return File(imageDirectory, fileName)
    }
    
    /**
     * 获取文件的Uri（用于相机拍摄）
     */
    fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
    
    /**
     * 压缩图片
     */
    fun compressImage(context: Context, sourceUri: Uri, targetFile: File): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap == null) return false
            
            // 获取图片旋转角度
            val rotation = getImageRotation(context, sourceUri)
            
            // 计算压缩比例
            val (width, height) = calculateInSampleSize(
                originalBitmap.width,
                originalBitmap.height,
                MAX_IMAGE_SIZE,
                MAX_IMAGE_SIZE
            )
            
            // 缩放图片
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)
            
            // 旋转图片（如果需要）
            val rotatedBitmap = if (rotation != 0f) {
                val matrix = Matrix().apply { postRotate(rotation) }
                Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
            } else {
                scaledBitmap
            }
            
            // 保存压缩后的图片
            val outputStream = FileOutputStream(targetFile)
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, outputStream)
            outputStream.close()
            
            // 释放内存
            if (originalBitmap != scaledBitmap) originalBitmap.recycle()
            if (scaledBitmap != rotatedBitmap) scaledBitmap.recycle()
            rotatedBitmap.recycle()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 获取图片旋转角度
     */
    private fun getImageRotation(context: Context, uri: Uri): Float {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = ExifInterface(inputStream!!)
            inputStream.close()
            
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (e: Exception) {
            0f
        }
    }
    
    /**
     * 计算合适的图片尺寸
     */
    private fun calculateInSampleSize(
        originalWidth: Int,
        originalHeight: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Pair<Int, Int> {
        var width = originalWidth
        var height = originalHeight
        
        if (width > reqWidth || height > reqHeight) {
            val widthRatio = width.toFloat() / reqWidth.toFloat()
            val heightRatio = height.toFloat() / reqHeight.toFloat()
            val ratio = maxOf(widthRatio, heightRatio)
            
            width = (width / ratio).toInt()
            height = (height / ratio).toInt()
        }
        
        return Pair(width, height)
    }
    
    /**
     * 删除图片文件
     */
    fun deleteImageFile(context: Context, fileName: String): Boolean {
        return try {
            val imageDirectory = getImageDirectory(context)
            val file = File(imageDirectory, fileName)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取图片文件
     */
    fun getImageFile(context: Context, fileName: String): File {
        val imageDirectory = getImageDirectory(context)
        return File(imageDirectory, fileName)
    }
    
    /**
     * 检查图片文件是否存在
     */
    fun imageFileExists(context: Context, fileName: String): Boolean {
        val file = getImageFile(context, fileName)
        return file.exists()
    }
    
    /**
     * 获取图片文件大小（字节）
     */
    fun getImageFileSize(context: Context, fileName: String): Long {
        val file = getImageFile(context, fileName)
        return if (file.exists()) file.length() else 0L
    }
    
    /**
     * 清理无用的图片文件
     */
    fun cleanupUnusedImages(context: Context, usedImageNames: Set<String>) {
        try {
            val imageDirectory = getImageDirectory(context)
            imageDirectory.listFiles()?.forEach { file ->
                if (file.isFile && file.name !in usedImageNames) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * 图片选择器数据类
 */
data class ImagePickerResult(
    val fileName: String,
    val success: Boolean,
    val error: String? = null
)

/**
 * 图片选择器Composable
 */
@Composable
fun rememberImagePicker(
    context: Context,
    onImageSelected: (ImagePickerResult) -> Unit
): ImagePicker {
    return remember {
        ImagePicker(context, onImageSelected)
    }
}

class ImagePicker(
    private val context: Context,
    private val onImageSelected: (ImagePickerResult) -> Unit
) {
    private var tempCameraFile: File? = null
    
    @Composable
    fun rememberLaunchers(): ImagePickerLaunchers {
        // 相册选择器
        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let { processSelectedImage(it) }
        }
        
        // 相机拍摄器
        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && tempCameraFile != null) {
                val uri = Uri.fromFile(tempCameraFile)
                processSelectedImage(uri)
            } else {
                onImageSelected(ImagePickerResult("", false, "拍摄失败"))
            }
        }
        
        return ImagePickerLaunchers(
            galleryLauncher = { galleryLauncher.launch("image/*") },
            cameraLauncher = {
                tempCameraFile = ImageUtils.createTempImageFile(context)
                val uri = ImageUtils.getFileUri(context, tempCameraFile!!)
                cameraLauncher.launch(uri)
            }
        )
    }
    
    private fun processSelectedImage(sourceUri: Uri) {
        try {
            val fileName = ImageUtils.generateImageFileName()
            val targetFile = ImageUtils.getImageFile(context, fileName)
            
            val success = ImageUtils.compressImage(context, sourceUri, targetFile)
            
            if (success) {
                onImageSelected(ImagePickerResult(fileName, true))
            } else {
                onImageSelected(ImagePickerResult("", false, "图片处理失败"))
            }
        } catch (e: Exception) {
            onImageSelected(ImagePickerResult("", false, "图片处理异常：${e.message}"))
        }
    }
}

data class ImagePickerLaunchers(
    val galleryLauncher: () -> Unit,
    val cameraLauncher: () -> Unit
)