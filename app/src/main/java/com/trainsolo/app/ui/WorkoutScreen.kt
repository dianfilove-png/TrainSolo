package com.trainsolo.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trainsolo.app.data.exercise
import com.trainsolo.app.data.program
import com.trainsolo.app.logic.formatAmount
import com.trainsolo.app.logic.grouped
import com.trainsolo.app.logic.workoutXp
import com.trainsolo.app.model.ExerciseKind
import com.trainsolo.app.model.Planned
import com.trainsolo.app.model.WorkoutEntry
import com.trainsolo.app.store.AppStore
import kotlin.math.roundToInt

private data class SheetRequest(val exerciseId: String?, val planned: Planned?, val nonce: Int)

@Composable
fun WorkoutScreen(store: AppStore, onOpenProgram: (String) -> Unit, onPickProgram: () -> Unit) {
    val state = store.state
    val today = store.today
    val toast = LocalToast.current
    val (date, setDate) = rememberSelectedDate(today)
    var sheet by remember { mutableStateOf<SheetRequest?>(null) }
    var nonce by remember { mutableIntStateOf(0) }
    var toDelete by remember { mutableStateOf<WorkoutEntry?>(null) }

    val entries = state.workouts.filter { it.date == date }.sortedBy { it.createdAt }
    val done = entries.map { it.exerciseId }.toSet()
    var sets = 0
    var minutes = 0
    var tonnage = 0.0
    var xp = 0
    for (e in entries) {
        val ex = exercise(e.exerciseId) ?: continue
        if (ex.kind == ExerciseKind.CARDIO) minutes += e.durationMin else sets += e.sets
        if (ex.kind == ExerciseKind.STRENGTH) tonnage += e.sets * e.reps * e.weight
        xp += workoutXp(e.exerciseId, e.sets, e.durationMin)
    }

    val active = program(state.activeProgramId)
    val dayIndex = active?.let { minOf(state.programProgress[it.id] ?: 0, it.days.size - 1) } ?: 0
    val day = active?.days?.get(dayIndex)

    fun log(id: String, v: ExerciseValues) {
        store.addWorkout(date, id, v.sets, v.reps, v.weight, v.durationMin, v.distanceKm)
        toast("+${workoutXp(id, v.sets, v.durationMin)} XP · ${exercise(id)?.name}")
    }

    fun open(id: String?, planned: Planned?) {
        nonce++
        sheet = SheetRequest(id, planned, nonce)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle("Тренировка", "Записывай всё, что сделал — мышцы на аватаре окрасятся")
        DaySwitcher(date, today, setDate)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniStat("${entries.size}", "упражн.")
            MiniStat("$sets", "подходов")
            MiniStat("$minutes", "мин кардио")
            MiniStat(grouped(tonnage.roundToInt()), "кг тоннаж")
        }
        PrimaryButton("Добавить упражнение", { open(null, null) }, Modifier.fillMaxWidth(), icon = "＋")

        if (active != null && day != null) {
            Card(border = C.primaryDim) {
                SectionTitle("${active.emoji} ${active.name}") {
                    Text("Подробнее", color = C.primary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onOpenProgram(active.id) })
                }
                if (active.days.size > 1) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        itemsIndexed(active.days) { i, d -> Chip(d.name, i == dayIndex, { store.setProgramDay(active.id, i) }) }
                    }
                }
                day.exercises.forEach { p ->
                    val ex = exercise(p.exerciseId) ?: return@forEach
                    val isDone = p.exerciseId in done
                    Row(
                        Modifier.fillMaxWidth().clickable { open(p.exerciseId, p) }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(if (isDone) "✅" else "⚪", fontSize = 20.sp)
                        Column(Modifier.weight(1f)) {
                            Text(
                                ex.name, color = if (isDone) C.textMuted else C.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                                textDecoration = if (isDone) TextDecoration.LineThrough else null,
                            )
                            Text(formatAmount(ex, p.sets, p.reps, p.weight, p.durationMin, p.distanceKm), color = C.textDim, fontSize = 13.sp)
                        }
                        if (!isDone) SmallButton("Готово", { log(p.exerciseId, defaultValues(ex, p)) })
                    }
                }
                val next = (dayIndex + 1) % active.days.size
                TextButton(
                    onClick = {
                        store.setProgramDay(active.id, next)
                        toast("День завершён! Следующий: ${active.days[next].name}")
                    },
                    enabled = day.exercises.any { it.exerciseId in done },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "🏁 Завершить день" + if (active.days.size > 1) " → ${active.days[next].name}" else "",
                        color = if (day.exercises.any { it.exerciseId in done }) C.primary else C.textMuted,
                        fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    )
                }
            }
        } else {
            Card {
                Text("Выбери программу — и здесь появится план на день с чек-листом упражнений.", color = C.textDim, fontSize = 14.sp, lineHeight = 20.sp)
                SecondaryButton("📋  Выбрать программу", onPickProgram, Modifier.fillMaxWidth())
            }
        }

        Card {
            SectionTitle("Выполнено") { if (xp > 0) Text("+$xp XP", color = C.xp, fontWeight = FontWeight.Bold) }
            if (entries.isEmpty()) {
                Empty("🏋️", "Пока пусто. Добавь первое упражнение!")
            } else {
                entries.forEach { e ->
                    val ex = exercise(e.exerciseId) ?: return@forEach
                    HorizontalDivider(color = C.border)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(ex.name, color = C.text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(formatAmount(ex, e.sets, e.reps, e.weight, e.durationMin, e.distanceKm), color = C.primary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(ex.primary.joinToString { it.title }, color = C.textMuted, fontSize = 12.sp, maxLines = 1)
                        }
                        Text("+${workoutXp(e.exerciseId, e.sets, e.durationMin)}", color = C.xp, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        IconButton(onClick = { toDelete = e }) { Icon(Icons.Filled.Delete, "Удалить", tint = C.textDim) }
                    }
                }
            }
        }
    }

    sheet?.let { req ->
        androidx.compose.runtime.key(req.nonce) {
            ExerciseSheet(req.exerciseId, req.planned, onDismiss = { sheet = null }) { id, v ->
                log(id, v)
                sheet = null
            }
        }
    }
    toDelete?.let { e ->
        ConfirmDialog("Удалить упражнение?", exercise(e.exerciseId)?.name ?: "", "Удалить", { store.removeWorkout(e.id) }, { toDelete = null })
    }
}
