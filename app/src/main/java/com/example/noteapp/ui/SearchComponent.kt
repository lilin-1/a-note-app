package com.example.noteapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteapp.repository.SearchType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchComponent(
    searchQuery: String,
    searchType: SearchType,
    isSearching: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSearchTypeChange: (SearchType) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 搜索输入框
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("搜索笔记") },
            placeholder = { Text(getSearchPlaceholder(searchType)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索图标"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清除搜索"
                        )
                    }
                }
            }
        )
        
        // 搜索类型选择
        if (isSearching || searchQuery.isNotEmpty()) {
            SearchTypeSelector(
                selectedType = searchType,
                onTypeSelected = onSearchTypeChange
            )
        }
    }
}

@Composable
fun SearchTypeSelector(
    selectedType: SearchType,
    onTypeSelected: (SearchType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "搜索范围",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SearchTypeOption(
                text = "全部",
                selected = selectedType == SearchType.ALL,
                onClick = { onTypeSelected(SearchType.ALL) }
            )
            
            SearchTypeOption(
                text = "标题",
                selected = selectedType == SearchType.TITLE,
                onClick = { onTypeSelected(SearchType.TITLE) }
            )
            
            SearchTypeOption(
                text = "内容",
                selected = selectedType == SearchType.CONTENT,
                onClick = { onTypeSelected(SearchType.CONTENT) }
            )
            
            SearchTypeOption(
                text = "标签",
                selected = selectedType == SearchType.TAG,
                onClick = { onTypeSelected(SearchType.TAG) }
            )
        }
    }
}

@Composable
fun SearchTypeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 14.sp
        )
    }
}

fun getSearchPlaceholder(searchType: SearchType): String {
    return when (searchType) {
        SearchType.ALL -> "搜索标题、内容或标签..."
        SearchType.TITLE -> "搜索笔记标题..."
        SearchType.CONTENT -> "搜索笔记内容..."
        SearchType.TAG -> "搜索标签..."
    }
}