package com.trainsolo.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.trainsolo.app.logic.MuscleStatus

object C {
    val bg = Color(0xFF070B14)
    val surface = Color(0xFF0E1526)
    val surfaceAlt = Color(0xFF141E33)
    val border = Color(0xFF1F2B47)
    val primary = Color(0xFF38BDF8)
    val primaryDim = Color(0xFF0C4A6E)
    val xp = Color(0xFFFBBF24)
    val text = Color(0xFFE2E8F0)
    val textDim = Color(0xFF94A3B8)
    val textMuted = Color(0xFF64748B)
    val danger = Color(0xFFF87171)
    val loaded = Color(0xFFEF4444)
    val recovering = Color(0xFFFACC15)
    val fresh = Color(0xFF22C55E)
    val protein = Color(0xFF60A5FA)
    val fat = Color(0xFFF472B6)
    val carbs = Color(0xFF34D399)
    val body = Color(0xFF1A2338)
    val bodyStroke = Color(0xFF2C3A5C)
}

fun MuscleStatus.color(): Color = when (this) {
    MuscleStatus.LOADED -> C.loaded
    MuscleStatus.RECOVERING -> C.recovering
    MuscleStatus.FRESH -> C.fresh
}

@Composable
fun TrainSoloTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = C.primary,
            onPrimary = Color(0xFF03121F),
            background = C.bg,
            onBackground = C.text,
            surface = C.surface,
            onSurface = C.text,
            surfaceVariant = C.surfaceAlt,
            onSurfaceVariant = C.textDim,
            surfaceContainer = C.surface,
            surfaceContainerLow = C.surface,
            surfaceContainerHigh = C.surfaceAlt,
            secondaryContainer = C.primaryDim,
            onSecondaryContainer = C.text,
            outline = C.border,
            error = C.danger,
        ),
        content = content,
    )
}
