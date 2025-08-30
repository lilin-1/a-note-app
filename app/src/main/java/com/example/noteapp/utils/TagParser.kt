package com.example.noteapp.utils

import java.math.BigDecimal

/**
 * 标签解析工具类
 * 用于解析特定格式的标签，如记账标签：记账_类型_金额
 */
object TagParser {
    
    // 记账标签的正则表达式：记账_类型_金额
    private val ACCOUNTING_TAG_REGEX = Regex("^记账_([^_]+)_([0-9]+(?:\\.[0-9]+)?)$")
    
    /**
     * 记账标签数据类
     */
    data class AccountingTag(
        val type: String,        // 类型：支出、收入等
        val amount: BigDecimal,  // 金额
        val originalTag: String  // 原始标签
    )
    
    /**
     * 解析记账标签
     * @param tag 标签字符串
     * @return AccountingTag对象，如果不是记账标签则返回null
     */
    fun parseAccountingTag(tag: String): AccountingTag? {
        val matchResult = ACCOUNTING_TAG_REGEX.matchEntire(tag.trim())
        return if (matchResult != null) {
            val type = matchResult.groupValues[1]
            val amountStr = matchResult.groupValues[2]
            try {
                val amount = BigDecimal(amountStr)
                AccountingTag(type, amount, tag)
            } catch (e: NumberFormatException) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * 从标签列表中提取所有记账标签
     * @param tags 标签列表
     * @return 记账标签列表
     */
    fun extractAccountingTags(tags: List<String>): List<AccountingTag> {
        return tags.mapNotNull { parseAccountingTag(it) }
    }
    
    /**
     * 检查标签是否为记账标签
     * @param tag 标签字符串
     * @return 是否为记账标签
     */
    fun isAccountingTag(tag: String): Boolean {
        return ACCOUNTING_TAG_REGEX.matches(tag.trim())
    }
    
    /**
     * 创建记账标签字符串
     * @param type 类型
     * @param amount 金额
     * @return 格式化的记账标签
     */
    fun createAccountingTag(type: String, amount: BigDecimal): String {
        return "记账_${type}_${amount.stripTrailingZeros().toPlainString()}"
    }
    
    /**
     * 验证记账标签格式
     * @param tag 标签字符串
     * @return 验证结果和错误信息
     */
    fun validateAccountingTag(tag: String): Pair<Boolean, String?> {
        if (tag.isBlank()) {
            return false to "标签不能为空"
        }
        
        if (!tag.startsWith("记账_")) {
            return false to "记账标签必须以'记账_'开头"
        }
        
        val parts = tag.split("_")
        if (parts.size != 3) {
            return false to "记账标签格式错误，应为：记账_类型_金额"
        }
        
        val type = parts[1]
        if (type.isBlank()) {
            return false to "记账类型不能为空"
        }
        
        val amountStr = parts[2]
        if (amountStr.isBlank()) {
            return false to "金额不能为空"
        }
        
        try {
            val amount = BigDecimal(amountStr)
            if (amount < BigDecimal.ZERO) {
                return false to "金额不能为负数"
            }
        } catch (e: NumberFormatException) {
            return false to "金额格式错误"
        }
        
        return true to null
    }
    
    /**
     * 获取常用的记账类型
     */
    fun getCommonAccountingTypes(): List<String> {
        return listOf("支出", "收入", "转账", "借款", "还款")
    }
}