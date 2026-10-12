package com.trainsolo.app.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.trainsolo.app.logic.AngleReps
import com.trainsolo.app.logic.ProximityReps
import com.trainsolo.app.logic.RepEvent
import com.trainsolo.app.logic.angleDeg
import com.trainsolo.app.logic.formGrade
import com.trainsolo.app.model.Exercise
import com.trainsolo.app.model.SensorMode
import kotlinx.coroutines.delay
import java.util.Locale

private enum class Stage { BRIEF, CALIBRATE, ACTIVE, REST, DONE }

private const val REST_SEC = 60
private const val CALIBRATE_SEC = 3
/** Подход завершается сам, если столько времени не было ни одного повтора. */
private const val IDLE_END_MS = 6000L

private fun briefing(mode: SensorMode) = when (mode) {
    SensorMode.PROXIMITY ->
        "Положи телефон на пол экраном вверх — верхним краем под грудь. Повтор засчитывается, когда грудь почти касается телефона. " +
            "Если датчик не реагирует — касайся экрана носом или подбородком."
    SensorMode.THIGH_ANGLE ->
        "Положи телефон в передний карман штанов и встань ровно. Система измеряет угол бедра: засчитываются только приседы до параллели."
    SensorMode.TORSO_ANGLE ->
        "Ляг на спину и прижми телефон к груди. Повтор засчитывается, когда корпус поднят достаточно высоко."
}

private fun calibrateHint(mode: SensorMode) = when (mode) {
    SensorMode.PROXIMITY -> "Примите упор лёжа"
    SensorMode.THIGH_ANGLE -> "Встаньте ровно"
    SensorMode.TORSO_ANGLE -> "Лягте на спину"
}

/** Голос Системы, вибрация и звук — всё из платформы, без лишних библиотек. */
private class SystemVoice(context: Context) {
    private var ready = false
    private val tts = TextToSpeech(context.applicationContext) { status ->
        ready = status == TextToSpeech.SUCCESS
    }
    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= 31) context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        else @Suppress("DEPRECATION") context.getSystemService(Vibrator::class.java)

    fun say(text: String) {
        if (!ready) return
        if (tts.voice?.locale?.language != "ru") tts.language = Locale.forLanguageTag("ru-RU")
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text)
    }

    fun buzz(ms: Long = 60) = runCatching { vibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE)) }

    fun buzzTwice() = runCatching { vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 90, 80), -1)) }

    fun buzzRecord() = runCatching { vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 60, 60, 120, 60, 300), -1)) }

    fun close() = tts.shutdown()
}

/**
 * «Глаз Системы»: полноэкранный режим, в котором телефон сам считает повторы.
 * [onSetDone] вызывается после каждого подхода с числом засчитанных повторов.
 */
