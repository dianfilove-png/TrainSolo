package com.trainsolo.app.logic

import com.trainsolo.app.model.AppState
import com.trainsolo.app.model.WorkoutEntry
import com.trainsolo.app.store.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.sin

class RepDetectorTest {
    /** Синусоида угла: [reps] повторов с амплитудой [peak] и периодом [periodMs]. */
    private fun run(det: AngleReps, peak: Float, reps: Int, periodMs: Long = 2000): List<RepEvent> {
        val out = mutableListOf<RepEvent>()
        var t = 0L
        while (t <= reps * periodMs + 500) {
            val phase = (t % periodMs).toDouble() / periodMs
            val angle = if (t >= reps * periodMs) 0f else (peak * sin(PI * phase)).toFloat()
            det.onSample(angle, t)?.let(out::add)
            t += 20
        }
        return out
    }

    @Test
    fun deepSquatsCount() = assertEquals(List(5) { RepEvent.REP }, run(AngleReps.thigh(), 85f, 5))

    @Test
    fun shallowSquatsArePartial() = assertEquals(List(3) { RepEvent.PARTIAL }, run(AngleReps.thigh(), 55f, 3))

    @Test
    fun tinyWobbleIgnored() = assertEquals(emptyList<RepEvent>(), run(AngleReps.thigh(), 30f, 4))

    @Test
    fun tooFastSquats() {
        val events = run(AngleReps.thigh(), 85f, 4, periodMs = 600)
        assertEquals(RepEvent.REP, events.first())
        assertEquals(true, RepEvent.TOO_FAST in events)
    }

    @Test
    fun proximityReps() {
        val p = ProximityReps()
        val events = listOf(
            p.onSample(false, 0), p.onSample(true, 1000), p.onSample(true, 1100), p.onSample(false, 1500),
            p.onSample(true, 1700), p.onSample(false, 2000), p.onSample(true, 3000),
        ).filterNotNull()
        assertEquals(listOf(RepEvent.REP, RepEvent.TOO_FAST, RepEvent.REP), events)
    }

    @Test
    fun angle() {
        assertEquals(90f, angleDeg(floatArrayOf(0f, 9.8f, 0f), floatArrayOf(9.8f, 0f, 0f)), 0.01f)
        assertEquals(0f, angleDeg(floatArrayOf(0f, 0f, 1f), floatArrayOf(0f, 0f, 5f)), 0.01f)
    }

    @Test
    fun grades() {
        assertEquals("S", formGrade(100, 100))
        assertEquals("A", formGrade(92, 100))
        assertEquals("E", formGrade(1, 10))
        assertEquals("—", formGrade(0, 0))
    }

    @Test
    fun verifiedBonusAndJson() {
        val d = LocalDate.of(2026, 10, 12)
        val plain = WorkoutEntry("a", d, "push_ups", 1, 12, 0.0, 0, 0.0, 0)
        val seen = plain.copy(id = "b", verifiedReps = 12)
        assertEquals(10, entryXp(plain))
        assertEquals(15, entryXp(seen))
        val s = AppState(workouts = listOf(plain, seen))
        assertEquals(s, Json.decode(Json.encode(s)))
    }
}
