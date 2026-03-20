package com.hainanu.signinassistant.domain.usecase

import android.net.Uri
import com.hainanu.signinassistant.alarm.ReminderScheduler
import com.hainanu.signinassistant.data.parser.TimetableParser
import com.hainanu.signinassistant.data.repository.HolidayRepository
import com.hainanu.signinassistant.data.repository.TimetableRepository
import com.hainanu.signinassistant.data.settings.SettingsRepository
import com.hainanu.signinassistant.domain.model.ImportedTimetableBundle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportTimetableUseCase @Inject constructor(
    private val timetableParser: TimetableParser,
    private val timetableRepository: TimetableRepository,
    private val holidayRepository: HolidayRepository,
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler,
) {

    suspend fun preview(uri: Uri): ImportedTimetableBundle = timetableParser.parse(uri)

    suspend fun confirmImport(bundle: ImportedTimetableBundle) {
        timetableRepository.replaceImport(bundle)
        holidayRepository.seedIfKnown(bundle.termId)
        settingsRepository.alignFirstWeekMondayForTerm(bundle.termId)
        reminderScheduler.rescheduleHorizon(reason = "import_success")
    }
}
