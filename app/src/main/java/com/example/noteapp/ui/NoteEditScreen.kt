package com.example.noteapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteapp.data.NoteEntity
import com.example.noteapp.data.NoteImage
import com.example.noteapp.utils.TagParser
import java.math.BigDecimal
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    note: NoteEntity? = null,
    onSave: (String, String, List<String>, List<NoteImage>) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var tagsText by remember { mutableStateOf(note?.tags?.joinToString(", ") ?: "") }
    var images by remember { mutableStateOf(note?.images ?: emptyList()) }
    var showAccountingDialog by remember { mutableStateOf(false) }
    
    val isNewNote = note == null
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isNewNote) "新建笔记" else "编辑笔记",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val tags = tagsText.split(",")
                                 .map { it.trim() }
                                 .filter { it.isNotBlank() }
                             if (tags.isNotEmpty()) {
                                 onSave(title.ifBlank { "无标题" }, content, tags, images)
                                 onBack()
                             }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "保存"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题输入
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 标签输入
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text("标签 (用逗号分隔)") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("例如: 工作, 记账_支出_50") }
                )
                
                // 快速添加记账标签按钮
                OutlinedButton(
                    onClick = { showAccountingDialog = true },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加记账标签",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("记账")
                }
            }
            
            // 简单稳定的富文本编辑器
            SimpleRichTextEditor(
                content = content,
                images = images,
                onContentChange = { content = it },
                onImagesChange = { images = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "请输入笔记内容"
            )
            
            // 保存按钮
            Button(
                onClick = {
                    val tags = tagsText.split(",")
                         .map { it.trim() }
                         .filter { it.isNotBlank() }
                     if (tags.isNotEmpty()) {
                         onSave(title.ifBlank { "无标题" }, content, tags, images)
                         onBack()
                     }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = tagsText.split(",").map { it.trim() }.filter { it.isNotBlank() }.isNotEmpty()
            ) {
                Text("保存笔记")
            }
        }
        
        // 记账标签创建对话框
        if (showAccountingDialog) {
            AccountingTagDialog(
                onDismiss = { showAccountingDialog = false },
                onTagCreated = { tag ->
                    val currentTags = if (tagsText.isBlank()) {
                        tag
                    } else {
                        "$tagsText, $tag"
                    }
                    tagsText = currentTags
                    showAccountingDialog = false
                }
            )
        }
    }
}

@Composable
fun AccountingTagDialog(
    onDismiss: () -> Unit,
    onTagCreated: (String) -> Unit
) {
    var selectedType by remember { mutableStateOf("支出") }
    var amount by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val accountingTypes = TagParser.getCommonAccountingTypes()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建记账标签") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 类型选择
                Text(
                    text = "记账类型",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accountingTypes.forEach { type ->
                        FilterChip(
                            onClick = { selectedType = type },
                            label = { Text(type) },
                            selected = selectedType == type
                        )
                    }
                }
                
                // 金额输入
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it
                        isError = false
                    },
                    label = { Text("金额") },
                    placeholder = { Text("请输入金额") },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text(errorMessage) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 预览
                 if (amount.isNotBlank()) {
                     val amountDecimal = amount.toBigDecimalOrNull()
                     if (amountDecimal != null) {
                         val previewTag = TagParser.createAccountingTag(selectedType, amountDecimal)
                         Text(
                             text = "预览: $previewTag",
                             fontSize = 12.sp,
                             color = MaterialTheme.colorScheme.primary
                         )
                     }
                 }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (amount.isBlank()) {
                        isError = true
                        errorMessage = "请输入金额"
                        return@TextButton
                    }
                    
                    try {
                        val amountDecimal = BigDecimal(amount)
                        if (amountDecimal <= BigDecimal.ZERO) {
                            isError = true
                            errorMessage = "金额必须大于0"
                            return@TextButton
                        }
                        
                        val tag = TagParser.createAccountingTag(selectedType, amountDecimal)
                        onTagCreated(tag)
                    } catch (e: NumberFormatException) {
                        isError = true
                        errorMessage = "金额格式错误"
                    }
                }
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}