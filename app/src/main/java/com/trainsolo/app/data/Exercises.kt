package com.trainsolo.app.data

import com.trainsolo.app.model.Exercise
import com.trainsolo.app.model.ExerciseKind.BODYWEIGHT
import com.trainsolo.app.model.ExerciseKind.CARDIO
import com.trainsolo.app.model.ExerciseKind.STRENGTH
import com.trainsolo.app.model.Muscle.ABS
import com.trainsolo.app.model.Muscle.BICEPS
import com.trainsolo.app.model.Muscle.CALVES
import com.trainsolo.app.model.Muscle.CHEST
import com.trainsolo.app.model.Muscle.FOREARMS
import com.trainsolo.app.model.Muscle.GLUTES
import com.trainsolo.app.model.Muscle.HAMSTRINGS
import com.trainsolo.app.model.Muscle.LATS
import com.trainsolo.app.model.Muscle.LOWER_BACK
import com.trainsolo.app.model.Muscle.OBLIQUES
import com.trainsolo.app.model.Muscle.QUADS
import com.trainsolo.app.model.Muscle.SHOULDERS
import com.trainsolo.app.model.Muscle.TRAPS
import com.trainsolo.app.model.Muscle.TRICEPS

val EXERCISES: List<Exercise> = listOf(
    // Грудь
    Exercise("bench_press", "Жим штанги лёжа", STRENGTH, listOf(CHEST), listOf(TRICEPS, SHOULDERS)),
    Exercise("incline_db_press", "Жим гантелей на наклонной", STRENGTH, listOf(CHEST, SHOULDERS), listOf(TRICEPS)),
    Exercise("dumbbell_fly", "Разводка гантелей", STRENGTH, listOf(CHEST), listOf(SHOULDERS)),
    Exercise("push_ups", "Отжимания", BODYWEIGHT, listOf(CHEST, TRICEPS), listOf(SHOULDERS, ABS)),
    Exercise("dips", "Отжимания на брусьях", BODYWEIGHT, listOf(TRICEPS, CHEST), listOf(SHOULDERS)),

    // Плечи
    Exercise("overhead_press", "Жим штанги стоя", STRENGTH, listOf(SHOULDERS), listOf(TRICEPS, TRAPS)),
    Exercise("db_shoulder_press", "Жим гантелей сидя", STRENGTH, listOf(SHOULDERS), listOf(TRICEPS)),
    Exercise("lateral_raise", "Махи гантелями в стороны", STRENGTH, listOf(SHOULDERS), listOf(TRAPS)),
    Exercise("face_pull", "Тяга каната к лицу", STRENGTH, listOf(SHOULDERS, TRAPS), emptyList()),

    // Спина
    Exercise("deadlift", "Становая тяга", STRENGTH, listOf(LOWER_BACK, GLUTES, HAMSTRINGS), listOf(TRAPS, FOREARMS, QUADS, LATS)),
    Exercise("barbell_row", "Тяга штанги в наклоне", STRENGTH, listOf(LATS, TRAPS), listOf(BICEPS, LOWER_BACK, SHOULDERS)),
    Exercise("pull_ups", "Подтягивания", BODYWEIGHT, listOf(LATS, BICEPS), listOf(FOREARMS, TRAPS)),
    Exercise("lat_pulldown", "Тяга верхнего блока", STRENGTH, listOf(LATS), listOf(BICEPS)),
    Exercise("seated_row", "Тяга нижнего блока", STRENGTH, listOf(LATS, TRAPS), listOf(BICEPS)),
    Exercise("shrugs", "Шраги", STRENGTH, listOf(TRAPS), listOf(FOREARMS)),
    Exercise("hyperextension", "Гиперэкстензия", BODYWEIGHT, listOf(LOWER_BACK), listOf(GLUTES, HAMSTRINGS)),

    // Руки
    Exercise("barbell_curl", "Подъём штанги на бицепс", STRENGTH, listOf(BICEPS), listOf(FOREARMS)),
    Exercise("hammer_curl", "Молотки с гантелями", STRENGTH, listOf(BICEPS, FOREARMS), emptyList()),
    Exercise("triceps_pushdown", "Разгибания на блоке", STRENGTH, listOf(TRICEPS), emptyList()),
    Exercise("french_press", "Французский жим", STRENGTH, listOf(TRICEPS), emptyList()),
    Exercise("wrist_curl", "Сгибания запястий", STRENGTH, listOf(FOREARMS), emptyList()),

    // Ноги
    Exercise("squat", "Приседания со штангой", STRENGTH, listOf(QUADS, GLUTES), listOf(HAMSTRINGS, LOWER_BACK, ABS)),
    Exercise("bw_squat", "Приседания", BODYWEIGHT, listOf(QUADS, GLUTES), listOf(HAMSTRINGS)),
    Exercise("leg_press", "Жим ногами", STRENGTH, listOf(QUADS, GLUTES), listOf(HAMSTRINGS)),
    Exercise("lunges", "Выпады", BODYWEIGHT, listOf(QUADS, GLUTES), listOf(HAMSTRINGS, CALVES)),
    Exercise("romanian_deadlift", "Румынская тяга", STRENGTH, listOf(HAMSTRINGS, GLUTES), listOf(LOWER_BACK)),
    Exercise("leg_curl", "Сгибание ног в тренажёре", STRENGTH, listOf(HAMSTRINGS), emptyList()),
    Exercise("leg_extension", "Разгибание ног в тренажёре", STRENGTH, listOf(QUADS), emptyList()),
    Exercise("hip_thrust", "Ягодичный мост со штангой", STRENGTH, listOf(GLUTES), listOf(HAMSTRINGS)),
    Exercise("glute_bridge", "Ягодичный мостик", BODYWEIGHT, listOf(GLUTES), listOf(HAMSTRINGS)),
    Exercise("calf_raise", "Подъём на носки", STRENGTH, listOf(CALVES), emptyList()),

    // Кор
    Exercise("sit_ups", "Подъёмы корпуса", BODYWEIGHT, listOf(ABS), listOf(OBLIQUES)),
    Exercise("plank", "Планка", BODYWEIGHT, listOf(ABS), listOf(OBLIQUES, SHOULDERS, LOWER_BACK), seconds = true),
    Exercise("leg_raises", "Подъём ног в висе", BODYWEIGHT, listOf(ABS), listOf(OBLIQUES, FOREARMS)),
    Exercise("russian_twist", "Русские скручивания", BODYWEIGHT, listOf(OBLIQUES), listOf(ABS)),
    Exercise("burpees", "Бёрпи", BODYWEIGHT, listOf(QUADS, CHEST), listOf(SHOULDERS, ABS, TRICEPS)),

    // Кардио
    Exercise("running", "Бег", CARDIO, listOf(QUADS, CALVES), listOf(HAMSTRINGS, GLUTES)),
    Exercise("cycling", "Велосипед", CARDIO, listOf(QUADS), listOf(CALVES, HAMSTRINGS, GLUTES)),
    Exercise("jump_rope", "Скакалка", CARDIO, listOf(CALVES), listOf(SHOULDERS, FOREARMS)),
    Exercise("rowing", "Гребной тренажёр", CARDIO, listOf(LATS, QUADS), listOf(BICEPS, HAMSTRINGS, LOWER_BACK)),
    Exercise("swimming", "Плавание", CARDIO, listOf(LATS, SHOULDERS), listOf(TRICEPS, CHEST, ABS)),
    Exercise("walking", "Ходьба", CARDIO, listOf(CALVES), listOf(QUADS, GLUTES)),
)

private val BY_ID = EXERCISES.associateBy { it.id }

fun exercise(id: String): Exercise? = BY_ID[id]
