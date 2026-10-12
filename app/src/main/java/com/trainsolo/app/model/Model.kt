package com.trainsolo.app.model

import java.time.LocalDate

enum class Muscle(val title: String) {
    CHEST("Грудь"),
    SHOULDERS("Плечи"),
    BICEPS("Бицепс"),
    TRICEPS("Трицепс"),
    FOREARMS("Предплечья"),
    ABS("Пресс"),
    OBLIQUES("Косые мышцы"),
    TRAPS("Трапеции"),
    LATS("Широчайшие"),
    LOWER_BACK("Поясница"),
    GLUTES("Ягодицы"),
    QUADS("Квадрицепсы"),
    HAMSTRINGS("Бицепс бедра"),
    CALVES("Икры"),
}

/** Как «Глаз Системы» засчитывает повторы. */
enum class SensorMode {
    /** Датчик приближения: грудь касается телефона на полу. */
    PROXIMITY,
    /** Телефон в переднем кармане: угол бедра. */
    THIGH_ANGLE,
    /** Телефон прижат к груди: угол корпуса. */
    TORSO_ANGLE,
}

/** STRENGTH — подходы × повторы × вес; BODYWEIGHT — подходы × повторы; CARDIO — минуты (+ км). */
enum class ExerciseKind { STRENGTH, BODYWEIGHT, CARDIO }

data class Exercise(
    val id: String,
    val name: String,
    val kind: ExerciseKind,
    val primary: List<Muscle>,
    val secondary: List<Muscle>,
    /** Для статических упражнений (планка) повторы — это секунды. */
    val seconds: Boolean = false,
    /** Можно выполнить дома без тренажёров. */
    val home: Boolean = false,
    /** Можно выполнять «под надзором Системы». */
    val sensor: SensorMode? = null,
)

data class WorkoutEntry(
    val id: String,
    val date: LocalDate,
    val exerciseId: String,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val durationMin: Int,
    val distanceKm: Double,
    val createdAt: Long,
    /** Повторы, подтверждённые датчиками телефона. */
    val verifiedReps: Int = 0,
)

enum class Meal(val title: String, val emoji: String) {
    BREAKFAST("Завтрак", "🍳"),
    LUNCH("Обед", "🍲"),
    DINNER("Ужин", "🍽️"),
    SNACK("Перекус", "🍎"),
}

data class FoodEntry(
    val id: String,
    val date: LocalDate,
    val meal: Meal,
    val name: String,
    val grams: Double,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val createdAt: Long,
)

enum class Sex(val title: String) { MALE("Мужчина"), FEMALE("Женщина") }

enum class Activity(val title: String, val factor: Double) {
    LOW("Сидячий образ жизни", 1.2),
    LIGHT("1–2 тренировки в неделю", 1.375),
    MODERATE("3–4 тренировки в неделю", 1.55),
    HIGH("5–6 тренировок в неделю", 1.725),
    EXTREME("Каждый день / тяжёлый труд", 1.9),
}

enum class Goal(val title: String, val factor: Double, val proteinPerKg: Double) {
    LOSE("Похудеть", 0.85, 2.0),
    MAINTAIN("Поддерживать форму", 1.0, 1.6),
    GAIN("Набрать массу", 1.1, 1.8),
}

data class Profile(
    val name: String = "Охотник",
    val sex: Sex = Sex.MALE,
    val age: Int = 25,
    val heightCm: Double = 178.0,
    val weightKg: Double = 75.0,
    val activity: Activity = Activity.MODERATE,
    val goal: Goal = Goal.MAINTAIN,
    /** Ручная норма калорий; null — считать автоматически. */
    val customCalories: Int? = null,
)

data class QuestClaim(val date: LocalDate, val questId: String, val xp: Int)

data class AppState(
    val profile: Profile = Profile(),
    val workouts: List<WorkoutEntry> = emptyList(),
    val foods: List<FoodEntry> = emptyList(),
    val questClaims: List<QuestClaim> = emptyList(),
    val activeProgramId: String? = null,
    /** Индекс следующего дня для каждой программы. */
    val programProgress: Map<String, Int> = emptyMap(),
)

data class Planned(
    val exerciseId: String,
    val sets: Int,
    val reps: Int = 0,
    val weight: Double = 0.0,
    val durationMin: Int = 0,
    val distanceKm: Double = 0.0,
)

data class ProgramDay(val name: String, val exercises: List<Planned>)

enum class ProgramLevel(val title: String) { BEGINNER("Новичок"), INTERMEDIATE("Средний"), HARDCORE("Хардкор") }

data class Program(
    val id: String,
    val name: String,
    val emoji: String,
    val level: ProgramLevel,
    val daysPerWeek: String,
    val description: String,
    val days: List<ProgramDay>,
    /** Программа для дома — без зала и штанги. */
    val home: Boolean = false,
)
