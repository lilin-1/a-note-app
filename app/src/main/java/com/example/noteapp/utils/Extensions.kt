package com.example.noteapp.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

/**
 * 通用扩展函数
 * 提供常用的扩展方法，提高代码复用性
 */

/**
 * Date扩展函数
 */
fun Date.formatWith(formatter: SimpleDateFormat): String = formatter.format(this)

fun Date.isToday(): Boolean {
    val today = Calendar.getInstance()
    val dateCalendar = Calendar.getInstance().apply { time = this@isToday }
    return today.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)
}

fun Date.isSameDay(other: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = this@isSameDay }
    val cal2 = Calendar.getInstance().apply { time = other }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

/**
 * String扩展函数
 */
fun String.isNotBlankOrEmpty(): Boolean = this.isNotBlank() && this.isNotEmpty()

fun String.truncate(maxLength: Int, suffix: String = "..."): String {
    return if (this.length <= maxLength) {
        this
    } else {
        this.take(maxLength - suffix.length) + suffix
    }
}

/**
 * List扩展函数
 */
fun <T> List<T>.takeIfNotEmpty(count: Int): List<T> {
    return if (this.isNotEmpty()) this.take(count) else emptyList()
}

fun <T> List<T>.chunkedSafely(size: Int): List<List<T>> {
    return if (this.isEmpty()) emptyList() else this.chunked(size)
}

/**
 * BigDecimal扩展函数
 */
fun BigDecimal.isPositive(): Boolean = this > BigDecimal.ZERO
fun BigDecimal.isNegative(): Boolean = this < BigDecimal.ZERO
fun BigDecimal.isZero(): Boolean = this.compareTo(BigDecimal.ZERO) == 0

fun BigDecimal.formatCurrency(): String {
    return "¥${this.stripTrailingZeros().toPlainString()}"
}

/**
 * Collection扩展函数
 */
fun <T> Collection<T>.isNotEmpty(): Boolean = this.isNotEmpty()

fun <T> Collection<T>.ifEmpty(defaultValue: () -> Collection<T>): Collection<T> {
    return if (this.isEmpty()) defaultValue() else this
}

/**
 * Composable扩展函数
 */
@Composable
fun rememberDateFormatter(pattern: String): SimpleDateFormat {
    return remember { SimpleDateFormat(pattern, Locale.getDefault()) }
}

/**
 * 安全操作扩展
 */
fun <T, R> T?.letNotNull(block: (T) -> R): R? {
    return if (this != null) block(this) else null
}

fun <T> T?.orDefault(defaultValue: T): T {
    return this ?: defaultValue
}

/**
 * 条件执行扩展
 */
fun <T> T.applyIf(condition: Boolean, block: T.() -> T): T {
    return if (condition) block() else this
}

fun <T> T.applyIfNotNull(value: Any?, block: T.() -> T): T {
    return if (value != null) block() else this
}

/**
 * 数字格式化扩展
 */
fun Int.formatWithCommas(): String {
    return String.format(Locale.getDefault(), "%,d", this)
}

fun Double.formatWithCommas(decimalPlaces: Int = 2): String {
    return String.format(Locale.getDefault(), "%,.${decimalPlaces}f", this)
}

/**
 * 时间计算扩展
 */
fun Date.addDays(days: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.DAY_OF_MONTH, days)
    return calendar.time
}

fun Date.addMonths(months: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.MONTH, months)
    return calendar.time
}

fun Date.startOfDay(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

fun Date.endOfDay(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.time
}

/**
 * 验证扩展
 */
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPhoneNumber(): Boolean {
    return android.util.Patterns.PHONE.matcher(this).matches()
}

/**
 * 集合操作扩展
 */
fun <T> List<T>.second(): T? = if (size >= 2) this[1] else null
fun <T> List<T>.third(): T? = if (size >= 3) this[2] else null

fun <T> List<T>.penultimate(): T? = if (size >= 2) this[size - 2] else null

/**
 * 字符串处理扩展
 */
fun String.capitalizeWords(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase() else it.toString() 
        }
    }
}

fun String.removeExtraSpaces(): String {
    return this.trim().replace(Regex("\\s+"), " ")
}

/**
 * 数学计算扩展
 */
fun BigDecimal.percentage(total: BigDecimal): BigDecimal {
    return if (total.isZero()) BigDecimal.ZERO
    else this.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal(100))
}

fun BigDecimal.percentageString(total: BigDecimal): String {
    val percentage = this.percentage(total)
    return "${percentage.setScale(1, RoundingMode.HALF_UP)}%"
}