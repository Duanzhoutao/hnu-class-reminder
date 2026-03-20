package com.hainanu.signinassistant.ui.navigation

sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object Import : AppRoute("import")
    data object Home : AppRoute("home")
    data object Week : AppRoute("week")
    data object Settings : AppRoute("settings")
    data object Debug : AppRoute("debug")
    data object CourseDetail : AppRoute("course/{courseId}") {
        fun create(courseId: Long): String = "course/$courseId"
    }
}
