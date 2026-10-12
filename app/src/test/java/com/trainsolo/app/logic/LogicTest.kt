package com.trainsolo.app.logic

import com.trainsolo.app.model.AppState
import com.trainsolo.app.model.FoodEntry
import com.trainsolo.app.model.Goal
import com.trainsolo.app.model.Meal
import com.trainsolo.app.model.Muscle
import com.trainsolo.app.model.Profile
import com.trainsolo.app.model.QuestClaim
import com.trainsolo.app.model.WorkoutEntry
import com.trainsolo.app.store.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LogicTest {
    private val today = LocalDate.of(2026, 10, 12)
    private var n = 0

    private fun w(date: LocalDate, id: String, sets: Int = 3, reps: Int = 10, weight: Double = 0.0, min: Int = 0, km: Double = 0.0) =
        WorkoutEntry("w${n++}", date, id, sets, reps, weight, min, km, 0)

    private fun f(date: LocalDate, kcal: Double = 500.0, meal: Meal = Meal.LUNCH) =
        FoodEntry("f${n++}", date, meal, "Еда", 100.0, kcal, 30.0, 10.0, 60.0, 0)

    @Test fun `сегодня красный, вчера жёлтый, потом зелёный`() {
        assertEquals(MuscleStatus.LOADED, statusForDays(0))
        assertEquals(MuscleStatus.RECOVERING, statusForDays(1))
        assertEquals(MuscleStatus.FRESH, statusForDays(2))
        assertEquals(MuscleStatus.FRESH, statusForDays(null))
    }

    @Test fun `без тренировок все мышцы зелёные`() {
        assertTrue(muscleStates(emptyList(), today).values.all { it.status == MuscleStatus.FRESH })
    }

    @Test fun `жим лёжа нагружает грудь, трицепс и плечи`() {
        val s = muscleStates(listOf(w(today, "bench_press", sets = 4)), today)
        assertEquals(MuscleStatus.LOADED, s.getValue(Muscle.CHEST).status)
        assertEquals(4.0, s.getValue(Muscle.CHEST).sets, 0.0)
        assertEquals(2.0, s.getValue(Muscle.TRICEPS).sets, 0.0)
        assertEquals(MuscleStatus.LOADED, s.getValue(Muscle.SHOULDERS).status)
        assertEquals(MuscleStatus.FRESH, s.getValue(Muscle.QUADS).status)
    }

    @Test fun `вчера жёлтый, позавчера зелёный, будущее игнорируется`() {
        val s = muscleStates(listOf(w(today.minusDays(1), "squat"), w(today.minusDays(2), "pull_ups"), w(today.plusDays(1), "calf_raise")), today)
        assertEquals(MuscleStatus.RECOVERING, s.getValue(Muscle.QUADS).status)
        assertEquals(MuscleStatus.FRESH, s.getValue(Muscle.LATS).status)
        assertEquals(2, s.getValue(Muscle.LATS).daysSince)
        assertEquals(MuscleStatus.FRESH, s.getValue(Muscle.CALVES).status)
    }

    @Test fun `опыт и уровни`() {
        assertEquals(40, workoutXp("bench_press", 4, 0))
        assertEquals(60, workoutXp("running", 1, 30))
        assertEquals(30, totalXp(AppState(foods = List(10) { f(today) })))
        assertEquals(0, xpForLevel(1)); assertEquals(100, xpForLevel(2)); assertEquals(250, xpForLevel(3))
        assertEquals(1, levelInfo(99).level)
        assertEquals(3, levelInfo(300).level); assertEquals(50, levelInfo(300).current)
        assertEquals("E", rankForLevel(1).letter); assertEquals("D", rankForLevel(5).letter); assertEquals("S", rankForLevel(60).letter)
    }

    @Test fun `норма калорий`() {
        assertEquals(2700, autoCalories(Profile()))
        assertEquals(2300, autoCalories(Profile(goal = Goal.LOSE)))
        assertEquals(2000, nutritionGoals(Profile(customCalories = 2000)).calories)
    }

    @Test fun `серии и итоги`() {
        val s = AppState(
            workouts = listOf(w(today.minusDays(1), "bench_press", 4, 8, 60.0), w(today, "running", 1, 0, 0.0, 30, 5.0)),
            foods = listOf(f(today.minusDays(2), 2700.0)),
        )
        val t = computeTotals(s, today)
        assertEquals(2, t.workoutDays); assertEquals(4, t.totalSets); assertEquals(1920, t.tonnage)
        assertEquals(3, t.currentStreak); assertEquals(3, t.bestStreak); assertEquals(1, t.onTargetDays)
        assertEquals(0, currentStreak(setOf(today.minusDays(2)), today))
    }

    @Test fun `задания на день стабильны`() {
        assertEquals(questsForDate(today).map { it.id }, questsForDate(today).map { it.id })
        assertEquals("workout", questsForDate(today).first().id)
        val st = questStatuses(today, DayContext(listOf(w(today, "squat")), emptyList(), nutritionGoals(Profile())), listOf(QuestClaim(today, "workout", 50)))
        assertTrue(st.first().done && st.first().claimed)
    }

    @Test fun `задание Охотника`() {
        val day = listOf(w(today, "push_ups", 10, 10), w(today, "sit_ups", 10, 10), w(today, "bw_squat", 10, 10))
        assertFalse(hunterQuestDone(day))
        assertTrue(hunterQuestDone(day + w(today, "running", 1, 0, 0.0, 60, 10.0)))
    }

    @Test fun `форматирование`() {
        assertEquals("Сегодня", formatDay(today, today))
        assertEquals("чт, 8 окт", formatDay(LocalDate.of(2026, 10, 8), today))
        assertEquals("дня", plural(22, "день", "дня", "дней"))
        assertEquals("дней", plural(11, "день", "дня", "дней"))
        assertEquals("2,5", num(2.5)); assertEquals("60", num(60.0))
    }

    @Test fun `сохранение и загрузка`() {
        val s = AppState(
            profile = Profile(name = "Дима", customCalories = 2500),
            workouts = listOf(w(today, "squat", 4, 8, 70.0)),
            foods = listOf(f(today, meal = Meal.DINNER)),
            questClaims = listOf(QuestClaim(today, "workout", 50)),
            activeProgramId = "ppl",
            programProgress = mapOf("ppl" to 2),
        )
        assertEquals(s, Json.decode(Json.encode(s)))
        assertEquals(AppState(), Json.decode(Json.encode(AppState())))
    }
}
