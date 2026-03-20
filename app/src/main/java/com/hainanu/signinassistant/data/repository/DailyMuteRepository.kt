package com.hainanu.signinassistant.data.repository

import com.hainanu.signinassistant.data.local.dao.DailyMuteDao
import com.hainanu.signinassistant.data.local.entity.DailyMuteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyMuteRepository @Inject constructor(
    private val dailyMuteDao: DailyMuteDao,
) {

    fun observe(date: LocalDate): Flow<Boolean> =
        dailyMuteDao.observeByDate(date.toString()).map { it?.enabled == true }

    suspend fun isMuted(date: LocalDate): Boolean =
        dailyMuteDao.getByDate(date.toString())?.enabled == true

    suspend fun setMuted(date: LocalDate, enabled: Boolean) {
        dailyMuteDao.upsert(DailyMuteEntity(date = date.toString(), enabled = enabled))
        dailyMuteDao.deleteBefore(LocalDate.now().toString())
    }
}
