package com.example.noteapp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 通用UI组件工具类
 * 提供可复用的UI组件，减少代码重复
 */
object UIComponents {
    
    /**
     * 标签芯片组件
     */
    @Composable
    fun TagChip(
        text: String,
        modifier: Modifier = Modifier,
        backgroundColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        textColor: Color = MaterialTheme.colorScheme.primary,
        fontSize: TextUnit = 12.sp,
        padding: PaddingValues = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            color = textColor,
            modifier = modifier
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(padding)
        )
    }
    
    /**
     * 标签列表组件
     */
    @Composable
    fun TagList(
        tags: List<String>,
        maxVisible: Int = 2,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tags.take(maxVisible).forEach { tag ->
                TagChip(text = tag)
            }
            if (tags.size > maxVisible) {
                Text(
                    text = "+${tags.size - maxVisible}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
    
    /**
     * 图标按钮组件
     */
    @Composable
    fun IconButtonWithDescription(
        icon: ImageVector,
        contentDescription: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        size: Dp = 24.dp,
        tint: Color = LocalContentColor.current
    ) {
        IconButton(
            onClick = onClick,
            modifier = modifier
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(size),
                tint = tint
            )
        }
    }
    
    /**
     * 信息卡片组件
     */
    @Composable
    fun InfoCard(
        title: String,
        content: @Composable () -> Unit,
        modifier: Modifier = Modifier,
        backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                content()
            }
        }
    }
    
    /**
     * 统计项组件
     */
    @Composable
    fun StatItem(
        label: String,
        value: String,
        modifier: Modifier = Modifier,
        valueColor: Color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 14.sp
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = valueColor
            )
        }
    }
    
    /**
     * 空状态组件
     */
    @Composable
    fun EmptyState(
        message: String,
        modifier: Modifier = Modifier,
        icon: ImageVector? = null
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = message,
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

// DateFormatters和Dimensions已移至独立文件

/**
 * 常用颜色扩展
 */
val Color.Companion.LightGray: Color
    get() = Color(0xFF9E9E9E)

val Color.Companion.DarkGray: Color
    get() = Color(0xFF424242)