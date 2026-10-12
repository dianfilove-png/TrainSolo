package com.trainsolo.app.store

import com.trainsolo.app.model.AppState
import com.trainsolo.app.model.FoodEntry
import com.trainsolo.app.model.Meal
import com.trainsolo.app.model.Profile
import com.trainsolo.app.model.QuestClaim
import com.trainsolo.app.model.WorkoutEntry
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Сериализация состояния в JSON (org.json встроен в Android — без лишних библиотек). */
object Json {
    fun encode(s: AppState): String = JSONObject().apply {
        put("version", 1)
        put("profile", JSONObject().apply {
            put("name", s.profile.name)
            put("sex", s.profile.sex.name)
            put("age", s.profile.age)
            put("heightCm", s.profile.heightCm)
            put("weightKg", s.profile.weightKg)
            put("activity", s.profile.activity.name)
            put("goal", s.profile.goal.name)
            s.profile.customCalories?.let { put("customCalories", it) }
        })
        put("workouts", JSONArray(s.workouts.map { w ->
            JSONObject().apply {
                put("id", w.id); put("date", w.date.toString()); put("exerciseId", w.exerciseId)
                put("sets", w.sets); put("reps", w.reps); put("weight", w.weight)
                put("durationMin", w.durationMin); put("distanceKm", w.distanceKm); put("createdAt", w.createdAt)
            }
        }))
        put("foods", JSONArray(s.foods.map { f ->
            JSONObject().apply {
                put("id", f.id); put("date", f.date.toString()); put("meal", f.meal.name); put("name", f.name)
                put("grams", f.grams); put("calories", f.calories); put("protein", f.protein)
                put("fat", f.fat); put("carbs", f.carbs); put("createdAt", f.createdAt)
            }
        }))
        put("questClaims", JSONArray(s.questClaims.map { q ->
            JSONObject().apply { put("date", q.date.toString()); put("questId", q.questId); put("xp", q.xp) }
        }))
        s.activeProgramId?.let { put("activeProgramId", it) }
        put("programProgress", JSONObject(s.programProgress))
    }.toString()

    fun decode(text: String): AppState {
        val o = JSONObject(text)
        val defaults = Profile()
        val p = o.optJSONObject("profile")
        val profile = if (p == null) defaults else Profile(
            name = p.optString("name", defaults.name),
            sex = enumOr(p.optString("sex"), defaults.sex),
            age = p.optInt("age", defaults.age),
            heightCm = p.optDouble("heightCm", defaults.heightCm),
            weightKg = p.optDouble("weightKg", defaults.weightKg),
            activity = enumOr(p.optString("activity"), defaults.activity),
            goal = enumOr(p.optString("goal"), defaults.goal),
            customCalories = if (p.has("customCalories")) p.getInt("customCalories") else null,
        )
        val progress = o.optJSONObject("programProgress")
        return AppState(
            profile = profile,
            workouts = o.optJSONArray("workouts").objects().map { w ->
                WorkoutEntry(
                    w.getString("id"), LocalDate.parse(w.getString("date")), w.getString("exerciseId"),
                    w.optInt("sets"), w.optInt("reps"), w.optDouble("weight", 0.0),
                    w.optInt("durationMin"), w.optDouble("distanceKm", 0.0), w.optLong("createdAt"),
                )
            },
            foods = o.optJSONArray("foods").objects().map { f ->
                FoodEntry(
                    f.getString("id"), LocalDate.parse(f.getString("date")), enumOr(f.optString("meal"), Meal.SNACK),
                    f.getString("name"), f.optDouble("grams", 0.0), f.optDouble("calories", 0.0),
                    f.optDouble("protein", 0.0), f.optDouble("fat", 0.0), f.optDouble("carbs", 0.0), f.optLong("createdAt"),
                )
            },
            questClaims = o.optJSONArray("questClaims").objects().map { q ->
                QuestClaim(LocalDate.parse(q.getString("date")), q.getString("questId"), q.getInt("xp"))
            },
            activeProgramId = if (o.has("activeProgramId")) o.getString("activeProgramId") else null,
            programProgress = progress?.keys()?.asSequence()?.associateWith { progress.getInt(it) } ?: emptyMap(),
        )
    }

    private fun JSONArray?.objects(): List<JSONObject> =
        if (this == null) emptyList() else (0 until length()).map { getJSONObject(it) }

    private inline fun <reified E : Enum<E>> enumOr(name: String, fallback: E): E =
        enumValues<E>().firstOrNull { it.name == name } ?: fallback

}
