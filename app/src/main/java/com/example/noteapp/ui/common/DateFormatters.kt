package com.example.noteapp.ui.common

import java.text.SimpleDateFormat
import java.util.*

/**
 * 日期格式化工具类
 * 提供统一的日期格式化方法
 */
object DateFormatters {
    
    /**
     * 完整日期时间格式
     */
    val fullDateFormatter: SimpleDateFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    }
    
    /**
     * 仅日期格式
     */
    val dateOnlyFormatter: SimpleDateFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }
    
    /**
     * 仅时间格式
     */
    val timeOnlyFormatter: SimpleDateFormat by lazy {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }
    
    /**
     * 简短日期格式
     */
    val shortDateFormatter: SimpleDateFormat by lazy {
        SimpleDateFormat("MM-dd", Locale.getDefault())
    }
    
    /**
     * 中文日期格式
     */
    val chineseDateFormatter: SimpleDateFormat by lazy {
        SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
    }
}