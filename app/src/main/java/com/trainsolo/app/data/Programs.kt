package com.trainsolo.app.data

import com.trainsolo.app.model.Planned
import com.trainsolo.app.model.Program
import com.trainsolo.app.model.ProgramDay
import com.trainsolo.app.model.ProgramLevel

private fun s(id: String, sets: Int, reps: Int, weight: Double = 0.0) = Planned(id, sets, reps = reps, weight = weight)
private fun c(id: String, minutes: Int, km: Double = 0.0) = Planned(id, 1, durationMin = minutes, distanceKm = km)

val PROGRAMS: List<Program> = listOf(
    Program(
        "fullbody_beginner", "Full Body для новичка", "🌱", ProgramLevel.BEGINNER, "3 дня в неделю",
        "Всё тело за одну тренировку. Базовые движения, умеренный объём — идеально, чтобы втянуться и выучить технику.",
        listOf(
            ProgramDay("День A", listOf(s("squat", 3, 10, 40.0), s("bench_press", 3, 10, 40.0), s("barbell_row", 3, 10, 30.0), s("plank", 3, 30))),
            ProgramDay("День B", listOf(s("romanian_deadlift", 3, 10, 40.0), s("overhead_press", 3, 10, 25.0), s("lat_pulldown", 3, 10, 35.0), s("lunges", 3, 10), s("sit_ups", 3, 15))),
            ProgramDay("День C", listOf(s("leg_press", 3, 12, 80.0), s("incline_db_press", 3, 10, 14.0), s("seated_row", 3, 12, 35.0), s("barbell_curl", 2, 12, 20.0), s("triceps_pushdown", 2, 12, 20.0))),
        ),
    ),
    Program(
        "ppl", "Push / Pull / Legs", "⚔️", ProgramLevel.INTERMEDIATE, "3–6 дней в неделю",
        "Классический сплит: жимы, тяги и ноги в разные дни. Каждая группа успевает восстановиться — следи, чтобы на аватаре она снова стала зелёной.",
        listOf(
            ProgramDay("Push — жимы", listOf(s("bench_press", 4, 8, 60.0), s("overhead_press", 3, 10, 35.0), s("incline_db_press", 3, 10, 20.0), s("lateral_raise", 3, 15, 8.0), s("triceps_pushdown", 3, 12, 25.0))),
            ProgramDay("Pull — тяги", listOf(s("deadlift", 3, 5, 80.0), s("pull_ups", 4, 8), s("barbell_row", 3, 10, 50.0), s("face_pull", 3, 15, 15.0), s("barbell_curl", 3, 12, 25.0), s("hammer_curl", 3, 12, 12.0))),
            ProgramDay("Legs — ноги", listOf(s("squat", 4, 8, 70.0), s("romanian_deadlift", 3, 10, 60.0), s("leg_press", 3, 12, 120.0), s("leg_curl", 3, 12, 30.0), s("calf_raise", 4, 15, 40.0))),
        ),
    ),
    Program(
        "upper_lower", "Верх / Низ", "🏋️", ProgramLevel.INTERMEDIATE, "4 дня в неделю",
        "Два дня на верх тела и два на низ. Хороший баланс силы и объёма.",
        listOf(
            ProgramDay("Верх A", listOf(s("bench_press", 4, 6, 65.0), s("barbell_row", 4, 8, 55.0), s("db_shoulder_press", 3, 10, 18.0), s("lat_pulldown", 3, 10, 50.0), s("barbell_curl", 2, 12, 25.0), s("french_press", 2, 12, 20.0))),
            ProgramDay("Низ A", listOf(s("squat", 4, 6, 80.0), s("romanian_deadlift", 3, 8, 70.0), s("leg_extension", 3, 12, 40.0), s("calf_raise", 4, 12, 50.0), s("leg_raises", 3, 12))),
            ProgramDay("Верх B", listOf(s("overhead_press", 4, 6, 40.0), s("pull_ups", 4, 8), s("incline_db_press", 3, 10, 22.0), s("seated_row", 3, 10, 55.0), s("lateral_raise", 3, 15, 8.0), s("shrugs", 3, 12, 60.0))),
            ProgramDay("Низ B", listOf(s("deadlift", 3, 5, 100.0), s("leg_press", 3, 10, 140.0), s("leg_curl", 3, 12, 35.0), s("hip_thrust", 3, 10, 80.0), s("russian_twist", 3, 20))),
        ),
    ),
    Program(
        "home_bodyweight", "Дома без железа", "🏠", ProgramLevel.BEGINNER, "3 дня в неделю",
        "Только вес собственного тела. Нужен коврик и немного места — никаких оправданий.",
        listOf(
            ProgramDay("Круг 1", listOf(s("push_ups", 4, 12), s("bw_squat", 4, 20), s("glute_bridge", 3, 15), s("plank", 3, 45))),
            ProgramDay("Круг 2", listOf(s("lunges", 3, 12), s("dips", 3, 10), s("hyperextension", 3, 15), s("russian_twist", 3, 20))),
            ProgramDay("Круг 3", listOf(s("burpees", 4, 10), s("push_ups", 3, 15), s("sit_ups", 3, 20), c("jump_rope", 10))),
        ),
    ),
    Program(
        "hunter", "Путь Охотника", "🗡️", ProgramLevel.HARDCORE, "Каждый день",
        "Ежедневное задание Системы: 100 отжиманий, 100 подъёмов корпуса, 100 приседаний и 10 км бега. Выполнишь — получишь особое достижение.",
        listOf(ProgramDay("Ежедневное задание", listOf(s("push_ups", 10, 10), s("sit_ups", 10, 10), s("bw_squat", 10, 10), c("running", 60, 10.0)))),
    ),
    Program(
        "cardio_burn", "Кардио и жиросжигание", "🔥", ProgramLevel.BEGINNER, "3 дня в неделю",
        "Выносливость, сердце и расход калорий. Отлично сочетается с дефицитом в разделе «Питание».",
        listOf(
            ProgramDay("Бег + кор", listOf(c("running", 30, 5.0), s("plank", 3, 40), s("russian_twist", 3, 20))),
            ProgramDay("Интервалы", listOf(c("jump_rope", 10), s("burpees", 5, 10), s("bw_squat", 4, 20), s("sit_ups", 3, 20))),
            ProgramDay("Длинное кардио", listOf(c("cycling", 45), c("walking", 30))),
        ),
    ),
)

fun program(id: String?): Program? = PROGRAMS.find { it.id == id }
