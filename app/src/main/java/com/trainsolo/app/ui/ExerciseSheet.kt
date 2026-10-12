package com.trainsolo.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trainsolo.app.data.EXERCISES
import com.trainsolo.app.data.exercise
import com.trainsolo.app.logic.workoutXp
import com.trainsolo.app.model.Exercise
import com.trainsolo.app.model.ExerciseKind
import com.trainsolo.app.model.Muscle
import com.trainsolo.app.model.Planned
import kotlin.math.roundToInt

data class ExerciseValues(val sets: Int, val reps: Int, val weight: Double, val durationMin: Int, val distanceKm: Double)

fun defaultValues(ex: Exercise, planned: Planned? = null): ExerciseValues {
    val cardio = ex.kind == ExerciseKind.CARDIO
    return if (planned != null) {
        ExerciseValues(planned.sets, planned.reps, planned.weight, planned.durationMin, planned.distanceKm)
    } else {
        ExerciseValues(
            sets = if (cardio) 1 else 3,
            reps = if (cardio) 0 else if (ex.seconds) 30 else 10,
            weight = if (ex.kind == ExerciseKind.STRENGTH) 20.0 else 0.0,
            durationMin = if (cardio) 30 else 0,
            distanceKm = 0.0,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MuscleTags(ex: Exercise) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ex.primary.forEach { Badge(it.title, C.loaded) }
        ex.secondary.forEach { Badge(it.title, C.textDim) }
    }
}

/** Выбор упражнения и ввод подходов. Если задан [exerciseId] — сразу открывается форма. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSheet(exerciseId: String?, planned: Planned?, onDismiss: () -> Unit, onSave: (String, ExerciseValues) -> Unit) {
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var picked by remember { mutableStateOf(exerciseId?.let(::exercise)) }
    var values by remember { mutableStateOf(picked?.let { defaultValues(it, planned) }) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet, containerColor = C.surface) {
        Column(Modifier.padding(horizontal = 16.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val ex = picked
            val v = values
            Text(ex?.name ?: "Выбери упражнение", color = C.text, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
            if (ex != null && v != null) {
                LogForm(ex, v, { values = it }, onBack = if (exerciseId == null) ({ picked = null }) else null, onSave = { onSave(ex.id, v) })
            } else {
                Picker { picked = it; values = defaultValues(it) }
            }
        }
    }
}

@Composable
private fun Picker(onPick: (Exercise) -> Unit) {
    var query by remember { mutableStateOf("") }
    // null — все, "cardio" — только кардио, иначе имя мышцы.
    var filter by remember { mutableStateOf<String?>(null) }
    val f = filter
    val list = EXERCISES.filter { e ->
        (query.isBlank() || e.name.contains(query.trim(), ignoreCase = true)) &&
            when (f) {
                null -> true
                "cardio" -> e.kind == ExerciseKind.CARDIO
                else -> Muscle.valueOf(f).let { m -> m in e.primary || m in e.secondary }
            }
    }
    Field("Поиск", query, { query = it }, placeholder = "Название упражнения")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        item { Chip("Все", filter == null, { filter = null }) }
        item { Chip("Кардио", filter == "cardio", { filter = "cardio" }) }
        items(Muscle.entries) { m -> Chip(m.title, filter == m.name, { filter = m.name }) }
    }
    LazyColumn(Modifier.heightIn(min = 200.dp, max = 460.dp)) {
        items(list, key = { it.id }) { e ->
            Row(
                Modifier.fillMaxWidth().clickable { onPick(e) }.padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(e.name, color = C.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        (if (e.kind == ExerciseKind.CARDIO) "Кардио · " else "") + e.primary.joinToString { it.title },
                        color = C.textMuted, fontSize = 13.sp, maxLines = 1,
                    )
                }
                Text("＋", color = C.primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = C.border)
        }
        if (list.isEmpty()) item { Text("Ничего не найдено", color = C.textMuted, modifier = Modifier.padding(12.dp)) }
    }
}

@Composable
private fun LogForm(ex: Exercise, v: ExerciseValues, onChange: (ExerciseValues) -> Unit, onBack: (() -> Unit)?, onSave: () -> Unit) {
    val xp = workoutXp(ex.id, v.sets, v.durationMin)
    val valid = if (ex.kind == ExerciseKind.CARDIO) v.durationMin > 0 else v.sets > 0 && v.reps > 0
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Нагружаемые мышцы (красные — основные):", color = C.textDim, fontSize = 13.sp)
            MuscleTags(ex)
        }
        if (ex.kind == ExerciseKind.CARDIO) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Stepper("Время", v.durationMin.toDouble(), { onChange(v.copy(durationMin = it.roundToInt())) }, Modifier.weight(1f), step = 5.0, suffix = "мин")
                Stepper("Дистанция", v.distanceKm, { onChange(v.copy(distanceKm = it)) }, Modifier.weight(1f), step = 0.5, suffix = "км")
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Stepper("Подходы", v.sets.toDouble(), { onChange(v.copy(sets = it.roundToInt())) }, Modifier.weight(1f))
                Stepper(
                    if (ex.seconds) "Секунды" else "Повторы", v.reps.toDouble(), { onChange(v.copy(reps = it.roundToInt())) },
                    Modifier.weight(1f), step = if (ex.seconds) 5.0 else 1.0,
                )
            }
            if (ex.kind == ExerciseKind.STRENGTH) {
                Stepper("Вес", v.weight, { onChange(v.copy(weight = it)) }, step = 2.5, suffix = "кг")
            }
        }
        Text("✨ Награда: +$xp XP", color = C.xp, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            if (onBack != null) SecondaryButton("Назад", onBack, Modifier.weight(1f))
            PrimaryButton("Записать", onSave, Modifier.weight(2f), enabled = valid, icon = "✓")
        }
    }
}
