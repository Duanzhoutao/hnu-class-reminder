package com.hainanu.signinassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.hainanu.signinassistant.ui.components.CourseCard
import com.hainanu.signinassistant.ui.components.CourseCardVariant
import com.hainanu.signinassistant.ui.components.EmptyState
import com.hainanu.signinassistant.ui.components.InfoChip
import com.hainanu.signinassistant.ui.components.SectionHeader
import com.hainanu.signinassistant.ui.components.TopAction
import com.hainanu.signinassistant.ui.components.formatDateLabel
import com.hainanu.signinassistant.ui.viewmodel.HomeViewModel
import com.hainanu.signinassistant.ui.theme.AppSpacing

@Composable
fun HomeScreen(
    onImport: () -> Unit,
    onOpenWeek: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDebug: () -> Unit,
    onOpenCourse: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AppScreen(
        title = "首页",
        actions = listOf(
            TopAction(Icons.Outlined.Settings, "设置", onOpenSettings),
            TopAction(Icons.Outlined.BugReport, "调试", onOpenDebug),
        ),
    ) { padding ->
        AppSectionList(modifier = Modifier.padding(padding)) {
            item {
                AppCard(highlighted = true) {
                    Text(
                        text = "第 ${uiState.weekIndex ?: "-"} 周",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = formatDateLabel(uiState.today),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        InfoChip(text = "今日 ${uiState.todayCourses.size} 节课", accent = true)
                        InfoChip(text = if (uiState.dailyMuted) "今日免打扰已开启" else "提醒正常")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setDailyMute(!uiState.dailyMuted) },
                        ) {
                            Text(if (uiState.dailyMuted) "关闭免打扰" else "开启免打扰")
                        }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = onOpenWeek,
                        ) {
                            Text("查看本周课表")
                        }
                    }
                    if (!uiState.holidayConfigured) {
                        Text(
                            text = "当前学期还没有内置校历，节假日不会自动跳过。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                SectionHeader(
                    title = "快捷入口",
                    subtitle = "保留关键操作，减少首页噪音。",
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    QuickLinkCard(
                        modifier = Modifier.weight(1f),
                        title = "重导入课表",
                        value = "更新本学期数据",
                        icon = Icons.Outlined.CloudUpload,
                        onClick = onImport,
                    )
                    QuickLinkCard(
                        modifier = Modifier.weight(1f),
                        title = "备注课程",
                        value = "${uiState.notesCount} 条",
                        icon = Icons.Outlined.CalendarMonth,
                        onClick = onOpenDebug,
                    )
                }
            }

            item {
                SectionHeader(
                    title = "今日课程",
                    subtitle = if (uiState.todayCourses.isEmpty()) "今天没有命中的课程提醒。" else "按当前周次、节次与提醒状态筛选。",
                )
            }

            if (uiState.todayCourses.isEmpty()) {
                item {
                    EmptyState(
                        title = "今天没有命中的提醒课程",
                        description = "可能是休息日、已开启今日免打扰，或者今天本来就没有符合当前周次的课程。",
                        action = {
                            OutlinedButton(onClick = onOpenWeek) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Outlined.NotificationsOff,
                                    contentDescription = null,
                                    modifier = Modifier.width(18.dp),
                                )
                                Text(" 查看本周安排")
                            }
                        },
                    )
                }
            } else {
                items(uiState.todayCourses) { course ->
                    CourseCard(
                        course = course,
                        sectionSlot = uiState.sectionSlots.firstOrNull {
                            it.startSection == course.startSection && it.endSection == course.endSection
                        },
                        variant = CourseCardVariant.TODAY,
                        subtitle = "系统已按当前周次与节假日规则筛选",
                        onClick = { onOpenCourse(course.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickLinkCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier
            .heightIn(min = 132.dp)
            .padding(0.dp),
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(onClick = onClick) {
            Text("进入")
        }
    }
}
