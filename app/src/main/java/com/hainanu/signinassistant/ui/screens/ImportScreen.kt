package com.hainanu.signinassistant.ui.screens

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hainanu.signinassistant.ui.components.AppCard
import com.hainanu.signinassistant.ui.components.AppScreen
import com.hainanu.signinassistant.ui.components.AppSectionList
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
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }.onFailure { error ->
            Log.w("ImportScreen", "Persistable permission unavailable for $uri", error)
        }
        viewModel.importFromUri(uri)
    }

    LaunchedEffect(uiState.importCompleted) {
        if (uiState.importCompleted) {
            onImported()
        }
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
                        title = "从教务课表 Excel 一键导入",
                        subtitle = "支持 `.xls` / `.xlsx`，导入后会覆盖旧课表并重建提醒。",
                    )
                    Button(
                        onClick = {
                            openDocumentLauncher.launch(
                                arrayOf(
                                    "application/vnd.ms-excel",
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    "application/octet-stream",
                                ),
                            )
                        },
                    ) {
                        Text("选择课表文件")
                    }
                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                    }
                    uiState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    uiState.importedFileName?.let { fileName ->
                        Text(
                            text = "已成功导入：$fileName",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            item {
                AppCard {
                    SectionHeader(
                        title = "导入教程",
                        subtitle = "先从海南大学教务系统导出课表，再回到 App 导入。",
                    )
                    TutorialStep("第一步：登录 https://jxgl.hainanu.edu.cn/")
                    TutorialStep("第二步：右边选择栏，培养管理——>学期课表")
                    TutorialStep("第三步：点击导出即可")
                    TutorialStep("第四步：回到APP，放入即可")
                    TextButton(onClick = { uriHandler.openUri(JXGL_URL) }) {
                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
                        Text(" 打开教务系统")
                    }
                }
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