@Composable
fun SystemSession(ex: Exercise, sets: Int, targetReps: Int, shadow: Int, onSetDone: (Int) -> Unit, onClose: () -> Unit) {
    val mode = ex.sensor ?: return
    val context = LocalContext.current
    val voice = remember { SystemVoice(context) }
    DisposableEffect(Unit) { onDispose { voice.close() } }
    val sensors = remember { context.getSystemService(SensorManager::class.java) }
    val sensor = remember {
        when (mode) {
            SensorMode.PROXIMITY -> sensors?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
            else -> sensors?.getDefaultSensor(Sensor.TYPE_GRAVITY) ?: sensors?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
    }
    // Без датчика приближения повтор засчитывается касанием экрана.
    val touchMode = mode == SensorMode.PROXIMITY

    var stage by remember { mutableStateOf(Stage.BRIEF) }
    var setIndex by remember { mutableIntStateOf(0) }
    var reps by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    var totalClean by remember { mutableIntStateOf(0) }
    var totalAttempts by remember { mutableIntStateOf(0) }
    var depth by remember { mutableStateOf(0f) }
    var lastRepAt by remember { mutableLongStateOf(0L) }
    var shadowBroken by remember { mutableStateOf(false) }
    var hint by remember { mutableStateOf("") }
    val gravity = remember { FloatArray(3) }
    val base = remember { FloatArray(3) }

    fun now() = System.currentTimeMillis()

    fun handle(event: RepEvent?) {
        when (event) {
            null -> return
            RepEvent.REP -> {
                reps++
                totalClean++
                totalAttempts++
                lastRepAt = now()
                hint = ""
                if (shadow > 0 && reps == shadow + 1 && !shadowBroken) {
                    shadowBroken = true
                    voice.buzzRecord()
                    voice.say("$reps. Тень прошлого себя повержена!")
                } else {
                    voice.buzz()
                    voice.say(reps.toString())
                }
            }
            RepEvent.PARTIAL -> {
                misses++
                totalAttempts++
                lastRepAt = now()
                hint = "Ниже! Не засчитано"
                voice.buzzTwice()
                voice.say("Ниже!")
            }
            RepEvent.TOO_FAST -> {
                misses++
                totalAttempts++
                hint = "Медленнее, Охотник"
                voice.buzzTwice()
                voice.say("Медленнее")
            }
        }
    }

    val proximity = remember { ProximityReps() }
    val angle = remember { if (mode == SensorMode.THIGH_ANGLE) AngleReps.thigh() else AngleReps.torso() }
    val tapReps = remember { ProximityReps() }

    fun finishSet() {
        if (reps > 0) onSetDone(reps)
        if (setIndex + 1 < sets) {
            stage = Stage.REST
        } else {
            stage = Stage.DONE
            voice.say("Задание выполнено. Оценка ${formGrade(totalClean, totalAttempts)}")
        }
    }

    // Подписка на датчик живёт, пока идёт калибровка или подход.
    DisposableEffect(stage) {
        val listening = stage == Stage.CALIBRATE || stage == Stage.ACTIVE
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                if (mode == SensorMode.PROXIMITY) {
                    if (stage == Stage.ACTIVE) handle(proximity.onSample(e.values[0] < minOf(e.sensor.maximumRange, 5f), now()))
                    return
                }
                val k = if (e.sensor.type == Sensor.TYPE_GRAVITY) 1f else 0.15f
                for (i in 0..2) gravity[i] += k * (e.values[i] - gravity[i])
                if (stage == Stage.ACTIVE) {
                    handle(angle.onSample(angleDeg(base, gravity), now()))
                    depth = angle.depth
                }
            }

            override fun onAccuracyChanged(s: Sensor?, accuracy: Int) = Unit
        }
        if (listening && sensor != null) sensors?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensors?.unregisterListener(listener) }
    }

    LaunchedEffect(stage) {
        when (stage) {
            Stage.CALIBRATE -> {
                voice.say("Подход ${setIndex + 1}. ${calibrateHint(mode)}. Калибровка")
                delay(CALIBRATE_SEC * 1000L)
                gravity.copyInto(base)
                reps = 0
                misses = 0
                depth = 0f
                hint = ""
                shadowBroken = false
                lastRepAt = 0L
                voice.say("Начали! Цель $targetReps")
                stage = Stage.ACTIVE
            }
            Stage.ACTIVE -> while (true) {
                delay(250)
                if (reps > 0 && now() - lastRepAt > IDLE_END_MS) {
                    finishSet()
                    break
                }
            }
            Stage.REST -> {
                voice.say("Отдых $REST_SEC секунд")
                delay((REST_SEC - 3) * 1000L)
                voice.say("Три, два, один")
                delay(3000)
                setIndex++
                stage = Stage.CALIBRATE
            }
            else -> Unit
        }
    }

    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)) {
        val view = LocalView.current
        DisposableEffect(Unit) {
            view.keepScreenOn = true
            onDispose { view.keepScreenOn = false }
        }
        Column(
            Modifier.fillMaxSize().background(C.bg).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("[ СИСТЕМА ]", color = C.primary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp)
            Text("👁 ${ex.name}", color = C.text, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            when (stage) {
                Stage.BRIEF -> {
                    Text(briefing(mode), color = C.textDim, fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center)
                    if (sensor == null) {
                        Text(
                            if (touchMode) "Датчик приближения не найден — засчитывай повторы касанием экрана."
                            else "На телефоне нет датчика положения — режим недоступен.",
                            color = C.recovering, fontSize = 14.sp, textAlign = TextAlign.Center,
                        )
                    }
                    Text("$sets × $targetReps · бонус Системы +5 XP за подход", color = C.xp, fontWeight = FontWeight.Bold)
                    Box(Modifier.weight(1f))
                    PrimaryButton("Начать задание", {
                        stage = Stage.CALIBRATE
                    }, Modifier.fillMaxWidth(), enabled = sensor != null || touchMode, icon = "▶")
                    SecondaryButton("Отмена", onClose, Modifier.fillMaxWidth())
                }
                Stage.CALIBRATE -> {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("${calibrateHint(mode)}…", color = C.text, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                    }
                    Text("Подход ${setIndex + 1} из $sets", color = C.textDim)
                }
                Stage.ACTIVE -> {
                    Text("Подход ${setIndex + 1} из $sets · цель $targetReps", color = C.textDim, fontSize = 15.sp)
                    if (shadow > 0) {
                        Text(
                            if (shadowBroken) "Тень повержена! Рекорд: $reps" else "Тень прошлого себя: $shadow",
                            color = if (shadowBroken) C.xp else C.textMuted, fontWeight = FontWeight.Bold,
                        )
                    }
                    Box(
                        Modifier.weight(1f).fillMaxWidth().clickable(
                            interactionSource = remember { MutableInteractionSource() }, indication = null, enabled = touchMode,
                        ) {
                            val t = now()
                            tapReps.onSample(false, t)
                            handle(tapReps.onSample(true, t))
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        DepthRing(if (mode == SensorMode.PROXIMITY) reps.toFloat() / maxOf(1, targetReps) else depth, reps >= targetReps)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$reps", color = C.text, fontSize = 96.sp, fontWeight = FontWeight.Black)
                            Text(hint.ifEmpty { if (touchMode) "или касание экрана" else " " }, color = if (hint.isEmpty()) C.textMuted else C.danger, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Stat("Не засчитано", "$misses")
                        Stat("Чистота", if (totalAttempts == 0) "—" else "${totalClean * 100 / totalAttempts}%")
                    }
                    PrimaryButton("Стоп — подход окончен", { finishSet() }, Modifier.fillMaxWidth(), icon = "■")
                }
                Stage.REST -> {
                    var left by remember { mutableIntStateOf(REST_SEC) }
                    LaunchedEffect(Unit) { while (left > 0) { delay(1000); left-- } }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Отдых", color = C.textDim, fontSize = 18.sp)
                            Text("$left", color = C.primary, fontSize = 88.sp, fontWeight = FontWeight.Black)
                            Text("Засчитано: $reps", color = C.text, fontSize = 16.sp)
                        }
                    }
                    SecondaryButton("Пропустить отдых", { setIndex++; stage = Stage.CALIBRATE }, Modifier.fillMaxWidth())
                }
                Stage.DONE -> {
                    val grade = formGrade(totalClean, totalAttempts)
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Задание выполнено", color = C.text, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                            Text(grade, color = C.xp, fontSize = 110.sp, fontWeight = FontWeight.Black)
                            Text("Подтверждено: $totalClean из $totalAttempts", color = C.textDim, fontSize = 16.sp)
                        }
                    }
                    PrimaryButton("Готово", onClose, Modifier.fillMaxWidth(), icon = "✓")
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = C.text, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = C.textMuted, fontSize = 12.sp)
    }
}

@Composable
private fun DepthRing(progress: Float, complete: Boolean) {
    Canvas(Modifier.size(260.dp)) {
        val stroke = 16.dp.toPx()
        val inset = stroke / 2
        val arc = Size(size.width - stroke, size.height - stroke)
        drawArc(C.border, 135f, 270f, false, Offset(inset, inset), arc, style = Stroke(stroke, cap = StrokeCap.Round))
        drawArc(
            if (complete) C.xp else C.primary, 135f, 270f * progress.coerceIn(0f, 1f), false, Offset(inset, inset), arc,
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
    }
}
