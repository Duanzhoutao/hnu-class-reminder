package com.hainanu.signinassistant.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hainanu.signinassistant.domain.model.Course
import com.hainanu.signinassistant.domain.model.SectionSlot

enum class CourseCardVariant {
    TODAY,
    WEEK,
    PREVIEW,
}

@Composable
fun CourseCard(
    course: Course,
    sectionSlot: SectionSlot?,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    variant: CourseCardVariant = CourseCardVariant.TODAY,
    onClick: (() -> Unit)? = null,
) {
    AppCard(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier,
        ),
        highlighted = variant == CourseCardVariant.TODAY,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoChip(text = weekdayLabel(course.weekday))
            InfoChip(text = "第${course.startSection}-${course.endSection}节", accent = true)
        }
        Text(
            text = course.courseName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "${sectionSlot?.startTime ?: "--:--"} - ${sectionSlot?.endTime ?: "--:--"}",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "地点：${course.location ?: "待确认"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "教师：${course.teacherName ?: "待确认"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (variant != CourseCardVariant.PREVIEW) {
            InfoChip(text = if (course.remindersEnabled) "提醒已开启" else "提醒已关闭")
        }
    }
}
