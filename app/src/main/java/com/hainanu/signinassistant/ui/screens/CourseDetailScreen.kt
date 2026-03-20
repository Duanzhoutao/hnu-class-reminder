package com.hainanu.signinassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hainanu.signinassistant.ui.components.AppCard
import com.hainanu.signinassistant.ui.components.AppScreen
import com.hainanu.signinassistant.ui.components.AppSectionList
import com.hainanu.signinassistant.ui.components.DetailRow
import com.hainanu.signinassistant.ui.components.EmptyState
import com.hainanu.signinassistant.ui.components.InfoChip
import com.hainanu.signinassistant.ui.components.SectionHeader
import com.hainanu.signinassistant.ui.components.compressWeeks
import com.hainanu.signinassistant.ui.components.weekdayLabel
import com.hainanu.signinassistant.ui.viewmodel.CourseDetailViewModel

@Composable
fun CourseDetailScreen(
    onBack: () -> Unit,
    viewModel: CourseDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val course = uiState.course

    AppScreen(
        title = "课程详情",
        onBack = onBack,
    ) { padding ->
        AppSectionList(modifier = Modifier.padding(padding)) {
            if (course == null) {
                item {
                    EmptyState(
                        title = "没有找到这门课",
                        description = "课程可能已被重新导入或当前页面参数失效。",
                    )
                }
            } else {
                item {
                    AppCard(highlighted = true) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoChip(text = weekdayLabel(course.weekday))
                            InfoChip(text = "第${course.startSection}-${course.endSection}节", accent = true)
                        }
                        Text(
                            text = course.courseName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "这门课的提醒会跟随课程总开关统一调度。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                item {
                    AppCard {
                        SectionHeader(title = "基础信息")
                        DetailRow("教师", course.teacherName ?: "待确认")
                        DetailRow("地点", course.location ?: "待确认")
                        DetailRow(
                            "时间",
                            buildString {
                                append(uiState.sectionSlot?.startTime ?: "--:--")
                                append(" - ")
                                append(uiState.sectionSlot?.endTime ?: "--:--")
                            },
                        )
                        DetailRow("周次", compressWeeks(course.weeks))
                    }
                }
                item {
                    AppCard {
                        SectionHeader(
                            title = "提醒状态",
                            subtitle = "关闭后会停止这门课的全部本地提醒。",
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("课程提醒", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = if (course.remindersEnabled) "当前已开启" else "当前已关闭",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = course.remindersEnabled,
                                onCheckedChange = viewModel::setReminderEnabled,
                            )
                        }
                    }
                }
            }
        }
    }
}
