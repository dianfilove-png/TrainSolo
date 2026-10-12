package com.trainsolo.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trainsolo.app.logic.num
import kotlin.math.max
import kotlin.math.roundToLong

@Composable
fun ScreenTitle(title: String, subtitle: String? = null) {
    Column {
        Text(title, color = C.text, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        if (subtitle != null) Text(subtitle, color = C.textDim, fontSize = 14.sp)
    }
}

@Composable
fun Card(
    modifier: Modifier = Modifier,
    border: Color = C.border,
    background: Color = C.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
fun SectionTitle(title: String, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = C.text, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
fun ProgressBar(value: Float, color: Color = C.primary, height: Int = 8, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(CircleShape)
            .background(C.surfaceAlt),
    ) {
        Box(
            Modifier
                .fillMaxWidth(value.coerceIn(0f, 1f))
                .height(height.dp)
                .clip(CircleShape)
                .background(color),
        )
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, icon: String? = null) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = C.primary, contentColor = Color(0xFF03121F)),
    ) {
        Text((if (icon != null) "$icon  " else "") + text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, color: Color = C.text) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (color == C.text) C.border else color),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (color == C.text) C.surfaceAlt else Color.Transparent, contentColor = color),
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
fun SmallButton(text: String, onClick: () -> Unit, filled: Boolean = false) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (filled) C.primary else C.surfaceAlt)
            .border(1.dp, if (filled) C.primary else C.border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(text, color = if (filled) Color(0xFF03121F) else C.text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Chip(label: String, active: Boolean, onClick: () -> Unit, color: Color = C.primary) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(if (active) color.copy(alpha = 0.15f) else C.surface)
            .border(1.dp, if (active) color else C.border, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(label, color = if (active) color else C.textDim, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun Badge(label: String, color: Color) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.13f))
            .border(1.dp, color.copy(alpha = 0.4f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

fun parseNumber(text: String): Double? = text.replace(',', '.').trim().toDoubleOrNull()

/** Числовое поле с кнопками − / +. */
@Composable
fun Stepper(
    label: String,
    value: Double,
    onChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    step: Double = 1.0,
    min: Double = 0.0,
    suffix: String? = null,
) {
    fun clamp(v: Double) = max(min, (v * 100).roundToLong() / 100.0)
    var text by remember { mutableStateOf(num(value)) }
    LaunchedEffect(value) {
        if (parseNumber(text) != value) text = num(value)
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label + (suffix?.let { " · $it" } ?: ""), color = C.textDim, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(C.surfaceAlt)
                .border(1.dp, C.border, RoundedCornerShape(12.dp)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onChange(clamp(value - step)) }) { Text("−", color = C.text, fontSize = 22.sp) }
            androidx.compose.foundation.text.BasicTextField(
                value = text,
                onValueChange = { t ->
                    text = t
                    parseNumber(t)?.let { onChange(clamp(it)) }
                },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = C.text, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(C.primary),
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { onChange(clamp(value + step)) }) { Icon(Icons.Filled.Add, "Увеличить", tint = C.text) }
        }
    }
}

@Composable
fun Field(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier, placeholder: String = "", numeric: Boolean = false) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = C.textDim, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder, color = C.textMuted) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = C.surfaceAlt,
                unfocusedContainerColor = C.surfaceAlt,
                focusedBorderColor = C.primary,
                unfocusedBorderColor = C.border,
                focusedTextColor = C.text,
                unfocusedTextColor = C.text,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun Empty(emoji: String, text: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 28.sp)
        Text(text, color = C.textMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun Dot(color: Color, size: Int = 10) {
    Box(Modifier.size(size.dp).clip(CircleShape).background(color))
}

@Composable
fun ConfirmDialog(title: String, text: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = C.surface,
        title = { Text(title, color = C.text) },
        text = { Text(text, color = C.textDim) },
        confirmButton = { TextButton(onClick = { onConfirm(); onDismiss() }) { Text(confirm, color = C.danger) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = C.textDim) } },
    )
}

@Composable
fun RowScope.MiniStat(value: String, label: String) {
    Column(
        Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(C.surface)
            .border(1.dp, C.border, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = C.text, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = C.textMuted, fontSize = 11.sp)
    }
}
