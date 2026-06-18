package com.solace.sleep.ui.theme

import androidx.compose.ui.graphics.Color

// Primary palette
val SolacePurple = Color(0xFF7C5CBF)
val SolacePurpleLight = Color(0xFF9C7FD4)
val SolacePurpleDark = Color(0xFF5B3E99)
val SolaceDeepNavy = Color(0xFF0D1B2A)
val SolaceMidnight = Color(0xFF1A1A2E)
val SolaceTwilight = Color(0xFF16213E)

// Sleep duration color scale
val SleepExcellent = Color(0xFF4CAF50)   // ≥100% goal - Green
val SleepGood = Color(0xFF009688)         // 80-99% - Teal
val SleepFair = Color(0xFFFFC107)         // 60-79% - Amber
val SleepPoor = Color(0xFFF44336)         // <60% - Red
val SleepNoData = Color(0xFF757575)       // No data - Gray

// Light theme
val md_theme_light_primary = Color(0xFF7C5CBF)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFE9DDFF)
val md_theme_light_onPrimaryContainer = Color(0xFF230058)
val md_theme_light_secondary = Color(0xFF635B70)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFE9DEF8)
val md_theme_light_onSecondaryContainer = Color(0xFF1E192B)
val md_theme_light_tertiary = Color(0xFF7E5260)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFFFD9E3)
val md_theme_light_onTertiaryContainer = Color(0xFF31101D)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFFFFBFE)
val md_theme_light_onBackground = Color(0xFF1C1B1F)
val md_theme_light_surface = Color(0xFFFFFBFE)
val md_theme_light_onSurface = Color(0xFF1C1B1F)
val md_theme_light_surfaceVariant = Color(0xFFE7E0EC)
val md_theme_light_onSurfaceVariant = Color(0xFF49454F)
val md_theme_light_outline = Color(0xFF79747E)

// Dark theme
val md_theme_dark_primary = Color(0xFFCFBDFF)
val md_theme_dark_onPrimary = Color(0xFF3B008A)
val md_theme_dark_primaryContainer = Color(0xFF5700B4)
val md_theme_dark_onPrimaryContainer = Color(0xFFE9DDFF)
val md_theme_dark_secondary = Color(0xFFCDC2DB)
val md_theme_dark_onSecondary = Color(0xFF332D41)
val md_theme_dark_secondaryContainer = Color(0xFF4A4458)
val md_theme_dark_onSecondaryContainer = Color(0xFFE9DEF8)
val md_theme_dark_tertiary = Color(0xFFEFB8C8)
val md_theme_dark_onTertiary = Color(0xFF4A2532)
val md_theme_dark_tertiaryContainer = Color(0xFF633B48)
val md_theme_dark_onTertiaryContainer = Color(0xFFFFD9E3)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF1C1B1F)
val md_theme_dark_onBackground = Color(0xFFE6E1E5)
val md_theme_dark_surface = Color(0xFF1C1B1F)
val md_theme_dark_onSurface = Color(0xFFE6E1E5)
val md_theme_dark_surfaceVariant = Color(0xFF49454F)
val md_theme_dark_onSurfaceVariant = Color(0xFFCAC4D0)
val md_theme_dark_outline = Color(0xFF938F99)

fun sleepDurationColor(durationMinutes: Int, goalMinutes: Int): Color {
    if (goalMinutes <= 0 || durationMinutes <= 0) return SleepNoData
    val percent = durationMinutes.toFloat() / goalMinutes * 100f
    return when {
        percent >= 100f -> SleepExcellent
        percent >= 80f -> SleepGood
        percent >= 60f -> SleepFair
        else -> SleepPoor
    }
}
