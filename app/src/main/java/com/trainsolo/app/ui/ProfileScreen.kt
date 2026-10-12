package com.trainsolo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trainsolo.app.logic.Goals
import com.trainsolo.app.logic.LevelInfo
import com.trainsolo.app.logic.Totals
import com.trainsolo.app.logic.achievements
import com.trainsolo.app.logic.autoCalories
import com.trainsolo.app.logic.grouped
import com.trainsolo.app.logic.num
import com.trainsolo.app.model.Activity
import com.trainsolo.app.model.Goal
import com.trainsolo.app.model.Sex
import com.trainsolo.app.store.AppStore
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(store: AppStore, level: LevelInfo, goals: Goals, totals: Totals) {
    val p = store.state.profile
    val toast = LocalToast.current
    var customKcal by remember { mutableStateOf(p.customCalories?.toString() ?: "") }
    var confirmReset by remember { mutableStateOf(false) }
    val list = achievements(totals, level.level, store.state.workouts)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle("Профиль", "Ур. ${level.level} · Ранг ${level.rank.letter}")

        Card {
            SectionTitle("Герой")
            Field("Имя", p.name, { name -> store.updateProfile { it.copy(name = name) } }, placeholder = "Охотник")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Sex.entries.forEach { s -> Chip(s.title, p.sex == s, { store.updateProfile { it.copy(sex = s) } }) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Stepper("Возраст", p.age.toDouble(), { v -> store.updateProfile { it.copy(age = v.roundToInt()) } }, Modifier.weight(1f), min = 10.0)
                Stepper("Рост", p.heightCm, { v -> store.updateProfile { it.copy(heightCm = v) } }, Modifier.weight(1f), min = 100.0, suffix = "см")
            }
            Stepper("Вес", p.weightKg, { v -> store.updateProfile { it.copy(weightKg = v) } }, step = 0.5, min = 30.0, suffix = "кг")
        }

        Card {
            SectionTitle("Активность и цель")
            Label("Активность")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Activity.entries.forEach { a -> Chip(a.title, p.activity == a, { store.updateProfile { it.copy(activity = a) } }) }
            }
            Label("Цель")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Goal.entries.forEach { g -> Chip(g.title, p.goal == g, { store.updateProfile { it.copy(goal = g) } }, color = C.xp) }
            }
            Label("Дневная норма")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Triple("Калории", "${goals.calories}", C.xp),
                    Triple("Белки", "${goals.protein} г", C.protein),
                    Triple("Жиры", "${goals.fat} г", C.fat),
                    Triple("Углеводы", "${goals.carbs} г", C.carbs),
                ).forEach { (label, value, color) ->
                    Column(
                        Modifier.weight(1f).background(C.surfaceAlt, RoundedCornerShape(8.dp)).padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(value, color = C.text, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
            Field(
                "Своя норма калорий (авто: ${autoCalories(p)})", customKcal,
                { t ->
                    customKcal = t
                    val n = parseNumber(t)?.roundToInt()
                    store.updateProfile { it.copy(customCalories = n?.takeIf { v -> v >= 800 }) }
                },
                placeholder = "Пусто — считать автоматически", numeric = true,
            )
            Text("Норма считается по формуле Миффлина — Сан Жеора с учётом активности и цели.", color = C.textMuted, fontSize = 12.sp)
        }

        Card {
            SectionTitle("Достижения") { Text("${list.count { it.done }}/${list.size}", color = C.xp, fontWeight = FontWeight.Bold) }
            list.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { a ->
                        Column(
                            Modifier.weight(1f)
                                .background(if (a.done) Color(0xFF1F1A0B) else C.surfaceAlt, RoundedCornerShape(12.dp))
                                .border(1.dp, if (a.done) C.xp else C.border, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(a.emoji, fontSize = 24.sp, modifier = Modifier.alpha(if (a.done) 1f else 0.35f))
                            Text(a.title, color = if (a.done) C.text else C.textDim, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                            Text(a.description, color = C.textMuted, fontSize = 12.sp, lineHeight = 16.sp)
                            if (!a.done && a.target > 1) ProgressBar((a.current / a.target).toFloat(), C.textDim, 4)
                        }
                    }
                    if (row.size == 1) Column(Modifier.weight(1f)) {}
                }
            }
        }

        Card {
            SectionTitle("Статистика")
            listOf(
                "Всего опыта" to "${grouped(level.totalXp)} XP",
                "Тренировочных дней" to "${totals.workoutDays}",
                "Подходов" to grouped(totals.totalSets),
                "Тоннаж" to "${grouped(totals.tonnage)} кг",
                "Кардио" to "${grouped(totals.cardioMinutes)} мин · ${num(totals.cardioKm)} км",
                "Лучшая серия" to "${totals.bestStreak} дн.",
                "Дней с дневником питания" to "${totals.foodDays}",
            ).forEach { (label, value) ->
                Row(Modifier.fillMaxWidth()) {
                    Text(label, color = C.textDim, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text(value, color = C.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        SecondaryButton("Сбросить весь прогресс", { confirmReset = true }, Modifier.fillMaxWidth(), color = C.danger)
    }

    if (confirmReset) {
        ConfirmDialog(
            "Сбросить прогресс?", "Все тренировки, питание и опыт будут удалены без возможности восстановления.", "Сбросить",
            onConfirm = {
                store.reset()
                customKcal = ""
                toast("Прогресс сброшен")
            },
            onDismiss = { confirmReset = false },
        )
    }
}

@Composable
private fun Label(text: String) {
    Text(text, color = C.textDim, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
}
