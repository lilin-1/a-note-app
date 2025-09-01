package com.example.noteapp.ui.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 主界面顶部栏组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    showFunctionMenu: Boolean,
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToFilter: () -> Unit,
    onNavigateToBackup: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "我的笔记",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            // 搜索按钮
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索"
                )
            }
            
            // 功能菜单按钮
            Box {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "功能菜单"
                    )
                }
                
                // 功能下拉菜单
                DropdownMenu(
                    expanded = showFunctionMenu,
                    onDismissRequest = onMenuDismiss
                ) {
                    DropdownMenuItem(
                        text = { Text("日历视图") },
                        onClick = {
                            onMenuDismiss()
                            onNavigateToCalendar()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "日历视图"
                            )
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("记账统计") },
                        onClick = {
                            onMenuDismiss()
                            onNavigateToStats()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = "记账统计"
                            )
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("日期筛选") },
                        onClick = {
                            onMenuDismiss()
                            onNavigateToFilter()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "日期筛选"
                            )
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("数据备份") },
                        onClick = {
                            onMenuDismiss()
                            onNavigateToBackup()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = "数据备份"
                            )
                        }
                    )
                }
            }
        }
    )
}