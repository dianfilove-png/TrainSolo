package com.trainsolo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trainsolo.app.data.program
import com.trainsolo.app.logic.DayContext
import com.trainsolo.app.logic.Goals
import com.trainsolo.app.logic.LevelInfo
import com.trainsolo.app.logic.MuscleState
import com.trainsolo.app.logic.MuscleStatus
import com.trainsolo.app.logic.Totals
import com.trainsolo.app.logic.characterStats
import com.trainsolo.app.logic.plural
import com.trainsolo.app.logic.questStatuses
import com.trainsolo.app.logic.sumFoods
import com.trainsolo.app.model.Muscle
import com.trainsolo.app.model.QuestClaim
import com.trainsolo.app.model.Sex
import com.trainsolo.app.store.AppStore
import kotlin.math.roundToInt

private fun describe(s: MuscleState): String = when (s.daysSince) {
    null -> "Ещё не тренировалась. Самое время начать!"
    0 -> "Нагружена сегодня (${maxOf(1, s.sets.roundToInt())} подх.). Дай ей отдохнуть — восстановится послезавтра."
    1 -> "Тренировалась вчера и сейчас восстанавливается. Будет готова завтра."
    else -> "Последняя нагрузка ${s.daysSince} ${plural(s.daysSince, "день", "дня", "дней")} назад. Готова к бою!"
}

