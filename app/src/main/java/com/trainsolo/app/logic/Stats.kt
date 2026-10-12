package com.trainsolo.app.logic

import com.trainsolo.app.data.exercise
import com.trainsolo.app.model.AppState
import com.trainsolo.app.model.ExerciseKind
import com.trainsolo.app.model.Muscle
import java.time.LocalDate
import kotlin.math.roundToInt

data class Totals(
    val workoutDays: Int,
    val totalSets: Int,
    /** Суммарный тоннаж (подходы × повторы × вес), кг. */
    val tonnage: Int,
    val cardioMinutes: Int,
    val cardioKm: Double,
    val foodDays: Int,
    /** Дни, когда калории попали в коридор 90–110% от нормы. */
    val onTargetDays: Int,
    val activeDays: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val musclesTrained: Set<Muscle>,
)

fun computeTotals(state: AppState, today: LocalDate): Totals {
    val workoutDates = HashSet<LocalDate>()
    val muscles = HashSet<Muscle>()
    var sets = 0
    var tonnage = 0.0
    var cardioMin = 0
    var cardioKm = 0.0
    for (w in state.workouts) {
        val ex = exercise(w.exerciseId) ?: continue
        workoutDates += w.date
        muscles += ex.primary
        if (ex.kind == ExerciseKind.CARDIO) {
            cardioMin += w.durationMin
            cardioKm += w.distanceKm
        } else {
            sets += w.sets
            if (ex.kind == ExerciseKind.STRENGTH) tonnage += w.sets * w.reps * w.weight
        }
    }

    val foodsByDate = state.foods.groupBy { it.date }
    val goal = nutritionGoals(state.profile).calories
    val onTarget = foodsByDate.values.count { (sumFoods(it).calories / goal) in 0.9..1.1 }

    val active = workoutDates + foodsByDate.keys
    return Totals(
        workoutDays = workoutDates.size,
        totalSets = sets,
        tonnage = tonnage.roundToInt(),
        cardioMinutes = cardioMin,
        cardioKm = (cardioKm * 10).roundToInt() / 10.0,
        foodDays = foodsByDate.size,
        onTargetDays = onTarget,
        activeDays = active.size,
        currentStreak = currentStreak(active, today),
        bestStreak = bestStreak(active),
        musclesTrained = muscles,
    )
}

/** Серия подряд идущих активных дней. Если сегодня ещё ничего не записано, серия не сгорает. */
fun currentStreak(active: Set<LocalDate>, today: LocalDate): Int {
    var day = if (today in active) today else today.minusDays(1)
    var streak = 0
    while (day in active) {
        streak++
        day = day.minusDays(1)
    }
    return streak
}

fun bestStreak(active: Set<LocalDate>): Int {
    var best = 0
    var run = 0
    var prev: LocalDate? = null
    for (d in active.sorted()) {
        run = if (prev != null && daysBetween(prev, d) == 1) run + 1 else 1
        best = maxOf(best, run)
        prev = d
    }
    return best
}

data class CharacterStat(val label: String, val value: Int, val color: Long, val hint: String)

fun characterStats(t: Totals): List<CharacterStat> = listOf(
    CharacterStat("СИЛ", 1 + t.totalSets / 20, 0xFFF87171, "+1 за каждые 20 подходов"),
    CharacterStat("ВЫН", 1 + t.cardioMinutes / 30, 0xFF38BDF8, "+1 за каждые 30 минут кардио"),
    CharacterStat("ДИС", 1 + t.activeDays / 3, 0xFFA78BFA, "+1 за каждые 3 активных дня"),
    CharacterStat("ЖИЗ", 1 + t.onTargetDays, 0xFF34D399, "+1 за день в норме калорий"),
)
