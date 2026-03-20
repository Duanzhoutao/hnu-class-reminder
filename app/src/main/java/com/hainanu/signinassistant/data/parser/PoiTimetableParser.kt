package com.hainanu.signinassistant.data.parser

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.hainanu.signinassistant.domain.model.ImportedCourseDraft
import com.hainanu.signinassistant.domain.model.ImportedCourseNoteDraft
import com.hainanu.signinassistant.domain.model.ImportedTimetableBundle
import com.hainanu.signinassistant.domain.model.ParseError
import com.hainanu.signinassistant.domain.model.SectionSlot
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoiTimetableParser @Inject constructor(
    @ApplicationContext private val context: Context,
) : TimetableParser {

    private val formatter = DataFormatter()

    override suspend fun parse(uri: Uri): ImportedTimetableBundle = withContext(Dispatchers.IO) {
        val sourceFileName = DocumentFile.fromSingleUri(context, uri)?.name ?: "课表文件"
        val errors = mutableListOf<ParseError>()
        val courses = mutableListOf<ImportedCourseDraft>()
        val notes = mutableListOf<ImportedCourseNoteDraft>()
        val sectionSlots = mutableListOf<SectionSlot>()

        context.contentResolver.openInputStream(uri).use { inputStream ->
            requireNotNull(inputStream) { "无法读取选中的课表文件" }
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)
            val termId = HainanuTimetableTextParser.extractTermId(
                sheet.getRow(1)?.getCell(0)?.let(::getCellText).orEmpty(),
            )

            for (rowIndex in 3..sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex) ?: continue
                val firstCellText = row.getCell(0)?.let(::getCellText).orEmpty().trim()
                val sectionSlot = HainanuTimetableTextParser.parseSectionSlot(firstCellText, termId)
                if (sectionSlot != null) {
                    sectionSlots += sectionSlot
                    for (colIndex in 1..7) {
                        val rawText = row.getCell(colIndex)?.let(::getCellText).orEmpty()
                        if (rawText.isBlank()) continue
                        val result = HainanuTimetableTextParser.parseCourseCell(
                            rawText = rawText,
                            weekday = colIndex,
                            fallbackStartSection = sectionSlot.startSection,
                            fallbackEndSection = sectionSlot.endSection,
                        )
                        courses += result.courses
                        errors += result.errors.map {
                            it.copy(rowIndex = rowIndex, colIndex = colIndex, termId = termId)
                        }
                    }
                } else {
                    val rowNotes = parseNoteRow(row)
                    notes += rowNotes
                }
            }

            workbook.close()

            ImportedTimetableBundle(
                courses = courses,
                sectionSlots = sectionSlots.distinctBy { "${it.startSection}-${it.endSection}" },
                notes = notes,
                parseErrors = errors,
                termId = termId,
                sourceFileName = sourceFileName,
                sourceUri = uri,
            )
        }
    }

    private fun parseNoteRow(row: Row): List<ImportedCourseNoteDraft> {
        val raw = buildString {
            for (col in 1..7) {
                val text = row.getCell(col)?.let(::getCellText).orEmpty().trim()
                if (text.isNotBlank()) append(text)
            }
        }
        return HainanuTimetableTextParser.parseNoteText(raw)
    }

    private fun getCellText(cell: org.apache.poi.ss.usermodel.Cell): String {
        return if (cell.cellType == CellType.NUMERIC) {
            formatter.formatCellValue(cell)
        } else {
            formatter.formatCellValue(cell).trim()
        }
    }
}
