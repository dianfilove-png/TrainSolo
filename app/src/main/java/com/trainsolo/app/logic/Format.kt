package com.trainsolo.app.logic

import com.trainsolo.app.model.Exercise
import com.trainsolo.app.model.ExerciseKind
import java.time.LocalDate
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/** 2.5 → «2,5», 60.0 → «60». */
fun num(x: Double): String {
    val r = (x * 10).roundToLong() / 10.0
    return if (r == r.toLong().toDouble()) r.toLong().toString() else r.toString().replace('.', ',')
}

/** Целое с пробелами между разрядами: 10000 → «10 000». */
fun grouped(n: Int): String = String.format(Locale.forLanguageTag("ru"), "%,d", n).replace(' ', ' ').replace(' ', ' ')

/** «4×8 · 60 кг», «3×30 сек», «30 мин · 5 км». */
fun formatAmount(ex: Exercise, sets: Int, reps: Int, weight: Double, durationMin: Int, distanceKm: Double): String {
    if (ex.kind == ExerciseKind.CARDIO) {
        return if (distanceKm > 0) "$durationMin мин · ${num(distanceKm)} км" else "$durationMin мин"
    }
    val base = "$sets×$reps" + if (ex.seconds) " сек" else ""
    return if (ex.kind == ExerciseKind.STRENGTH && weight > 0) "$base · ${num(weight)} кг" else base
}

/** Склонение: plural(3, "день", "дня", "дней"). */
fun plural(n: Int, one: String, few: String, many: String): String {
    val a = abs(n) % 100
    val last = a % 10
    return when {
        a in 11..19 -> many
        last in 2..4 -> few
        last == 1 -> one
        else -> many
    }
}

private val MONTHS = listOf("янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")
private val WEEKDAYS = listOf("пн", "вт", "ср", "чт", "пт", "сб", "вс")

/** «Сегодня», «Вчера» или «чт, 8 окт». */
fun formatDay(date: LocalDate, today: LocalDate): String = when (daysBetween(date, today)) {
    0 -> "Сегодня"
    1 -> "Вчера"
    else -> "${WEEKDAYS[date.dayOfWeek.value - 1]}, ${date.dayOfMonth} ${MONTHS[date.monthValue - 1]}"
}

fun Double.roundInt(): Int = roundToInt()
