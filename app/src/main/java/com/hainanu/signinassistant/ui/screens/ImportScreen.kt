package com.hainanu.signinassistant.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hainanu.signinassistant.domain.model.ImportedCourseDraft
import com.hainanu.signinassistant.ui.components.AppCard
import com.hainanu.signinassistant.ui.components.AppScreen
import com.hainanu.signinassistant.ui.components.AppSectionList
import com.hainanu.signinassistant.ui.components.EmptyState
import com.hainanu.signinassistant.ui.components.InfoChip
import com.hainanu.signinassistant.ui.components.SectionHeader
import com.hainanu.signinassistant.ui.viewmodel.ImportViewModel

private const val JXGL_URL = "https://jxgl.hainanu.edu.cn/"

@Composable
fun ImportScreen(
    onImported: () -> Unit,
    onBack: () -> Unit,
    viewModel: ImportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
        viewModel.previewImport(uri)
    }

    LaunchedEffect(uiState.importCompleted) {
        if (uiState.importCompleted) onImported()
    }

    AppScreen(
        title = "导入海大课表",
        onBack = onBack,
    ) { padding ->
        AppSectionList(modifier = Modifier.padding(padding)) {
            item {
                AppCard {
                    Icon(
                        imageVector = Icons.Outlined.FileUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    SectionHeader(
                        title = "从 Excel 一键导入",
                        subtitle = "支持 `.xls` / `.xlsx`，重新导入会覆盖旧课表并重建提醒。",
                    )
                    Button(onClick = { openDocumentLauncher.launch(arrayOf("*/*")) }) {
                        Text("选择课表文件")
                    }
                    uiState.errorMessage?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                    }
                }
            }

            item {
                AppCard {
                    SectionHeader(
                        title = "导出教程",
                        subtitle = "先从海大教务系统导出课表，再回到 App 导入。",
                    )
                    TutorialStep("第一步：登录 https://jxgl.hainanu.edu.cn/")
                    TutorialStep("第二步：右边选择栏，培养管理 -> 学期课表")
                    TutorialStep("第三步：点击导出即可")
                    TutorialStep("第四步：回到 App，放入即可")
                    TextButton(onClick = { uriHandler.openUri(JXGL_URL) }) {
                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
                        Text(" 打开教务系统")
                    }
                }
            }

            uiState.previewBundle?.let { bundle ->
                item {
                    AppCard(highlighted = true) {
                        SectionHeader(
                            title = "解析摘要",
                            subtitle = bundle.sourceFileName,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoChip(text = "学期 ${bundle.termId}", accent = true)
                            InfoChip(text = "成功课程 ${bundle.courses.size}")
                            InfoChip(text = "备注课程 ${bundle.notes.size}")
                            InfoChip(text = "解析失败 ${bundle.parseErrors.size}")
                        }
                        Button(onClick = viewModel::confirmImport) {
                            Text("确认导入并覆盖旧数据")
                        }
                    }
                }

                item {
                    SectionHeader(
                        title = "课程预览",
                        subtitle = "先确认几门典型课程是否解析正确。",
                    )
                }

                items(bundle.courses.take(6)) { course ->
                    ImportPreviewCard(course = course)
                }

                if (bundle.notes.isNotEmpty()) {
                    item {
                        AppCard {
                            SectionHeader(
                                title = "备注课程",
                                subtitle = "这些课程只展示，不参与今日课表和提醒。",
                            )
                            bundle.notes.take(5).forEach { note ->
                                Text(
                                    text = "• ${note.title} · ${note.teacherName ?: "待确认"} · ${note.weeks.sorted().joinToString(",")}周",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                if (bundle.parseErrors.isNotEmpty()) {
                    item {
                        AppCard {
                            SectionHeader(
                                title = "解析异常",
                                subtitle = "不会阻止导入，但建议检查这些单元格。",
                            )
                            bundle.parseErrors.take(4).forEach { error ->
                                Text(
                                    text = "第 ${error.rowIndex} 行，第 ${error.colIndex} 列：${error.errorMessage}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            } ?: item {
                EmptyState(
                    title = "还没有解析结果",
                    description = "先选择一份海大课表文件，系统会自动展示学期、课程数和预览内容。",
                    action = {
                        OutlinedButton(onClick = { openDocumentLauncher.launch(arrayOf("*/*")) }) {
                            Icon(Icons.Outlined.Description, contentDescription = null)
                            Text(" 选择文件")
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun TutorialStep(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ImportPreviewCard(course: ImportedCourseDraft) {
    AppCard {
        Text(course.courseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            text = "星期 ${course.weekday} · 第 ${course.startSection}-${course.endSection} 节",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "教师：${course.teacherName ?: "待确认"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "地点：${course.location ?: "待确认"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