@Composable
fun HeroScreen(
    store: AppStore,
    level: LevelInfo,
    muscles: Map<Muscle, MuscleState>,
    totals: Totals,
    goals: Goals,
    onGo: (Destination) -> Unit,
) {
    val state = store.state
    val today = store.today
    val toast = LocalToast.current
    var selected by rememberSaveable { mutableStateOf<Muscle?>(null) }

    val todayWorkouts = remember(state.workouts, today) { state.workouts.filter { it.date == today } }
    val todayFoods = remember(state.foods, today) { state.foods.filter { it.date == today } }
    val quests = questStatuses(today, DayContext(todayWorkouts, todayFoods, goals), state.questClaims)
    val eaten = sumFoods(todayFoods).calories.roundToInt()
    val todaySets = todayWorkouts.sumOf { it.sets }
    val active = program(state.activeProgramId)
    val rank = level.rank
    val rankColor = Color(rank.color)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(border = rankColor.copy(alpha = 0.55f), background = Color(0xFF0B1322)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    Modifier.size(56.dp).background(C.bg, RoundedCornerShape(12.dp)).border(2.dp, rankColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(rank.letter, color = rankColor, fontSize = 30.sp, fontWeight = FontWeight.Black)
                }
                Column(Modifier.weight(1f)) {
                    Text(state.profile.name, color = C.text, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                    Text("Ранг ${rank.letter} · ${rank.title}", color = C.textDim, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("УРОВЕНЬ", color = C.textMuted, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                    Text("${level.level}", color = C.xp, fontSize = 32.sp, fontWeight = FontWeight.Black)
                }
            }
            ProgressBar(level.progress, C.xp, 10)
            Row {
                Text("${level.current} / ${level.needed} XP", color = C.xp, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(
                    "🔥 Серия: ${totals.currentStreak} ${plural(totals.currentStreak, "день", "дня", "дней")}",
                    color = C.textDim, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                characterStats(totals).forEach { s ->
                    Column(
                        Modifier.weight(1f).background(C.surfaceAlt, RoundedCornerShape(8.dp)).padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(s.label, color = Color(s.color), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Text("${s.value}", color = C.text, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        if (state.workouts.isEmpty() && state.foods.isEmpty()) {
            Card(border = C.primary) {
                Text("[ СИСТЕМА ]", color = C.primary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp)
                Text(
                    "Добро пожаловать, ${state.profile.name}! Записывай тренировки и еду, выполняй ежедневные задания и прокачивай героя от ранга E до S.",
                    color = C.text, fontSize = 15.sp, lineHeight = 22.sp,
                )
                PrimaryButton("Выбрать программу", onClick = { onGo(Destination.PROGRAMS) }, modifier = Modifier.fillMaxWidth(), icon = "📋")
            }
        }

        Card {
            SectionTitle("Состояние мышц")
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val figure = ((maxWidth - 8.dp) / 2).coerceAtMost(170.dp)
                BodyMap(muscles, selected, onSelect = { selected = if (selected == it) null else it }, figureWidth = figure, aura = rankColor, female = state.profile.sex == Sex.FEMALE)
            }
            StatusLegend(muscles)
            val sel = selected
            if (sel != null) {
                val ms = muscles.getValue(sel)
                Column(
                    Modifier.fillMaxWidth().background(C.surfaceAlt, RoundedCornerShape(12.dp))
                        .border(1.dp, ms.status.color(), RoundedCornerShape(12.dp)).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(sel.title, color = C.text, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                        Text(ms.status.label, color = ms.status.color(), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(describe(ms), color = C.textDim, fontSize = 14.sp, lineHeight = 20.sp)
                }
            } else {
                val loaded = Muscle.entries.filter { muscles.getValue(it).status == MuscleStatus.LOADED }
                val recovering = Muscle.entries.filter { muscles.getValue(it).status == MuscleStatus.RECOVERING }
                if (loaded.isNotEmpty()) StatusLine("Сегодня: ", C.loaded, loaded)
                if (recovering.isNotEmpty()) StatusLine("Вчера: ", C.recovering, recovering)
                Text(
                    if (loaded.isEmpty() && recovering.isEmpty()) "Все мышцы восстановлены — нажми на любую, чтобы узнать подробности"
                    else "Нажми на мышцу, чтобы узнать подробности",
                    color = C.textMuted, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Card {
            SectionTitle("Ежедневные задания")
            quests.forEach { q ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (q.claimed) "✅" else if (q.done) "🎁" else "⚪", fontSize = 20.sp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row {
                            Text(q.def.title, color = if (q.claimed) C.textMuted else C.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text("${minOf(q.current, q.target)}/${q.target} ${q.def.unit}", color = C.textMuted, fontSize = 12.sp)
                        }
                        ProgressBar(q.current.toFloat() / q.target, if (q.done) C.fresh else C.primary, 6)
                    }
                    if (q.done && !q.claimed) {
                        SmallButton("+${q.def.xp}", filled = true, onClick = {
                            store.claimQuest(QuestClaim(today, q.def.id, q.def.xp))
                            toast("Задание выполнено! +${q.def.xp} XP")
                        })
                    } else {
                        Text(
                            if (q.claimed) "Получено" else "${q.def.xp} XP",
                            color = if (q.claimed) C.textMuted else C.xp, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End, modifier = Modifier.widthIn(min = 58.dp),
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniCard(Modifier.weight(1f), "🏋️", "$todaySets", plural(todaySets, "подход", "подхода", "подходов") + " сегодня" +
                (active?.let { "\n${it.emoji} ${it.name}" } ?: "")) { onGo(Destination.WORKOUT) }
            MiniCard(Modifier.weight(1f), "🍽️", "$eaten", "из ${goals.calories} ккал") { onGo(Destination.NUTRITION) }
        }
    }
}

@Composable
private fun StatusLine(prefix: String, color: Color, muscles: List<Muscle>) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) { append(prefix) }
            append(muscles.joinToString(", ") { it.title })
        },
        color = C.text, fontSize = 14.sp, lineHeight = 20.sp,
    )
}

@Composable
private fun MiniCard(modifier: Modifier, emoji: String, value: String, label: String, onClick: () -> Unit) {
    Column(
        modifier.background(C.surface, RoundedCornerShape(16.dp)).border(1.dp, C.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(emoji, fontSize = 20.sp)
        Text(value, color = C.text, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = C.textDim, fontSize = 13.sp)
    }
}
