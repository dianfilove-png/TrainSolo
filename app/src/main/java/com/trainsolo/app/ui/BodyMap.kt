package com.trainsolo.app.ui

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Region
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.PathParser
import com.trainsolo.app.logic.MuscleState
import com.trainsolo.app.logic.MuscleStatus
import com.trainsolo.app.model.Muscle

/** Подготовленный контур: путь для рисования и область для попадания пальцем. */
private class Shape(val muscle: Muscle?, val path: androidx.compose.ui.graphics.Path, private val region: Region) {
    fun contains(x: Float, y: Float) = region.contains((x * HIT_SCALE).toInt(), (y * HIT_SCALE).toInt())
}

private const val HIT_SCALE = 4f
private val HIT_CLIP = Region(0, 0, (200 * HIT_SCALE).toInt(), (400 * HIT_SCALE).toInt())
private val MIRROR = Matrix().apply { setScale(-1f, 1f); postTranslate(200f, 0f) }

private fun buildShapes(muscle: Muscle?, d: String): List<Shape> {
    val left = PathParser.createPathFromPathData(d)
    val right = Path(left).apply { transform(MIRROR) }
    return listOf(left, right).map { p ->
        val hit = Path(p).apply { transform(Matrix().apply { setScale(HIT_SCALE, HIT_SCALE) }) }
        Shape(muscle, p.asComposePath(), Region().apply { setPath(hit, HIT_CLIP) })
    }
}

/** Набор контуров для одного пола. */
private class Body(female: Boolean) {
    private val f: (String) -> String = if (female) ::feminize else { d -> d }
    val silhouette = buildShapes(null, f(SILHOUETTE))
    val front = FRONT_SHAPES.flatMap { buildShapes(it.muscle, f(it.d)) }
    val back = BACK_SHAPES.flatMap { buildShapes(it.muscle, f(it.d)) }
    val hair = if (female) buildShapes(null, HAIR_BACK) else emptyList()
}

private val MALE by lazy { Body(false) }
private val FEMALE by lazy { Body(true) }

@Composable
private fun Figure(
    body: Body,
    front: Boolean,
    muscles: Map<Muscle, MuscleState>,
    selected: Muscle?,
    onSelect: (Muscle) -> Unit,
    width: Dp,
    aura: Color,
) {
    val shapes = if (front) body.front else body.back
    Canvas(
        Modifier
            .size(width, width * 2)
            .pointerInput(shapes) {
                detectTapGestures { offset ->
                    val k = size.width / 200f
                    val hit = shapes.lastOrNull { it.contains(offset.x / k, offset.y / k) }
                    hit?.muscle?.let(onSelect)
                }
            },
    ) {
        val k = size.width / 200f
        scale(k, k, pivot = Offset.Zero) {
            drawOval(
                Brush.radialGradient(listOf(aura.copy(alpha = 0.28f), Color.Transparent), center = Offset(100f, 180f), radius = 190f),
                topLeft = Offset(0f, -10f),
                size = Size(200f, 400f),
            )
            drawOval(Color.Black.copy(alpha = 0.5f), Offset(58f, 388f), Size(84f, 10f))

            // Сначала контур с толстой обводкой, затем заливка поверх — так не видно шва по центру.
            body.hair.forEach { drawPath(it.path, HAIR) }
            body.silhouette.forEach { drawPath(it.path, C.bodyStroke, style = Stroke(2.4f)) }
            body.silhouette.forEach { drawPath(it.path, C.body) }

            drawOval(C.body, Offset(83f, 4f), Size(34f, 42f))
            drawOval(C.bodyStroke, Offset(83f, 4f), Size(34f, 42f), style = Stroke(1.2f))
            if (body.hair.isNotEmpty()) {
                // Сзади волосы закрывают затылок целиком, спереди — только чёлку.
                if (front) drawPath(FRINGE, HAIR) else drawOval(HAIR, Offset(82f, 2f), Size(36f, 40f))
            }
            if (front) {
                drawOval(aura, Offset(89.6f, 24.4f), Size(6.8f, 3.2f))
                drawOval(aura, Offset(103.6f, 24.4f), Size(6.8f, 3.2f))
            }

            for (s in shapes) {
                val m = s.muscle ?: continue
                val isSelected = selected == m
                val alpha = if (selected != null && !isSelected) 0.4f else 0.92f
                drawPath(s.path, muscles.getValue(m).status.color().copy(alpha = alpha))
                drawPath(s.path, if (isSelected) Color.White else C.bg, style = Stroke(if (isSelected) 1.8f else 0.8f))
            }
        }
    }
}

private val HAIR = Color(0xFF3A2A22)
private val FRINGE = PathParser.createPathFromPathData(
    "M83,22 C82,10 90,3 100,3 C110,3 118,10 117,22 C112,16 106,13 100,14 C93,13 87,16 83,22 Z",
).asComposePath()

@Composable
fun BodyMap(
    muscles: Map<Muscle, MuscleState>,
    selected: Muscle?,
    onSelect: (Muscle) -> Unit,
    figureWidth: Dp,
    aura: Color,
    female: Boolean = false,
) {
    val body = if (female) FEMALE else MALE
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        listOf(true to "СПЕРЕДИ", false to "СЗАДИ").forEach { (front, caption) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Figure(body, front, muscles, selected, onSelect, figureWidth, aura)
                Text(caption, color = C.textMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun StatusLegend(muscles: Map<Muscle, MuscleState>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally)) {
        MuscleStatus.entries.forEach { s ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Dot(s.color())
                Text("${s.short} · ${muscles.values.count { it.status == s }}", color = C.textDim, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
