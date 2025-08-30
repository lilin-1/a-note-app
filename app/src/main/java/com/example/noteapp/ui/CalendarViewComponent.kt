package com.example.noteapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteapp.data.NoteEntity
import java.text.SimpleDateFormat
import java.util.*

data class CalendarDay(
    val date: Date,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val notes: List<NoteEntity>,
    val hasFilteredTag: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarViewComponent(
    notes: List<NoteEntity>,
    selectedTag: String? = null,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    val monthFormatter = remember { SimpleDateFormat("yyyy年MM月", Locale.getDefault()) }
    
    // 生成日历数据
    val calendarDays = remember(currentMonth, notes, selectedTag) {
        generateCalendarDays(currentMonth, notes, selectedTag)
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 月份导航
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        currentMonth = Calendar.getInstance().apply {
                            time = currentMonth.time
                            add(Calendar.MONTH, -1)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "上个月"
                    )
                }
                
                Text(
                    text = monthFormatter.format(currentMonth.time),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                IconButton(
                    onClick = {
                        currentMonth = Calendar.getInstance().apply {
                            time = currentMonth.time
                            add(Calendar.MONTH, 1)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "下个月"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 星期标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 日历网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(calendarDays) { day ->
                    CalendarDayItem(
                        day = day,
                        onClick = { onDateSelected(day.date) }
                    )
                }
            }
            
            // 图例
            if (selectedTag != null) {
                Spacer(modifier = Modifier.height(16.dp))
                CalendarLegend(selectedTag = selectedTag)
            }
        }
    }
}

@Composable
fun CalendarDayItem(
    day: CalendarDay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        day.isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        day.hasFilteredTag -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
        day.notes.isNotEmpty() -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.Transparent
    }
    
    val textColor = when {
        !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        day.isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                fontSize = 14.sp,
                color = textColor,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
            )
            
            // 笔记指示器
            if (day.notes.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (day.hasFilteredTag) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                )
            }
        }
    }
}

@Composable
fun CalendarLegend(
    selectedTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "图例",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 有笔记的日期
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = "有笔记",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 有指定标签的日期
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                    Text(
                        text = "含\"$selectedTag\"标签",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarTagFilterDialog(
    allTags: List<String>,
    selectedTag: String?,
    onTagSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择标签筛选") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                // 全部选项
                item {
                    FilterChip(
                        onClick = { onTagSelected(null) },
                        label = { Text("全部") },
                        selected = selectedTag == null
                    )
                }
                
                // 标签选项
                items(allTags) { tag ->
                    FilterChip(
                        onClick = { onTagSelected(tag) },
                        label = { Text(tag) },
                        selected = selectedTag == tag
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

// 生成日历数据的工具函数
fun generateCalendarDays(
    currentMonth: Calendar,
    notes: List<NoteEntity>,
    selectedTag: String?
): List<CalendarDay> {
    val calendar = Calendar.getInstance()
    val today = Calendar.getInstance()
    val result = mutableListOf<CalendarDay>()
    
    // 设置到当前月的第一天
    calendar.time = currentMonth.time
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    
    // 获取当前月第一天是星期几
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    
    // 添加上个月的日期（填充第一周）
    val prevMonth = Calendar.getInstance().apply {
        time = calendar.time
        add(Calendar.MONTH, -1)
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
    }
    
    for (i in firstDayOfWeek - 1 downTo 1) {
        val date = Calendar.getInstance().apply {
            time = prevMonth.time
            add(Calendar.DAY_OF_MONTH, -(i - 1))
        }.time
        
        val dayNotes = getNotesForDate(notes, date)
        val hasFilteredTag = selectedTag?.let { tag ->
            dayNotes.any { note -> note.tags.contains(tag) }
        } ?: false
        
        result.add(
            CalendarDay(
                date = date,
                dayOfMonth = Calendar.getInstance().apply { time = date }.get(Calendar.DAY_OF_MONTH),
                isCurrentMonth = false,
                isToday = false,
                notes = dayNotes,
                hasFilteredTag = hasFilteredTag
            )
        )
    }
    
    // 添加当前月的日期
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (day in 1..daysInMonth) {
        calendar.set(Calendar.DAY_OF_MONTH, day)
        val date = calendar.time
        
        val isToday = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        
        val dayNotes = getNotesForDate(notes, date)
        val hasFilteredTag = selectedTag?.let { tag ->
            dayNotes.any { note -> note.tags.contains(tag) }
        } ?: false
        
        result.add(
            CalendarDay(
                date = date,
                dayOfMonth = day,
                isCurrentMonth = true,
                isToday = isToday,
                notes = dayNotes,
                hasFilteredTag = hasFilteredTag
            )
        )
    }
    
    // 添加下个月的日期（填充最后一周）
    val nextMonth = Calendar.getInstance().apply {
        time = calendar.time
        add(Calendar.MONTH, 1)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    
    val remainingDays = 42 - result.size // 6周 * 7天
    for (day in 1..remainingDays) {
        nextMonth.set(Calendar.DAY_OF_MONTH, day)
        val date = nextMonth.time
        
        val dayNotes = getNotesForDate(notes, date)
        val hasFilteredTag = selectedTag?.let { tag ->
            dayNotes.any { note -> note.tags.contains(tag) }
        } ?: false
        
        result.add(
            CalendarDay(
                date = date,
                dayOfMonth = day,
                isCurrentMonth = false,
                isToday = false,
                notes = dayNotes,
                hasFilteredTag = hasFilteredTag
            )
        )
    }
    
    return result
}

// 获取指定日期的笔记
fun getNotesForDate(notes: List<NoteEntity>, date: Date): List<NoteEntity> {
    val calendar = Calendar.getInstance()
    calendar.time = date
    val targetYear = calendar.get(Calendar.YEAR)
    val targetDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
    
    return notes.filter { note ->
        calendar.time = note.creationTime
        calendar.get(Calendar.YEAR) == targetYear &&
                calendar.get(Calendar.DAY_OF_YEAR) == targetDayOfYear
    }
}