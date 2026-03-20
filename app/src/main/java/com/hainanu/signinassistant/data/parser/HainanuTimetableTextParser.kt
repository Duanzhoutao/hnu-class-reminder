package com.hainanu.signinassistant.data.parser

import com.hainanu.signinassistant.domain.model.ImportedCourseDraft
import com.hainanu.signinassistant.domain.model.ImportedCourseNoteDraft
import com.hainanu.signinassistant.domain.model.ParseError
import com.hainanu.signinassistant.domain.model.SectionSlot
import java.time.LocalTime

internal object HainanuTimetableTextParser {

    fun extractTermId(header: String): String {
        val regex = Regex("""学年学期[:：]\s*([0-9]{4}-[0-9]{4}-[12])""")
        return regex.find(header)?.groupValues?.get(1) ?: "unknown-term"
    }

    fun parseSectionSlot(text: String, termId: String): SectionSlot? {
        if (!text.contains("节")) return null
        val timeMatch = Regex("""(\d{2}:\d{2})-(\d{2}:\d{2})""").find(text) ?: return null
        val sections = Regex("""\d+""").findAll(text.substringBefore("节")).map { it.value.toInt() }.toList()
        if (sections.isEmpty()) return null
        return SectionSlot(
            id = 0,
            termId = termId,
            label = text.lines().firstOrNull()?.trim().orEmpty().ifBlank { text },
            startSection = sections.min(),
            endSection = sections.max(),
            startTime = LocalTime.parse(timeMatch.groupValues[1]),
            endTime = LocalTime.parse(timeMatch.groupValues[2]),
        )
    }

    data class ParseResult(
        val courses: List<ImportedCourseDraft>,
        val errors: List<ParseError>,
    )

    fun parseCourseCell(
        rawText: String,
        weekday: Int,
        fallbackStartSection: Int,
        fallbackEndSection: Int,
    ): ParseResult {
        val normalizedTokens = rawText
            .replace('\u00A0', ' ')
            .split('\n', '|')
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val courses = mutableListOf<ImportedCourseDraft>()
        val errors = mutableListOf<ParseError>()
        var index = 0
        while (index < normalizedTokens.size) {
            val courseName = normalizedTokens.getOrNull(index)
            val teacher = normalizedTokens.getOrNull(index + 1)
            val weekToken = normalizedTokens.getOrNull(index + 2)
            if (courseName == null || teacher == null || weekToken == null || !weekToken.contains("周")) {
                errors += ParseError(
                    id = 0,
                    rowIndex = -1,
                    colIndex = -1,
                    rawText = rawText,
                    errorMessage = "无法按海大课表格式解析单元格片段：${normalizedTokens.drop(index).joinToString(" / ")}",
                    termId = "",
                )
                break
            }

            val locationCandidate = normalizedTokens.getOrNull(index + 3)
            val hasLocation = locationCandidate?.let {
                it.startsWith("(") || it.contains("楼") || it.contains("教室") || it.contains("理工")
            } == true
            val location = if (hasLocation) locationCandidate else null
            val sectionsMatch = Regex("""\[(.*?)[节\]]""").find(weekToken)
            val weeksSpec = weekToken.substringBefore("[")
            val parsedSections = sectionsMatch?.groupValues?.get(1)
                ?.let { Regex("""\d+""").findAll(it).map { match -> match.value.toInt() }.toList() }
                .orEmpty()
            val startSection = parsedSections.minOrNull() ?: fallbackStartSection
            val endSection = parsedSections.maxOrNull() ?: fallbackEndSection
            val weeks = WeekPatternParser.parse(weeksSpec)

            courses += ImportedCourseDraft(
                courseName = courseName,
                teacherName = teacher,
                location = location,
                weekday = weekday,
                startSection = startSection,
                endSection = endSection,
                weeks = weeks,
                rawText = rawText,
            )
            index += if (hasLocation) 4 else 3
        }
        return ParseResult(courses, errors)
    }

    fun parseNoteText(raw: String): List<ImportedCourseNoteDraft> {
        if (!raw.contains(";")) return emptyList()
        return raw.removePrefix("：")
            .split(";")
            .mapNotNull { segment ->
                val trimmed = segment.trim()
                if (trimmed.isBlank()) return@mapNotNull null
                val parts = trimmed.split(Regex("""\s+""")).filter { it.isNotBlank() }
                if (parts.size < 3) return@mapNotNull null
                val weeksToken = parts.last()
                val teacherToken = parts[parts.lastIndex - 1]
                val title = parts.dropLast(2).joinToString(" ")
                ImportedCourseNoteDraft(
                    title = title,
                    teacherName = teacherToken,
                    weeks = WeekPatternParser.parse(weeksToken),
                    rawText = trimmed,
                )
            }
    }
}
