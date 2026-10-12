package com.trainsolo.app.logic

import com.trainsolo.app.data.FoodPreset
import com.trainsolo.app.model.FoodEntry
import com.trainsolo.app.model.Profile
import com.trainsolo.app.model.Sex
import kotlin.math.roundToInt

data class Macros(val calories: Double, val protein: Double, val fat: Double, val carbs: Double)

/** Базовый обмен по формуле Миффлина — Сан Жеора. */
fun bmr(p: Profile): Double {
    val base = 10 * p.weightKg + 6.25 * p.heightCm - 5 * p.age
    return if (p.sex == Sex.MALE) base + 5 else base - 161
}

fun autoCalories(p: Profile): Int = (bmr(p) * p.activity.factor * p.goal.factor / 10).roundToInt() * 10

data class Goals(val calories: Int, val protein: Int, val fat: Int, val carbs: Int)

fun nutritionGoals(p: Profile): Goals {
    val calories = p.customCalories?.takeIf { it > 0 } ?: autoCalories(p)
    val protein = (p.weightKg * p.goal.proteinPerKg).roundToInt()
    val fat = (calories * 0.25 / 9).roundToInt()
    val carbs = maxOf(0, ((calories - protein * 4 - fat * 9) / 4.0).roundToInt())
    return Goals(calories, protein, fat, carbs)
}

fun sumFoods(foods: List<FoodEntry>): Macros = Macros(
    foods.sumOf { it.calories },
    foods.sumOf { it.protein },
    foods.sumOf { it.fat },
    foods.sumOf { it.carbs },
)

private fun round1(x: Double) = (x * 10).roundToInt() / 10.0

fun scalePreset(preset: FoodPreset, grams: Double): Macros {
    val k = grams / 100
    return Macros(
        (preset.calories * k).roundToInt().toDouble(),
        round1(preset.protein * k),
        round1(preset.fat * k),
        round1(preset.carbs * k),
    )
}
