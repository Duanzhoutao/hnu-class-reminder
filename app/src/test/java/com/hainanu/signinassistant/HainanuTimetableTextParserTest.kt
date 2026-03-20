package com.hainanu.signinassistant

import com.google.common.truth.Truth.assertThat
import com.hainanu.signinassistant.data.parser.HainanuTimetableTextParser
import org.junit.Test

class HainanuTimetableTextParserTest {

    @Test
    fun parseSectionSlotFromExcelHeader() {
        val slot = HainanuTimetableTextParser.parseSectionSlot(
            text = "3、4节\n(03,04)\n09:45-11:25",
            termId = "2025-2026-2",
        )

        assertThat(slot).isNotNull()
        assertThat(slot?.startSection).isEqualTo(3)
        assertThat(slot?.endSection).isEqualTo(4)
        assertThat(slot?.startTime.toString()).isEqualTo("09:45")
    }

    @Test
    fun splitMultipleCoursesFromSingleCell() {
        val raw = """
            | 大数据可视化
            | 莫欣岳
            | 1-12(周)[03-04节]
            | (海甸)5-413
            |
            | 大数据安全与隐私保护
            | 冯霞
            | 13-16(周)[03-04节]
            | (海甸)理工楼417
        """.trimIndent()

        val result = HainanuTimetableTextParser.parseCourseCell(
            rawText = raw,
            weekday = 4,
            fallbackStartSection = 3,
            fallbackEndSection = 4,
        )

        assertThat(result.errors).isEmpty()
        assertThat(result.courses).hasSize(2)
        assertThat(result.courses.first().courseName).isEqualTo("大数据可视化")
        assertThat(result.courses.last().weeks).containsExactly(13, 14, 15, 16)
    }

    @Test
    fun parseBottomNoteRow() {
        val notes = HainanuTimetableTextParser.parseNoteText(
            "：形势与政策6  林罗添骥  12-13周   ;劳动教育实践6  黄苗苗  1-3周   ;",
        )

        assertThat(notes).hasSize(2)
        assertThat(notes.first().title).isEqualTo("形势与政策6")
        assertThat(notes.last().weeks).containsExactly(1, 2, 3)
    }
}
