package com.hainanu.signinassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hainanu.signinassistant.ui.components.AppCard
import com.hainanu.signinassistant.ui.components.AppScreen
import com.hainanu.signinassistant.ui.components.AppSectionList
import com.hainanu.signinassistant.ui.components.CourseCard
import com.hainanu.signinassistant.ui.components.CourseCardVariant
import com.hainanu.signinassistant.ui.components.EmptyState
import com.hainanu.signinassistant.ui.components.SectionHeader
import com.hainanu.signinassistant.ui.components.weekdayLabel
import com.hainanu.signinassistant.ui.viewmodel.WeekScheduleViewModel

@Composable
fun WeekScheduleScreen(
    onBack: () -> Unit,
    onCourseClick: (Long) -> Unit,
    viewModel: WeekScheduleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AppScreen(
        title = "本周课表",
        onBack = onBack,
    ) { padding ->
        AppSectionList(modifier = Modifier.padding(padding)) {
            item {
                AppCard(highlighted = true) {
                    Text(
                        text = "第 ${uiState.weekIndex ?: "-"} 周",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = "按星期分组展示当前周有效课程。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            (1..7).forEach { weekday ->
                val dayCourses = uiState.groupedCourses[weekday].orEmpty()
                item {
                    SectionHeader(
                        title = weekdayLabel(weekday),
                        subtitle = if (dayCourses.isEmpty()) "这一天没有命中课程" else "${dayCourses.size} 节课程",
                    )
                }
                if (dayCourses.isEmpty()) {
                    item {
                        EmptyState(
                            title = "${weekdayLabel(weekday)}无课",
                            description = "当前周在这一天没有符合周次规则的课程。",
                        )
                    }
                } else {
                    items(dayCourses) { course ->
                        CourseCard(
                            course = course,
                            sectionSlot = uiState.sectionSlots.firstOrNull {
                                it.startSection == course.startSection && it.endSection == course.endSection
                            },
                            variant = CourseCardVariant.WEEK,
                            subtitle = "周次 ${course.weeks.sorted().first()} 起，按本周状态展示",
                            onClick = { onCourseClick(course.id) },
                        )
                    }
                }
            }
        }
    }
}
