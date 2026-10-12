package com.trainsolo.app.logic

import kotlin.math.acos
import kotlin.math.sqrt

/** Что «Глаз Системы» увидел в потоке данных датчика. */
enum class RepEvent {
    /** Честный повтор. */
    REP,
    /** Движение начато, но не доведено до нужной амплитуды. */
    PARTIAL,
    /** Повтор слишком быстрый — не засчитан. */
    TOO_FAST,
}

/** Минимальное время между повторами: быстрее — это уже «дёрганье», а не упражнение. */
const val MIN_REP_INTERVAL_MS = 800L

/**
 * Отжимания: телефон лежит на полу под грудью экраном вверх.
 * Повтор засчитывается в момент, когда грудь приблизилась к датчику (переход «далеко → близко»).
 */
class ProximityReps(private val minIntervalMs: Long = MIN_REP_INTERVAL_MS) {
    private var near = false
    private var lastRep = Long.MIN_VALUE / 2

    fun onSample(isNear: Boolean, timeMs: Long): RepEvent? {
        val wasNear = near
        near = isNear
        if (!isNear || wasNear) return null
        if (timeMs - lastRep < minIntervalMs) return RepEvent.TOO_FAST
        lastRep = timeMs
        return RepEvent.REP
    }
}

/**
 * Повторы по углу наклона телефона относительно положения при калибровке.
 * Движение начинается, когда угол превысил [partialMin], и заканчивается возвратом ниже [up].
 * Если в пике угол дошёл до [down] — это повтор, иначе — недоповтор.
 */
class AngleReps(
    private val down: Float,
    private val partialMin: Float,
    private val up: Float = 25f,
    private val minIntervalMs: Long = MIN_REP_INTERVAL_MS,
) {
    private var moving = false
    private var peak = 0f
    private var lastRep = Long.MIN_VALUE / 2

    /** Текущая глубина: 0 — исходное положение, 1 — нужная амплитуда. */
    var depth = 0f
        private set

    fun onSample(angle: Float, timeMs: Long): RepEvent? {
        depth = (angle / down).coerceIn(0f, 1f)
        if (!moving) {
            if (angle > partialMin) {
                moving = true
                peak = angle
            }
            return null
        }
        peak = maxOf(peak, angle)
        if (angle > up) return null
        moving = false
        return when {
            peak < down -> RepEvent.PARTIAL
            timeMs - lastRep < minIntervalMs -> RepEvent.TOO_FAST
            else -> { lastRep = timeMs; RepEvent.REP }
        }
    }

    companion object {
        /** Присед: бедро из вертикали почти до горизонтали. */
        fun thigh() = AngleReps(down = 70f, partialMin = 40f)
        /** Подъём корпуса: из положения лёжа до полусида. */
        fun torso() = AngleReps(down = 50f, partialMin = 25f)
    }
}

/** Угол в градусах между двумя векторами гравитации. */
fun angleDeg(a: FloatArray, b: FloatArray): Float {
    val dot = a[0] * b[0] + a[1] * b[1] + a[2] * b[2]
    val len = sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]) * sqrt(b[0] * b[0] + b[1] * b[1] + b[2] * b[2])
    if (len == 0f) return 0f
    return Math.toDegrees(acos((dot / len).coerceIn(-1f, 1f)).toDouble()).toFloat()
}

/** Оценка за технику по доле засчитанных повторов. */
fun formGrade(clean: Int, attempts: Int): String {
    if (attempts == 0) return "—"
    val pct = clean * 100 / attempts
    return when {
        pct >= 97 -> "S"
        pct >= 90 -> "A"
        pct >= 80 -> "B"
        pct >= 65 -> "C"
        pct >= 50 -> "D"
        else -> "E"
    }
}

/**
 * Тень от груди над датчиком освещённости. Работает дальше датчика приближения (10–20 см)
 * и выручает там, где тот «виртуальный» или не отдаётся приложениям.
 *
 * Пороги подстраиваются сами: берутся максимум и минимум освещённости за последние секунды,
 * поэтому неважно, насколько светло в комнате и насколько тело затеняет телефон в верхней точке.
 */
class LightShadow(private val windowMs: Long = 4000, private val darkAt: Float = 0.4f, private val releaseAt: Float = 0.7f) {
    private val samples = ArrayDeque<Pair<Long, Float>>()
    private var dark = false

    fun reset() {
        samples.clear()
        dark = false
    }

    fun isDark(lux: Float, timeMs: Long): Boolean {
        samples.addLast(timeMs to lux)
        while (samples.size > 2 && timeMs - samples.first().first > windowMs) samples.removeFirst()
        val hi = samples.maxOf { it.second }
        val lo = samples.minOf { it.second }
        val span = hi - lo
        // Слишком слабое изменение — это шум, а не тень.
        if (hi < MIN_LUX || span < maxOf(MIN_SPAN_LUX, hi * MIN_SPAN_RATIO)) {
            if (dark && lux >= hi * 0.9f) dark = false
            return dark
        }
        dark = if (dark) lux < lo + span * releaseAt else lux < lo + span * darkAt
        return dark
    }

    companion object {
        const val MIN_LUX = 3f
        const val MIN_SPAN_LUX = 2f
        const val MIN_SPAN_RATIO = 0.15f
    }
}

/** Бинарный датчик приближения: «далеко» обычно равно maximumRange, «близко» — 0. */
fun proximityNear(value: Float, maxRange: Float): Boolean = value < minOf(maxRange, 5f)
