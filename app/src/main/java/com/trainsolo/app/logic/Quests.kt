package com.trainsolo.app.logic

import com.trainsolo.app.data.exercise
import com.trainsolo.app.model.ExerciseKind
import com.trainsolo.app.model.FoodEntry
import com.trainsolo.app.model.Meal
import com.trainsolo.app.model.Muscle
import com.trainsolo.app.model.QuestClaim
import com.trainsolo.app.model.WorkoutEntry
import java.time.LocalDate
import kotlin.math.roundToInt

data class DayContext(val workouts: List<WorkoutEntry>, val foods: List<FoodEntry>, val goals: Goals)

class QuestDef(
    val id: String,
    val title: String,
    val xp: Int,
    val unit: String,
    val target: (DayContext) -> Int,
    val measure: (DayContext) -> Int,
)

private fun repsOf(ctx: DayContext, ids: Set<String>) =
    ctx.workouts.filter { it.exerciseId in ids }.sumOf { it.sets * it.reps }

private fun kindOf(w: WorkoutEntry) = exercise(w.exerciseId)?.kind

private val BASE_QUEST = QuestDef("workout", "Выполни тренировку", 50, "", { 1 }, { if (it.workouts.isNotEmpty()) 1 else 0 })

private val TRAINING_QUESTS = listOf(
    QuestDef("sets12", "Сделай 12 подходов", 60, "подх.", { 12 }) { ctx ->
        ctx.workouts.filter { kindOf(it) != ExerciseKind.CARDIO }.sumOf { it.sets }
    },
    QuestDef("cardio20", "Кардио 20 минут", 50, "мин", { 20 }) { ctx ->
        ctx.workouts.filter { kindOf(it) == ExerciseKind.CARDIO }.sumOf { it.durationMin }
    },
    QuestDef("pushups50", "50 отжиманий", 50, "раз", { 50 }) { repsOf(it, setOf("push_ups")) },
    QuestDef("squats50", "50 приседаний", 50, "раз", { 50 }) { repsOf(it, setOf("bw_squat", "squat")) },
    QuestDef("muscles4", "Нагрузи 4 группы мышц", 50, "гр.", { 4 }) { ctx ->
        ctx.workouts.flatMap { exercise(it.exerciseId)?.primary.orEmpty() }.toSet().size
    },
    QuestDef("core3", "3 подхода на пресс", 40, "подх.", { 3 }) { ctx ->
        ctx.workouts.filter { w ->
            val ex = exercise(w.exerciseId)
            ex != null && ex.kind != ExerciseKind.CARDIO && ex.primary.any { it == Muscle.ABS || it == Muscle.OBLIQUES }
        }.sumOf { it.sets }
    },
)

private val NUTRITION_QUESTS = listOf(
    QuestDef("meals3", "Запиши 3 приёма пищи", 40, "", { 3 }) { ctx -> ctx.foods.map { it.meal }.toSet().size },
    QuestDef("protein", "Набери норму белка", 50, "г", { it.goals.protein }) { ctx -> sumFoods(ctx.foods).protein.roundToInt() },
    QuestDef("calories", "Попади в норму калорий ±10%", 60, "", { 1 }) { ctx ->
        if (sumFoods(ctx.foods).calories / ctx.goals.calories in 0.9..1.1) 1 else 0
    },
    QuestDef("breakfast", "Не пропусти завтрак", 30, "", { 1 }) { ctx ->
        if (ctx.foods.any { it.meal == Meal.BREAKFAST }) 1 else 0
    },
)

private fun hash(s: String): Long {
    var h = 5381L
    for (ch in s) h = ((h shl 5) + h + ch.code) and 0xFFFFFFFFL
    return h
}

/** Три задания на день: тренировка + тренировочное + по питанию. Для одной даты всегда одинаковы. */
fun questsForDate(date: LocalDate): List<QuestDef> {
    val h = hash(date.toString())
    return listOf(
        BASE_QUEST,
        TRAINING_QUESTS[(h % TRAINING_QUESTS.size).toInt()],
        NUTRITION_QUESTS[((h / 7) % NUTRITION_QUESTS.size).toInt()],
    )
}

data class QuestStatus(val def: QuestDef, val current: Int, val target: Int, val claimed: Boolean) {
    val done: Boolean get() = current >= target
}

fun questStatuses(date: LocalDate, ctx: DayContext, claims: List<QuestClaim>): List<QuestStatus> =
    questsForDate(date).map { def ->
        QuestStatus(
            def = def,
            current = def.measure(ctx),
            target = maxOf(1, def.target(ctx)),
            claimed = claims.any { it.date == date && it.questId == def.id },
        )
    }
