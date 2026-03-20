package com.hainanu.signinassistant.data.parser

import android.net.Uri
import com.hainanu.signinassistant.domain.model.ImportedTimetableBundle

interface TimetableParser {
    suspend fun parse(uri: Uri): ImportedTimetableBundle
}
