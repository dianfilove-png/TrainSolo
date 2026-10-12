package com.trainsolo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trainsolo.app.logic.formatDay
import java.time.LocalDate

/**
 * Выбранный день. null — «сегодня»: после полуночи экран сам переключится на новый день.
 * Возвращает дату и функцию её смены.
 */
@Composable
fun rememberSelectedDate(today: LocalDate): Pair<LocalDate, (LocalDate) -> Unit> {
    var picked by rememberSaveable { mutableStateOf<String?>(null) }
    val date = picked?.let(LocalDate::parse)?.takeIf { it < today } ?: today
    return date to { d: LocalDate -> picked = if (d >= today) null else d.toString() }
}

@Composable
fun DaySwitcher(date: LocalDate, today: LocalDate, onChange: (LocalDate) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(C.surface, RoundedCornerShape(12.dp)).border(1.dp, C.border, RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onChange(date.minusDays(1)) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Предыдущий день", tint = C.text)
        }
        Text(
            "📅  " + formatDay(date, today),
            color = C.text, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f).clickable { onChange(today) },
        )
        IconButton(onClick = { onChange(date.plusDays(1)) }, enabled = date < today) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Следующий день", tint = if (date < today) C.text else C.border)
        }
    }
}
