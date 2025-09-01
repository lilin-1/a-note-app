package com.example.noteapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteapp.service.AccountingService
import java.math.BigDecimal

@Composable
fun AccountingStatsComponent(
    statistics: AccountingService.AccountingStatistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题
            Text(
                text = "记账统计",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // 总览卡片
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 收入卡片
                StatCard(
                    title = "总收入",
                    amount = statistics.totalIncome,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                
                // 支出卡片
                StatCard(
                    title = "总支出",
                    amount = statistics.totalExpense,
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 余额卡片
            StatCard(
                title = "余额",
                amount = statistics.balance,
                icon = Icons.Default.AccountBalance,
                color = if (statistics.balance >= BigDecimal.ZERO) {
                    Color(0xFF2196F3)
                } else {
                    Color(0xFFFF9800)
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // 按类型统计
            if (statistics.typeStatistics.isNotEmpty()) {
                Text(
                    text = "按类型统计",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(statistics.typeStatistics.toList().sortedByDescending { it.second }) { (type, amount) ->
                        TypeStatItem(
                            type = type,
                            amount = amount
                        )
                    }
                }
            }
            
            // 笔记数量
            Text(
                text = "记账笔记：${statistics.noteCount}条",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    amount: BigDecimal,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = formatAmount(amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun TypeStatItem(
    type: String,
    amount: BigDecimal,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = type,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = formatAmount(amount),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AccountingStatsDialog(
    statistics: AccountingService.AccountingStatistics,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("记账统计详情")
        },
        text = {
            AccountingStatsComponent(statistics = statistics)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

private fun formatAmount(amount: BigDecimal): String {
    return "¥${amount.stripTrailingZeros().toPlainString()}"
}