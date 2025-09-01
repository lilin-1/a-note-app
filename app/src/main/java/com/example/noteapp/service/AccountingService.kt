package com.example.noteapp.service

import com.example.noteapp.data.NoteEntity
import com.example.noteapp.utils.TagParser
import java.math.BigDecimal
import java.util.*

/**
 * 记账服务类
 * 提供记账数据的统计和分析功能
 */
class AccountingService {
    
    /**
     * 记账统计数据类
     */
    data class AccountingStatistics(
        val totalIncome: BigDecimal,     // 总收入
        val totalExpense: BigDecimal,    // 总支出
        val balance: BigDecimal,         // 余额（收入-支出）
        val typeStatistics: Map<String, BigDecimal>, // 按类型统计
        val noteCount: Int               // 记账笔记数量
    )
    
    /**
     * 按类型分组的记账数据
     */
    data class AccountingByType(
        val type: String,
        val amount: BigDecimal,
        val notes: List<NoteEntity>
    )
    
    /**
     * 计算指定笔记列表的记账统计
     * @param notes 笔记列表
     * @return 记账统计数据
     */
    fun calculateStatistics(notes: List<NoteEntity>): AccountingStatistics {
        val accountingNotes = getAccountingNotes(notes)
        val allAccountingTags = accountingNotes.flatMap { TagParser.extractAccountingTags(it.tags) }
        
        val (totalIncome, totalExpense) = calculateTotals(allAccountingTags)
        val typeStatistics = calculateTypeStatistics(allAccountingTags)
        
        return AccountingStatistics(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = totalIncome.subtract(totalExpense),
            typeStatistics = typeStatistics,
            noteCount = accountingNotes.size
        )
    }
    
    /**
     * 计算总收入和总支出
     */
    private fun calculateTotals(accountingTags: List<TagParser.AccountingTag>): Pair<BigDecimal, BigDecimal> {
        var totalIncome = BigDecimal.ZERO
        var totalExpense = BigDecimal.ZERO
        
        accountingTags.forEach { tag ->
            when (tag.type) {
                "收入" -> totalIncome = totalIncome.add(tag.amount)
                else -> totalExpense = totalExpense.add(tag.amount) // 其他类型按支出处理
            }
        }
        
        return totalIncome to totalExpense
    }
    
    /**
     * 计算按类型分组的统计
     */
    private fun calculateTypeStatistics(accountingTags: List<TagParser.AccountingTag>): Map<String, BigDecimal> {
        return accountingTags.groupBy { it.type }
            .mapValues { (_, tags) -> tags.sumOf { it.amount } }
    }
    
    /**
     * 按类型分组记账数据
     * @param notes 笔记列表
     * @return 按类型分组的记账数据
     */
    fun groupByType(notes: List<NoteEntity>): List<AccountingByType> {
        val typeGroups = mutableMapOf<String, MutableList<Pair<BigDecimal, NoteEntity>>>()
        
        notes.forEach { note ->
            val accountingTags = TagParser.extractAccountingTags(note.tags)
            accountingTags.forEach { accountingTag ->
                val list = typeGroups.getOrPut(accountingTag.type) { mutableListOf() }
                list.add(accountingTag.amount to note)
            }
        }
        
        return typeGroups.map { (type, pairs) ->
            val totalAmount = pairs.sumOf { it.first }
            val notes = pairs.map { it.second }.distinctBy { it.id }
            AccountingByType(type, totalAmount, notes)
        }.sortedByDescending { it.amount }
    }
    
    /**
     * 筛选指定日期范围内的记账笔记
     * @param notes 笔记列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 筛选后的笔记列表
     */
    fun filterByDateRange(
        notes: List<NoteEntity>,
        startDate: Date,
        endDate: Date
    ): List<NoteEntity> {
        return notes.filter { note ->
            val hasAccountingTags = TagParser.extractAccountingTags(note.tags).isNotEmpty()
            hasAccountingTags && note.creationTime >= startDate && note.creationTime <= endDate
        }
    }
    
    /**
     * 筛选指定类型的记账笔记
     * @param notes 笔记列表
     * @param type 记账类型
     * @return 筛选后的笔记列表
     */
    fun filterByType(notes: List<NoteEntity>, type: String): List<NoteEntity> {
        return notes.filter { note ->
            TagParser.extractAccountingTags(note.tags).any { it.type == type }
        }
    }
    
    /**
     * 获取所有记账笔记
     * @param notes 笔记列表
     * @return 包含记账标签的笔记列表
     */
    fun getAccountingNotes(notes: List<NoteEntity>): List<NoteEntity> {
        return notes.filter { note ->
            TagParser.extractAccountingTags(note.tags).isNotEmpty()
        }
    }
    
    /**
     * 格式化金额显示
     * @param amount 金额
     * @return 格式化后的金额字符串
     */
    fun formatAmount(amount: BigDecimal): String {
        return "¥${amount.stripTrailingZeros().toPlainString()}"
    }
    
    /**
     * 获取记账摘要信息
     * @param notes 笔记列表
     * @return 摘要字符串
     */
    fun getSummary(notes: List<NoteEntity>): String {
        val statistics = calculateStatistics(notes)
        return buildString {
            append("记账笔记：${statistics.noteCount}条\n")
            append("总收入：${formatAmount(statistics.totalIncome)}\n")
            append("总支出：${formatAmount(statistics.totalExpense)}\n")
            append("余额：${formatAmount(statistics.balance)}")
        }
    }
}