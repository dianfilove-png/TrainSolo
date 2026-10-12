package com.trainsolo.app.logic

import com.trainsolo.app.data.exercise
import com.trainsolo.app.model.Muscle
import com.trainsolo.app.model.WorkoutEntry
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** LOADED — тренировалась сегодня (красная), RECOVERING — вчера (жёлтая), FRESH — восстановилась (зелёная). */
enum class MuscleStatus(val label: String, val short: String) {
    LOADED("Нагружена сегодня", "Сегодня"),
    RECOVERING("Восстанавливается", "Вчера"),
    FRESH("Готова к нагрузке", "Готовы"),
}

data class MuscleState(
    val status: MuscleStatus,
    val lastTrained: LocalDate?,
    val daysSince: Int?,
    /** Подходы за последний день нагрузки: целевые мышцы — 1, вспомогательные — 0.5. */
    val sets: Double,
)

fun statusForDays(daysSince: Int?): MuscleStatus = when (daysSince) {
    0 -> MuscleStatus.LOADED
    1 -> MuscleStatus.RECOVERING
    else -> MuscleStatus.FRESH
}

fun daysBetween(from: LocalDate, to: LocalDate): Int = ChronoUnit.DAYS.between(from, to).toInt()

fun muscleStates(workouts: List<WorkoutEntry>, today: LocalDate): Map<Muscle, MuscleState> {
    val lastDate = HashMap<Muscle, LocalDate>()
    val lastSets = HashMap<Muscle, Double>()

    fun touch(muscle: Muscle, date: LocalDate, sets: Double) {
        val prev = lastDate[muscle]
        when {
            prev == null || date > prev -> {
                lastDate[muscle] = date
                lastSets[muscle] = sets
            }
            date == prev -> lastSets[muscle] = lastSets.getValue(muscle) + sets
        }
    }

    for (entry in workouts) {
        if (entry.date > today) continue
        val ex = exercise(entry.exerciseId) ?: continue
        val sets = maxOf(1, entry.sets).toDouble()
        ex.primary.forEach { touch(it, entry.date, sets) }
        ex.secondary.forEach { touch(it, entry.date, sets / 2) }
    }

    return Muscle.entries.associateWith { m ->
        val date = lastDate[m]
        val days = date?.let { daysBetween(it, today) }
        MuscleState(statusForDays(days), date, days, lastSets[m] ?: 0.0)
    }
}
