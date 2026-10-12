package com.trainsolo.app.store

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.trainsolo.app.model.AppState
import com.trainsolo.app.model.FoodEntry
import com.trainsolo.app.model.Profile
import com.trainsolo.app.model.QuestClaim
import com.trainsolo.app.model.WorkoutEntry
import java.time.LocalDate
import java.util.UUID

/** Состояние приложения. Хранится локально в SharedPreferences в виде JSON. */
class AppStore(context: Context) {
    private val prefs = context.getSharedPreferences("trainsolo", Context.MODE_PRIVATE)

    var state by mutableStateOf(load())
        private set

    /** Текущая дата. Обновляется при возвращении в приложение — после полуночи экраны переключатся на новый день. */
    var today by mutableStateOf(LocalDate.now())
        private set

    fun refreshToday() {
        val now = LocalDate.now()
        if (now != today) today = now
    }

    private fun load(): AppState = try {
        prefs.getString(KEY, null)?.let { Json.decode(it) } ?: AppState()
    } catch (e: Exception) {
        AppState()
    }

    private fun update(transform: (AppState) -> AppState) {
        state = transform(state)
        prefs.edit().putString(KEY, Json.encode(state)).apply()
    }

    fun addWorkout(
        date: LocalDate, exerciseId: String, sets: Int, reps: Int, weight: Double, durationMin: Int, distanceKm: Double,
        verifiedReps: Int = 0,
    ) = update {
        it.copy(workouts = it.workouts + WorkoutEntry(newId(), date, exerciseId, sets, reps, weight, durationMin, distanceKm, System.currentTimeMillis(), verifiedReps))
    }

    fun removeWorkout(id: String) = update { s -> s.copy(workouts = s.workouts.filterNot { it.id == id }) }

    fun addFood(entry: FoodEntry) = update { it.copy(foods = it.foods + entry.copy(id = newId(), createdAt = System.currentTimeMillis())) }

    fun removeFood(id: String) = update { s -> s.copy(foods = s.foods.filterNot { it.id == id }) }

    fun claimQuest(claim: QuestClaim) = update { s ->
        if (s.questClaims.any { it.date == claim.date && it.questId == claim.questId }) s
        else s.copy(questClaims = s.questClaims + claim)
    }

    fun updateProfile(transform: (Profile) -> Profile) = update { it.copy(profile = transform(it.profile)) }

    fun setActiveProgram(id: String?) = update { it.copy(activeProgramId = id) }

    fun setProgramDay(programId: String, dayIndex: Int) =
        update { it.copy(programProgress = it.programProgress + (programId to dayIndex)) }

    fun reset() = update { AppState() }

    private fun newId() = UUID.randomUUID().toString()

    private companion object {
        const val KEY = "state_v1"
    }
}
