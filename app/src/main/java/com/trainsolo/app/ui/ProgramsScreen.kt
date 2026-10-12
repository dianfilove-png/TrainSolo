package com.trainsolo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trainsolo.app.data.PROGRAMS
import com.trainsolo.app.data.exercise
import com.trainsolo.app.data.program
import com.trainsolo.app.logic.formatAmount
import com.trainsolo.app.model.Muscle
import com.trainsolo.app.model.Program
import com.trainsolo.app.model.ProgramLevel
import com.trainsolo.app.store.AppStore

fun ProgramLevel.color(): Color = when (this) {
    ProgramLevel.BEGINNER -> C.fresh
    ProgramLevel.INTERMEDIATE -> C.primary
    ProgramLevel.HARDCORE -> C.loaded
}

/** Мышцы, которые программа нагружает как основные. */
fun Program.muscles(): List<Muscle> =
    days.flatMap { d -> d.exercises.flatMap { exercise(it.exerciseId)?.primary.orEmpty() } }.distinct()

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProgramBadges(p: Program, active: Boolean) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Badge(p.level.title, p.level.color())
        Badge(p.daysPerWeek, C.textDim)
        if (active) Badge("Активна", C.primary)
    }
}

@Composable
fun ProgramsScreen(store: AppStore, onOpen: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle("Программы", "Выбери путь развития своего героя")
        PROGRAMS.forEach { p ->
            val active = store.state.activeProgramId == p.id
            Column(
                Modifier.fillMaxWidth().background(C.surface, RoundedCornerShape(16.dp))
                    .border(1.dp, if (active) C.primary else C.border, RoundedCornerShape(16.dp))
                    .clickable { onOpen(p.id) }.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(p.emoji, fontSize = 32.sp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(p.name, color = C.text, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                        ProgramBadges(p, active)
                    }
                    Text("›", color = C.textMuted, fontSize = 24.sp)
                }
                Text(p.description, color = C.textDim, fontSize = 14.sp, lineHeight = 20.sp, maxLines = 3)
                Text(p.muscles().joinToString(" · ") { it.title }, color = C.textMuted, fontSize = 12.sp, maxLines = 2)
            }
        }
    }
}

@Composable
fun ProgramDetailScreen(store: AppStore, id: String, onBack: () -> Unit, onStarted: () -> Unit) {
    val p = program(id) ?: return Empty("⚠️", "Программа не найдена")
    val toast = LocalToast.current
    val active = store.state.activeProgramId == p.id
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = C.text) }
            Text("${p.emoji} ${p.name}", color = C.text, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }
        ProgramBadges(p, active)
        Text(p.description, color = C.textDim, fontSize = 15.sp, lineHeight = 22.sp)
        Text("Нагружает: " + p.muscles().joinToString { it.title.lowercase() }, color = C.textMuted, fontSize = 13.sp)
        if (active) {
            SecondaryButton("Остановить программу", {
                store.setActiveProgram(null)
                toast("Программа остановлена")
            }, Modifier.fillMaxWidth(), color = C.danger)
        } else {
            PrimaryButton("Начать программу", {
                store.setActiveProgram(p.id)
                store.setProgramDay(p.id, 0)
                toast("${p.emoji} Программа «${p.name}» активна")
                onStarted()
            }, Modifier.fillMaxWidth(), icon = "▶")
        }
        p.days.forEachIndexed { i, day ->
            Card {
                Text("${i + 1}. ${day.name}", color = C.primary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                day.exercises.forEach { pl ->
                    val ex = exercise(pl.exerciseId) ?: return@forEach
                    HorizontalDivider(color = C.border)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(ex.name, color = C.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(ex.primary.joinToString { it.title }, color = C.textMuted, fontSize = 12.sp)
                        }
                        Text(formatAmount(ex, pl.sets, pl.reps, pl.weight, pl.durationMin, pl.distanceKm), color = C.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
