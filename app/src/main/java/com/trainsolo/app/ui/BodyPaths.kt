package com.trainsolo.app.ui

import com.trainsolo.app.model.Muscle
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

/**
 * Контуры аватара в системе координат 200×400 (SVG path data).
 * Описана только левая половина фигуры (x ≤ 100) — правая получается зеркальным отражением.
 */
data class MuscleShape(val muscle: Muscle, val d: String)

/** Скруглённый прямоугольник в виде path. */
private fun rr(x: Float, y: Float, w: Float, h: Float, r: Float): String =
    "M${x + r},$y L${x + w - r},$y Q${x + w},$y ${x + w},${y + r} L${x + w},${y + h - r} " +
        "Q${x + w},${y + h} ${x + w - r},${y + h} L${x + r},${y + h} Q$x,${y + h} $x,${y + h - r} L$x,${y + r} Q$x,$y ${x + r},$y Z"

const val SILHOUETTE =
    "M100,44 L92,45 C91,51 91,56 90,60 C84,63 74,65 66,67 C55,69 47,77 46,90 C45,100 44,110 44,120 " +
        "C43,130 42,138 42,145 C39,158 35,172 34,186 C34,192 35,198 36,202 C32,208 31,216 33,222 " +
        "C35,228 43,229 46,224 C48,218 48,210 47,204 C49,192 53,176 56,162 C58,154 60,148 60,144 " +
        "C62,132 63,120 64,108 C66,104 68,102 70,102 C71,118 72,138 73,156 C73,166 71,176 69,186 " +
        "C66,196 64,206 65,218 C66,240 69,262 72,282 C72,290 70,298 70,306 C70,322 73,340 76,356 " +
        "C77,362 77,368 77,372 C73,378 71,386 75,390 C81,393 92,393 95,389 C96,384 94,376 93,371 " +
        "C94,356 96,340 96,324 C96,312 95,300 95,292 C96,272 98,248 98,228 C99,218 99,212 100,208 Z"

private const val SHOULDER = "M67,70 C56,71 49,79 48,90 C48,98 50,104 53,108 C56,100 60,92 66,86 C70,82 72,76 67,70 Z"
private const val FOREARM = "M45,148 C41,160 38,174 37,190 C39,196 44,198 47,196 C50,182 54,168 57,154 C56,148 50,144 45,148 Z"

val FRONT_SHAPES = listOf(
    MuscleShape(TRAPS, "M90,61 C84,64 76,66 69,68 C76,70 83,70 89,68 Z"),
    MuscleShape(SHOULDERS, SHOULDER),
    MuscleShape(CHEST, "M99,72 L75,71 C70,75 68,83 68,92 C68,102 72,110 80,112 C88,114 95,112 99,110 Z"),
    MuscleShape(BICEPS, "M54,110 C51,118 50,128 51,138 C53,142 58,143 61,140 C63,130 63,120 62,110 C60,106 56,106 54,110 Z"),
    MuscleShape(FOREARMS, FOREARM),
    MuscleShape(ABS, rr(88f, 116f, 11f, 14f, 3f)),
    MuscleShape(ABS, rr(87.5f, 133f, 11.5f, 14f, 3f)),
    MuscleShape(ABS, rr(87.5f, 150f, 11.5f, 14f, 3f)),
    MuscleShape(ABS, "M88,167 L99,167 L99,194 C95,192 91,186 89,178 C88,174 88,170 88,167 Z"),
    MuscleShape(OBLIQUES, "M84,114 C78,116 74,122 73,132 C72,148 73,164 75,176 C78,182 82,186 86,188 C85,176 85,160 85,146 C85,134 85,122 84,114 Z"),
    MuscleShape(QUADS, "M69,206 C66,222 67,246 71,266 C74,276 80,282 87,282 C93,280 96,270 97,258 C98,240 98,224 96,212 C92,206 84,202 76,200 C73,201 70,203 69,206 Z"),
    MuscleShape(CALVES, "M74,304 C72,318 73,334 77,350 C80,356 85,356 88,350 C91,336 93,320 91,306 C89,298 77,298 74,304 Z"),
)

val BACK_SHAPES = listOf(
    MuscleShape(TRAPS, "M99,46 L93,47 C92,53 91,57 90,61 C83,64 75,66 69,69 C77,74 84,80 89,90 C93,100 97,112 99,124 Z"),
    MuscleShape(SHOULDERS, SHOULDER),
    MuscleShape(LATS, "M72,84 C80,88 86,94 89,102 C93,110 95,118 96,126 C96,134 95,142 93,150 C88,158 82,158 78,152 C74,144 71,130 70,114 C70,102 70,92 72,84 Z"),
    MuscleShape(LOWER_BACK, "M99,140 L99,196 C95,196 91,195 88,193 C87,182 87,170 89,160 C92,154 96,148 99,140 Z"),
    MuscleShape(OBLIQUES, "M74,160 C72,170 72,180 74,190 L85,193 C85,180 85,168 84,160 C81,164 77,164 74,160 Z"),
    MuscleShape(TRICEPS, "M53,108 C49,116 48,128 50,140 C53,144 58,145 61,141 C63,130 64,118 62,108 C59,104 55,104 53,108 Z"),
    MuscleShape(FOREARMS, FOREARM),
    MuscleShape(GLUTES, "M99,198 C92,196 84,196 77,198 C70,202 66,210 66,220 C66,232 72,240 82,242 C90,243 96,240 99,236 Z"),
    MuscleShape(HAMSTRINGS, "M69,246 C68,260 70,272 73,282 C77,288 84,290 89,288 C94,284 96,274 97,262 C98,256 98,250 97,246 C90,250 79,250 69,246 Z"),
    MuscleShape(CALVES, "M73,300 C69,314 70,330 75,344 C79,350 85,350 89,344 C93,330 94,314 91,300 C88,292 77,292 73,300 Z"),
)
