package com.hainanu.signinassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hainanu.signinassistant.ui.components.AppCard
import com.hainanu.signinassistant.ui.components.AppScreen
import com.hainanu.signinassistant.ui.components.AppSectionList
import com.hainanu.signinassistant.ui.components.EmptyState
import com.hainanu.signinassistant.ui.components.SectionHeader
import com.hainanu.signinassistant.ui.components.reminderTierLabel
import com.hainanu.signinassistant.ui.components.reminderTypeLabel
import com.hainanu.signinassistant.ui.viewmodel.DebugViewModel

@Composable
fun DebugScreen(
    onBack: () -> Unit,
    viewModel: DebugViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AppScreen(
        title = "调试页",
        onBack = onBack,
    ) { padding ->
        AppSectionList(modifier = Modifier.padding(padding)) {
            item {
                SectionHeader(
                    title = "未来 14 天已调度提醒",
                    subtitle = "核对提醒时间、类型和档位是否符合预期。",
                )
            }
            if (uiState.scheduledReminders.isEmpty()) {
                item {
                    EmptyState(
                        title = "还没有已调度提醒",
                        description = "通常是刚安装、课表为空，或者当前课程都被规则过滤掉了。",
                    )
                }
            } else {
                items(uiState.scheduledReminders.take(30)) { item ->
                    AppCard {
                        Text(item.courseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${item.date} ${item.triggerAt.toLocalTime()} · ${reminderTypeLabel(item.reminderType)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            reminderTierLabel(item.reminderTier),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            item {
                SectionHeader(
                    title = "最近通知记录",
                    subtitle = "用于确认广播和通知是否真正触发。",
                )
            }
            if (uiState.notificationLogs.isEmpty()) {
                item {
                    EmptyState(
                        title = "还没有通知记录",
                        description = "触发一条提醒后，这里会开始出现最近发送记录。",
                    )
                }
            } else {
                items(uiState.notificationLogs) { log ->
                    AppCard {
                        Text(log.courseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${log.triggeredAt} · ${reminderTypeLabel(log.reminderType)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            reminderTierLabel(log.reminderTier),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            item {
                SectionHeader(
                    title = "最近解析错误",
                    subtitle = "这里保留导入时未能正常解析的单元格。",
                )
            }
            if (uiState.parseErrors.isEmpty()) {
                item {
                    EmptyState(
                        title = "没有解析错误",
                        description = "当前课表导入结果看起来比较完整。",
                    )
                }
            } else {
                items(uiState.parseErrors) { error ->
                    AppCard {
                        Text(
                            "第 ${error.rowIndex} 行，第 ${error.colIndex} 列",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            error.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            error.rawText,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
