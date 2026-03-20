package com.hainanu.signinassistant.data.repository

import com.hainanu.signinassistant.data.local.dao.CourseDao
import com.hainanu.signinassistant.data.local.dao.CourseNoteDao
import com.hainanu.signinassistant.data.local.dao.ParseErrorDao
import com.hainanu.signinassistant.data.local.dao.SectionSlotDao
import com.hainanu.signinassistant.domain.model.Course
import com.hainanu.signinassistant.domain.model.CourseNote
import com.hainanu.signinassistant.domain.model.ImportedTimetableBundle
import com.hainanu.signinassistant.domain.model.ParseError
import com.hainanu.signinassistant.domain.model.SectionSlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimetableRepository @Inject constructor(
    private val courseDao: CourseDao,
    private val sectionSlotDao: SectionSlotDao,
    private val parseErrorDao: ParseErrorDao,
    private val courseNoteDao: CourseNoteDao,
) {

    val coursesFlow: Flow<List<Course>> = courseDao.observeAll().map { items -> items.map { it.toDomain() } }
    val sectionSlotsFlow: Flow<List<SectionSlot>> = sectionSlotDao.observeAll().map { items -> items.map { it.toDomain() } }
    val notesFlow: Flow<List<CourseNote>> = courseNoteDao.observeAll().map { items -> items.map { it.toDomain() } }
    val parseErrorsFlow: Flow<List<ParseError>> = parseErrorDao.observeAll().map { items -> items.map { it.toDomain() } }
    val hasImportedTimetableFlow: Flow<Boolean> = courseDao.observeCount().map { it > 0 }
    val snapshotFlow: Flow<TimetableSnapshot> = combine(
        coursesFlow,
        sectionSlotsFlow,
        notesFlow,
        parseErrorsFlow,
    ) { courses, sectionSlots, notes, parseErrors ->
        TimetableSnapshot(courses, sectionSlots, notes, parseErrors)
    }

    suspend fun replaceImport(bundle: ImportedTimetableBundle) {
        courseDao.clear()
        sectionSlotDao.clear()
        parseErrorDao.clear()
        courseNoteDao.clear()

        courseDao.insertAll(bundle.courses.map { it.toEntity(bundle.termId) })
        sectionSlotDao.insertAll(bundle.sectionSlots.map { it.toEntity() })
        parseErrorDao.insertAll(bundle.parseErrors.map { it.toEntity() })
        courseNoteDao.insertAll(bundle.notes.map { it.toEntity(bundle.termId) })
    }

    suspend fun getCourse(courseId: Long): Course? = courseDao.getById(courseId)?.toDomain()

    fun observeCourse(courseId: Long): Flow<Course?> = courseDao.observeById(courseId).map { it?.toDomain() }

    suspend fun getCourses(): List<Course> = courseDao.getAll().map { it.toDomain() }

    suspend fun getSectionSlots(): List<SectionSlot> = sectionSlotDao.getAll().map { it.toDomain() }

    suspend fun updateCourseReminderEnabled(courseId: Long, enabled: Boolean) {
        courseDao.updateReminderEnabled(courseId, enabled)
    }

    suspend fun hasImportedTimetable(): Boolean = courseDao.count() > 0
}

data class TimetableSnapshot(
    val courses: List<Course>,
    val sectionSlots: List<SectionSlot>,
    val notes: List<CourseNote>,
    val parseErrors: List<ParseError>,
)
