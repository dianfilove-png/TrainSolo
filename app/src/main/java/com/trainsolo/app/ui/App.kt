package com.trainsolo.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.trainsolo.app.logic.computeTotals
import com.trainsolo.app.logic.levelInfo
import com.trainsolo.app.logic.muscleStates
import com.trainsolo.app.logic.nutritionGoals
import com.trainsolo.app.logic.rankForLevel
import com.trainsolo.app.logic.totalXp
import com.trainsolo.app.store.AppStore
import kotlinx.coroutines.delay

/** Показать короткое уведомление вверху экрана. */
val LocalToast = staticCompositionLocalOf<(String) -> Unit> { {} }

private enum class Tab(val title: String, val emoji: String) {
    HERO("Герой", "🦸"),
    WORKOUT("Тренировка", "🏋️"),
    PROGRAMS("Программы", "📋"),
    NUTRITION("Питание", "🍽️"),
    PROFILE("Профиль", "👤"),
}

@Composable
fun App(store: AppStore) {
    val state = store.state
    val today = store.today
    val level = remember(state) { levelInfo(totalXp(state)) }
    val goals = remember(state.profile) { nutritionGoals(state.profile) }
    val muscles = remember(state.workouts, today) { muscleStates(state.workouts, today) }
    val totals = remember(state, today) { computeTotals(state, today) }

    var tab by rememberSaveable { mutableStateOf(Tab.HERO) }
    var openProgram by rememberSaveable { mutableStateOf<String?>(null) }

    var toast by remember { mutableStateOf<Pair<String, Long>?>(null) }
    val showToast: (String) -> Unit = { toast = it to System.nanoTime() }
    LaunchedEffect(toast) {
        if (toast != null) {
            delay(1900)
            toast = null
        }
    }

    BackHandler(enabled = openProgram != null) { openProgram = null }
    BackHandler(enabled = openProgram == null && tab != Tab.HERO) { tab = Tab.HERO }

    CompositionLocalProvider(LocalToast provides showToast) {
        Box(Modifier.fillMaxSize().background(C.bg)) {
            Scaffold(
                containerColor = C.bg,
                bottomBar = {
                    NavigationBar(containerColor = C.surface, tonalElevation = 0.dp) {
                        Tab.entries.forEach { t ->
                            NavigationBarItem(
                                selected = tab == t && openProgram == null,
                                onClick = { tab = t; openProgram = null },
                                icon = { Text(t.emoji, fontSize = 20.sp) },
                                label = { Text(t.title, fontSize = 11.sp, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedTextColor = C.primary,
                                    unselectedTextColor = C.textMuted,
                                    indicatorColor = C.primaryDim,
                                ),
                            )
                        }
                    }
                },
            ) { padding ->
                Box(Modifier.padding(padding)) {
                    val program = openProgram
                    if (program != null) {
                        ProgramDetailScreen(store, program, onBack = { openProgram = null }, onStarted = { openProgram = null; tab = Tab.WORKOUT })
                    } else {
                        when (tab) {
                            Tab.HERO -> HeroScreen(store, level, muscles, totals, goals, onGo = { tab = it.toTab() })
                            Tab.WORKOUT -> WorkoutScreen(store, onOpenProgram = { openProgram = it }, onPickProgram = { tab = Tab.PROGRAMS })
                            Tab.PROGRAMS -> ProgramsScreen(store, onOpen = { openProgram = it })
                            Tab.NUTRITION -> NutritionScreen(store, goals)
                            Tab.PROFILE -> ProfileScreen(store, level, goals, totals)
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = toast != null,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 8.dp, start = 24.dp, end = 24.dp),
            ) {
                Text(
                    toast?.first ?: "",
                    color = C.xp,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(Color(0xFF0B1A2E), CircleShape)
                        .border(1.dp, C.xp, CircleShape)
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                )
            }
        }
    }

    LevelUpWatcher(level.level)
}

/** Куда переходить с карточек главного экрана. */
enum class Destination { WORKOUT, NUTRITION, PROGRAMS }

private fun Destination.toTab() = when (this) {
    Destination.WORKOUT -> Tab.WORKOUT
    Destination.NUTRITION -> Tab.NUTRITION
    Destination.PROGRAMS -> Tab.PROGRAMS
}

/** Окно «Уровень повышен», когда уровень превышает последний просмотренный. */
@Composable
private fun LevelUpWatcher(level: Int) {
    var seen by rememberSaveable { mutableIntStateOf(level) }
    // Уровень мог упасть (удалили записи или сбросили прогресс) — запоминаем новый минимум.
    if (level < seen) seen = level
    if (level <= seen) return

    val rank = rankForLevel(level)
    val newRank = rankForLevel(seen).letter != rank.letter
    val rankColor = Color(rank.color)
    Dialog(onDismissRequest = { seen = level }) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(C.surface, RoundedCornerShape(22.dp))
                .border(1.5.dp, rankColor, RoundedCornerShape(22.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("[ СИСТЕМА ]", color = C.primary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp)
            Text("УРОВЕНЬ ПОВЫШЕН!", color = C.text, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("$level", color = rankColor, fontSize = 72.sp, fontWeight = FontWeight.Black)
            Text(
                if (newRank) "Новый ранг: ${rank.letter} — ${rank.title}" else "Ты стал сильнее. Продолжай в том же духе.",
                color = C.textDim,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
            PrimaryButton("Продолжить", onClick = { seen = level }, modifier = Modifier.fillMaxWidth())
        }
    }
}
