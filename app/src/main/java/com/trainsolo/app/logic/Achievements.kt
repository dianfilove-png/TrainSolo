package com.trainsolo.app.logic

import com.trainsolo.app.model.Muscle
import com.trainsolo.app.model.WorkoutEntry

data class Achievement(
    val id: String,
    val emoji: String,
    val title: String,
    val description: String,
    val current: Double,
    val target: Double,
) {
    val done: Boolean get() = current >= target
}

/** Выполнено ли «Ежедневное задание Охотника» хотя бы в один день. */
fun hunterQuestDone(workouts: List<WorkoutEntry>): Boolean =
    workouts.groupBy { it.date }.values.any { day ->
        fun reps(vararg ids: String) = day.filter { it.exerciseId in ids }.sumOf { it.sets * it.reps }
        reps("push_ups") >= 100 && reps("sit_ups") >= 100 && reps("bw_squat", "squat") >= 100 &&
            day.filter { it.exerciseId == "running" }.sumOf { it.distanceKm } >= 10
    }

fun achievements(t: Totals, level: Int, workouts: List<WorkoutEntry>): List<Achievement> {
    fun a(id: String, emoji: String, title: String, desc: String, current: Number, target: Number) =
        Achievement(id, emoji, title, desc, minOf(current.toDouble(), target.toDouble()), target.toDouble())

    return listOf(
        a("first", "🩸", "Первая кровь", "Запиши первую тренировку", t.workoutDays, 1),
        a("days10", "📅", "Десятка", "10 тренировочных дней", t.workoutDays, 10),
        a("sets100", "💯", "Сотня подходов", "Сделай 100 подходов", t.totalSets, 100),
        a("ton10", "🏗️", "Десять тонн", "Подними суммарно 10 000 кг", t.tonnage, 10000),
        a("streak7", "🔥", "Неделя в строю", "7 активных дней подряд", t.bestStreak, 7),
        a("food7", "🥗", "Дневник питания", "Записывай еду 7 дней", t.foodDays, 7),
        a("anatomy", "🧠", "Анатом", "Нагрузи все группы мышц", t.musclesTrained.size, Muscle.entries.size),
        a("marathon", "🏃", "Марафонец", "Пробеги суммарно 42,2 км", t.cardioKm, 42.2),
        a("rankC", "⭐", "Ранг C", "Достигни 10 уровня", level, 10),
        a("hunter", "🗡️", "Задание Системы", "100 отжиманий, 100 подъёмов корпуса, 100 приседаний и 10 км за день", if (hunterQuestDone(workouts)) 1 else 0, 1),
    )
}
