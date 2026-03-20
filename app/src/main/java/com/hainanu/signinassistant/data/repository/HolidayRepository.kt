package com.hainanu.signinassistant.data.repository

import android.content.Context
import com.hainanu.signinassistant.data.local.dao.HolidayDao
import com.hainanu.signinassistant.domain.model.HolidayRule
import com.hainanu.signinassistant.domain.model.HolidayType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HolidayRepository @Inject constructor(
    private val holidayDao: HolidayDao,
    @ApplicationContext private val context: Context,
) {

    fun observeForTerm(termId: String): Flow<List<HolidayRule>> =
        holidayDao.observeForTerm(termId).map { items -> items.map { it.toDomain() } }

    suspend fun getForTerm(termId: String): List<HolidayRule> =
        holidayDao.getForTerm(termId).map { it.toDomain() }

    suspend fun seedIfKnown(termId: String): Boolean {
        val assetName = "holidays/$termId.json"
        return try {
            val json = context.assets.open(assetName).bufferedReader().use { it.readText() }
            val array = JSONArray(json)
            val holidays = buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        HolidayRule(
                            date = java.time.LocalDate.parse(item.getString("date")),
                            type = HolidayType.valueOf(item.getString("type")),
                            makeupWeekday = item.optInt("makeupWeekday").takeIf { it > 0 },
                            termId = termId,
                            title = item.optString("title").takeIf(String::isNotBlank),
                        ),
                    )
                }
            }
            holidayDao.clearForTerm(termId)
            holidayDao.insertAll(holidays.map { it.toEntity() })
            true
        } catch (_: Exception) {
            false
        }
    }
}
