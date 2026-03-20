package com.hainanu.signinassistant.ui.components

import com.hainanu.signinassistant.domain.model.ReminderTier
import com.hainanu.signinassistant.domain.model.ReminderType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val monthDayFormatter = DateTimeFormatter.ofPattern("M月d日")
private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun formatDateLabel(date: LocalDate): String = "${dateFormatter.format(date)} ${weekdayLabel(date.dayOfWeek.value)}"

fun weekdayLabel(weekday: Int): String = when (weekday) {
    1 -> "周一"
    2 -> "周二"
    3 -> "周三"
    4 -> "周四"
    5 -> "周五"
    6 -> "周六"
    7 -> "周日"
    else -> "未知"
}

fun weekdayShortLabel(weekday: Int): String = when (weekday) {
    1 -> "一"
    2 -> "二"
    3 -> "三"
    4 -> "四"
    5 -> "五"
    6 -> "六"
    7 -> "日"
    else -> "-"
}

fun formatCompactDate(date: LocalDate): String = date.format(monthDayFormatter)

fun compressWeeks(weeks: Set<Int>): String {
    if (weeks.isEmpty()) return "未设置"
    val sorted = weeks.sorted()
    val ranges = mutableListOf<String>()
    var start = sorted.first()
    var previous = start
    for (index in 1 until sorted.size) {
        val current = sorted[index]
        if (current == previous + 1) {
            previous = current
        } else {
            ranges += if (start == previous) "$start" else "$start-$previous"
            start = current
            previous = current
        }
    }
    ranges += if (start == previous) "$start" else "$start-$previous"
    return ranges.joinToString(", ") + "周"
}

fun reminderTierLabel(tier: ReminderTier): String = when (tier) {
    ReminderTier.NORMAL -> "A 普通提醒"
    ReminderTier.STANDARD -> "B 标准提醒"
    ReminderTier.STRONG -> "C 强提醒"
}

fun reminderTypeLabel(type: ReminderType): String = when (type) {
    ReminderType.PRE_CLASS -> "课前提醒"
    ReminderType.ON_CLASS -> "上课提醒"
    ReminderType.POST_CLASS -> "课后提醒"
}
