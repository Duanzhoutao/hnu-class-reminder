package com.hainanu.signinassistant.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hainanu.signinassistant.domain.model.ReminderTier
import com.hainanu.signinassistant.ui.components.AppCard
import com.hainanu.signinassistant.ui.components.AppScreen
import com.hainanu.signinassistant.ui.components.AppSectionList
import com.hainanu.signinassistant.ui.components.DetailRow
import com.hainanu.signinassistant.ui.components.SectionHeader
import com.hainanu.signinassistant.ui.components.reminderTierLabel
import com.hainanu.signinassistant.ui.viewmodel.SettingsViewModel
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showPermissionSettingsDialog by rememberSaveable { mutableStateOf(false) }
    val notificationEnabled = notificationsAvailable(context)
    val exactEnabled = canScheduleExactAlarm(context)
    val batteryOptimizationIgnored = isIgnoringBatteryOptimizations(context)

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.markNotificationPermissionPrompted()
        when {
            granted -> scope.launch { snackbarHostState.showSnackbar("通知权限已开启，提醒可以正常显示。") }
            shouldOpenSystemSettings(context) -> showPermissionSettingsDialog = true
            else -> scope.launch { snackbarHostState.showSnackbar("通知权限未开启，提醒将不会显示。") }
        }
    }

    if (showPermissionSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionSettingsDialog = false },
            title = { Text("需要通知权限") },
            text = { Text("如果之前选择了“不再询问”，需要前往系统设置手动开启通知权限。") },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionSettingsDialog = false
                        openAppSettings(context)
                    },
                ) {
                    Text("去系统设置")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPermissionSettingsDialog = false }) {
                    Text("稍后再说")
                }
            },
        )
    }

    AppScreen(
        title = "提醒设置",
        onBack = onBack,
        snackbarHostState = snackbarHostState,
    ) { padding ->
        AppSectionList(modifier = Modifier.padding(padding)) {
            item {
                AppCard {
                    SectionHeader(
                        title = "学期时间",
                        subtitle = "第一周周一会直接影响首页、本周课表和本地提醒调度。",
                    )
                    DetailRow(
                        label = "第一周周一",
                        value = uiState.firstWeekMonday?.toString() ?: "还没有设置",
                    )
                    OutlinedButton(
                        onClick = {
                            val now = uiState.firstWeekMonday ?: LocalDate.now()
                            DatePickerDialog(
                                context,
                                { _: DatePicker, year: Int, month: Int, day: Int ->
                                    viewModel.updateFirstWeekMonday(LocalDate.of(year, month + 1, day))
                                },
                                now.year,
                                now.monthValue - 1,
                                now.dayOfMonth,
                            ).show()
                        },
                    ) {
                        Text("选择日期")
                    }
                }
            }

            item {
                AppCard {
                    SectionHeader(
                        title = "提醒类型",
                        subtitle = "保持结构轻量，只保留必要说明。",
                    )
                    SettingSwitchRow(
                        label = "课前提醒",
                        hint = "默认课前 20 分钟。",
                        checked = uiState.preClassReminderEnabled,
                        onCheckedChange = { viewModel.updateReminderSwitches(pre = it) },
                    )
                    SettingSwitchRow(
                        label = "上课提醒",
                        hint = "适合签到较严格的课程。",
                        checked = uiState.onClassReminderEnabled,
                        onCheckedChange = { viewModel.updateReminderSwitches(on = it) },
                    )
                    SettingSwitchRow(
                        label = "课后提醒",
                        hint = "适合补一层迟到提醒。",
                        checked = uiState.postClassReminderEnabled,
                        onCheckedChange = { viewModel.updateReminderSwitches(post = it) },
                    )
                }
            }

            item {
                AppCard {
                    SectionHeader(
                        title = "提醒方式",
                        subtitle = "统一三档提醒强度，减少分散设置。",
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReminderTier.entries.forEach { tier ->
                            FilterChip(
                                selected = uiState.reminderTier == tier,
                                onClick = { viewModel.updateTier(tier) },
                                label = { Text(reminderTierLabel(tier)) },
                            )
                        }
                    }
                    SettingSwitchRow(
                        label = "允许震动",
                        hint = "普通档会自动忽略震动。",
                        checked = uiState.vibrationEnabled,
                        onCheckedChange = { viewModel.updateSound(vibrationEnabled = it) },
                    )
                    SettingSwitchRow(
                        label = "启用铃声",
                        hint = "标准 / 强提醒可配合系统通知音。",
                        checked = uiState.soundEnabled,
                        onCheckedChange = { viewModel.updateSound(soundEnabled = it) },
                    )
                }
            }

            item {
                AppCard {
                    SectionHeader(
                        title = "权限状态",
                        subtitle = "这里集中展示提醒可靠性相关权限。",
                    )
                    DetailRow("通知权限", if (notificationEnabled) "已开启" else "未开启")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationEnabled) {
                        OutlinedButton(
                            onClick = {
                                viewModel.markNotificationPermissionPrompted()
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                        ) {
                            Text("申请通知权限")
                        }
                    }
                    DetailRow("精确闹钟", if (exactEnabled) "已可用" else "未授权，提醒可能延迟")
                    if (!exactEnabled) {
                        Text(
                            text = "未开启精确闹钟时，提醒可能延迟几分钟，但 App 仍会继续工作。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        OutlinedButton(
                            onClick = { context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)) },
                        ) {
                            Text("前往精确闹钟设置")
                        }
                    }
                }
            }

            item {
                AppCard {
                    SectionHeader(
                        title = "省电优化",
                        subtitle = "某些厂商会延迟后台维护任务，可以按需放宽。",
                    )
                    DetailRow("当前状态", if (batteryOptimizationIgnored) "已放宽" else "可选优化")
                    Text(
                        text = if (batteryOptimizationIgnored) {
                            "当前设备较少对 App 进行省电限制，提醒维护任务会更稳定。"
                        } else {
                            "如果你发现提醒维护任务偶尔延迟，可以在系统电池设置中将本 App 设为不限制。"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!batteryOptimizationIgnored) {
                        OutlinedButton(onClick = { openBatteryOptimizationSettings(context) }) {
                            Text("查看电池优化")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    label: String,
    hint: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun notificationsAvailable(context: Context): Boolean {
    if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

private fun shouldOpenSystemSettings(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val activity = context as? ComponentActivity ?: return false
        !activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        false
    }
}

private fun canScheduleExactAlarm(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }
}

private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

private fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        android.net.Uri.fromParts("package", context.packageName, null),
    )
    context.startActivity(intent)
}

private fun openBatteryOptimizationSettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
}
