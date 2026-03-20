package com.hainanu.signinassistant.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hainanu.signinassistant.ui.navigation.AppRoute
import com.hainanu.signinassistant.ui.screens.CourseDetailScreen
import com.hainanu.signinassistant.ui.screens.DebugScreen
import com.hainanu.signinassistant.ui.screens.HomeScreen
import com.hainanu.signinassistant.ui.screens.ImportScreen
import com.hainanu.signinassistant.ui.screens.SettingsScreen
import com.hainanu.signinassistant.ui.screens.SplashRoute
import com.hainanu.signinassistant.ui.screens.WeekScheduleScreen
import com.hainanu.signinassistant.ui.viewmodel.RootViewModel

@Composable
fun HainanuSignInAssistantApp() {
    val navController = rememberNavController()
    val rootViewModel: RootViewModel = hiltViewModel()
    val hasImported by rootViewModel.hasImported.collectAsStateWithLifecycle()
    val settings by rootViewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    var showPermissionSettingsDialog by rememberSaveable { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        rootViewModel.markNotificationPermissionPrompted()
        if (!granted) {
            showPermissionSettingsDialog = true
        }
    }

    LaunchedEffect(hasImported, settings.notificationPermissionPrompted) {
        val shouldPrompt = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            hasImported &&
            !settings.notificationPermissionPrompted &&
            !notificationsAvailable(context)

        if (shouldPrompt) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (showPermissionSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionSettingsDialog = false },
            title = { Text("需要通知权限") },
            text = { Text("未开启通知权限时，课程提醒不会显示。你可以稍后在系统设置中手动开启。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionSettingsDialog = false
                        openAppSettings(activity)
                    },
                ) {
                    Text("去设置")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionSettingsDialog = false }) {
                    Text("稍后再说")
                }
            },
        )
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route,
    ) {
        composable(AppRoute.Splash.route) {
            SplashRoute()
            LaunchedEffect(hasImported) {
                navController.navigate(
                    if (hasImported) AppRoute.Home.route else AppRoute.Import.route,
                ) {
                    popUpTo(AppRoute.Splash.route) { inclusive = true }
                }
            }
        }
        composable(AppRoute.Import.route) {
            ImportScreen(
                onImported = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Import.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(AppRoute.Home.route) {
            HomeScreen(
                onImport = { navController.navigate(AppRoute.Import.route) },
                onOpenWeek = { navController.navigate(AppRoute.Week.route) },
                onOpenSettings = { navController.navigate(AppRoute.Settings.route) },
                onOpenDebug = { navController.navigate(AppRoute.Debug.route) },
                onOpenCourse = { courseId -> navController.navigate(AppRoute.CourseDetail.create(courseId)) },
            )
        }
        composable(AppRoute.Week.route) {
            WeekScheduleScreen(
                onBack = { navController.popBackStack() },
                onCourseClick = { courseId -> navController.navigate(AppRoute.CourseDetail.create(courseId)) },
            )
        }
        composable(AppRoute.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoute.Debug.route) {
            DebugScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = AppRoute.CourseDetail.route,
            arguments = listOf(navArgument("courseId") { type = NavType.LongType }),
        ) {
            CourseDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}

private fun notificationsAvailable(context: android.content.Context): Boolean {
    val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    if (!enabled) return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

private fun openAppSettings(activity: Activity?) {
    activity ?: return
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", activity.packageName, null),
    )
    activity.startActivity(intent)
}
