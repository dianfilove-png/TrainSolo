package com.trainsolo.app.logic

import com.trainsolo.app.data.exercise
import com.trainsolo.app.model.AppState
import com.trainsolo.app.model.ExerciseKind
import java.time.LocalDate

const val XP_PER_SET = 10
const val XP_PER_CARDIO_MIN = 2
const val XP_PER_FOOD = 5
/** Опыт за еду начисляется максимум за столько записей в день. */
const val FOOD_XP_ENTRIES_PER_DAY = 6

fun workoutXp(exerciseId: String, sets: Int, durationMin: Int): Int =
    if (exercise(exerciseId)?.kind == ExerciseKind.CARDIO) durationMin * XP_PER_CARDIO_MIN
    else maxOf(0, sets) * XP_PER_SET

fun totalXp(state: AppState): Int {
    var xp = state.workouts.sumOf { workoutXp(it.exerciseId, it.sets, it.durationMin) }
    val foodPerDay = state.foods.groupingBy { it.date }.eachCount()
    xp += foodPerDay.values.sumOf { minOf(it, FOOD_XP_ENTRIES_PER_DAY) * XP_PER_FOOD }
    xp += state.questClaims.sumOf { it.xp }
    return xp
}

/** Сколько всего опыта нужно, чтобы достичь уровня [level] (уровень 1 — 0 XP). */
fun xpForLevel(level: Int): Int {
    val n = maxOf(0, level - 1)
    return 100 * n + 25 * n * maxOf(0, n - 1)
}

data class Rank(val letter: String, val title: String, val color: Long, val minLevel: Int)

val RANKS = listOf(
    Rank("E", "Новобранец", 0xFF94A3B8, 1),
    Rank("D", "Боец", 0xFF34D399, 5),
    Rank("C", "Воин", 0xFF38BDF8, 10),
    Rank("B", "Ветеран", 0xFFA78BFA, 20),
    Rank("A", "Элита", 0xFFF472B6, 30),
    Rank("S", "Монарх", 0xFFFBBF24, 45),
)

fun rankForLevel(level: Int): Rank = RANKS.last { level >= it.minLevel }

data class LevelInfo(
    val level: Int,
    val totalXp: Int,
    /** Опыт, набранный на текущем уровне. */
    val current: Int,
    /** Сколько опыта нужно для перехода на следующий уровень. */
    val needed: Int,
    val rank: Rank,
) {
    val progress: Float get() = current.toFloat() / needed
}

fun levelInfo(xp: Int): LevelInfo {
    var level = 1
    while (xpForLevel(level + 1) <= xp) level++
    val start = xpForLevel(level)
    return LevelInfo(level, xp, xp - start, xpForLevel(level + 1) - start, rankForLevel(level))
}

/** XP за один день — для сводки на экране тренировки. */
fun dayWorkoutXp(state: AppState, date: LocalDate): Int =
    state.workouts.filter { it.date == date }.sumOf { workoutXp(it.exerciseId, it.sets, it.durationMin) }
