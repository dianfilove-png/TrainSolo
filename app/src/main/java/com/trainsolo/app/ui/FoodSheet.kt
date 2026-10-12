package com.trainsolo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trainsolo.app.data.FOOD_PRESETS
import com.trainsolo.app.data.FoodPreset
import com.trainsolo.app.logic.num
import com.trainsolo.app.logic.scalePreset
import com.trainsolo.app.model.FoodEntry
import com.trainsolo.app.model.Meal
import java.time.LocalDate
import kotlin.math.roundToInt

private enum class FoodTab(val title: String) { CATALOG("Каталог"), RECENT("Недавние"), MANUAL("Вручную") }

/** Черновик записи: дата, id и время проставляются при сохранении. */
fun draft(meal: Meal, name: String, grams: Double, kcal: Double, p: Double, f: Double, c: Double) =
    FoodEntry("", LocalDate.MIN, meal, name, grams, kcal, p, f, c, 0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSheet(initialMeal: Meal, recent: List<FoodEntry>, onDismiss: () -> Unit, onSave: (FoodEntry) -> Unit) {
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var meal by remember { mutableStateOf(initialMeal) }
    var tab by remember { mutableStateOf(FoodTab.CATALOG) }
    var preset by remember { mutableStateOf<FoodPreset?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet, containerColor = C.surface) {
        Column(Modifier.padding(horizontal = 16.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(preset?.name ?: "Добавить еду", color = C.text, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Meal.entries.forEach { m -> Chip(m.title, meal == m, { meal = m }) }
            }
            val p = preset
            if (p != null) {
                PresetForm(p, onBack = { preset = null }) { grams ->
                    val m = scalePreset(p, grams)
                    onSave(draft(meal, p.name, grams, m.calories, m.protein, m.fat, m.carbs))
                }
            } else {
                Row(Modifier.fillMaxWidth().background(C.surfaceAlt, RoundedCornerShape(12.dp)).padding(3.dp)) {
                    FoodTab.entries.forEach { t ->
                        Box(
                            Modifier.weight(1f).background(if (tab == t) C.primaryDim else C.surfaceAlt, RoundedCornerShape(8.dp))
                                .clickable { tab = t }.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(t.title, color = if (tab == t) C.text else C.textDim, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
                when (tab) {
                    FoodTab.CATALOG -> Catalog { preset = it }
                    FoodTab.RECENT -> Recent(recent) { onSave(it.copy(meal = meal)) }
                    FoodTab.MANUAL -> Manual { onSave(it.copy(meal = meal)) }
                }
            }
        }
    }
}

@Composable
private fun Catalog(onPick: (FoodPreset) -> Unit) {
    var query by remember { mutableStateOf("") }
    val list = FOOD_PRESETS.filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }
    Field("Поиск", query, { query = it }, placeholder = "Название продукта")
    LazyColumn(Modifier.heightIn(min = 200.dp, max = 420.dp)) {
        items(list, key = { it.name }) { f ->
            Row(Modifier.fillMaxWidth().clickable { onPick(f) }.padding(vertical = 12.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(f.name, color = C.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("${num(f.calories)} ккал / 100 г · порция ${f.portion} г", color = C.textMuted, fontSize = 13.sp)
                }
                Text("›", color = C.textMuted, fontSize = 22.sp)
            }
            HorizontalDivider(color = C.border)
        }
        if (list.isEmpty()) item { Text("Нет такого продукта — добавь его во вкладке «Вручную»", color = C.textMuted, modifier = Modifier.padding(12.dp)) }
    }
}

@Composable
private fun PresetForm(p: FoodPreset, onBack: () -> Unit, onSave: (Double) -> Unit) {
    var grams by remember { mutableDoubleStateOf(p.portion.toDouble()) }
    val m = scalePreset(p, grams)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 16.dp)) {
        Stepper("Вес порции", grams, { grams = it }, step = 10.0, suffix = "г")
        Text(
            "${m.calories.roundToInt()} ккал   Б ${num(m.protein)} · Ж ${num(m.fat)} · У ${num(m.carbs)}",
            color = C.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().background(C.surfaceAlt, RoundedCornerShape(12.dp)).padding(14.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton("Назад", onBack, Modifier.weight(1f))
            PrimaryButton("Добавить", { onSave(grams) }, Modifier.weight(2f), enabled = grams > 0, icon = "✓")
        }
    }
}

@Composable
private fun Recent(items: List<FoodEntry>, onPick: (FoodEntry) -> Unit) {
    if (items.isEmpty()) {
        Text("Здесь появятся продукты, которые ты уже записывал", color = C.textMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(24.dp))
        return
    }
    LazyColumn(Modifier.heightIn(min = 200.dp, max = 420.dp)) {
        items(items) { f ->
            Row(Modifier.fillMaxWidth().clickable { onPick(f) }.padding(vertical = 12.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(f.name, color = C.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        (if (f.grams > 0) "${num(f.grams)} г · " else "") + "${f.calories.roundToInt()} ккал · Б ${num(f.protein)} · Ж ${num(f.fat)} · У ${num(f.carbs)}",
                        color = C.textMuted, fontSize = 13.sp,
                    )
                }
                Text("＋", color = C.primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = C.border)
        }
    }
}

@Composable
private fun Manual(onSave: (FoodEntry) -> Unit) {
    var name by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var grams by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    val calories = parseNumber(kcal) ?: 0.0
    Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Field("Название", name, { name = it }, placeholder = "Например, борщ")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Field("Калории", kcal, { kcal = it }, Modifier.weight(1f), "ккал", numeric = true)
            Field("Вес, г", grams, { grams = it }, Modifier.weight(1f), "необяз.", numeric = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Field("Белки", protein, { protein = it }, Modifier.weight(1f), "0", numeric = true)
            Field("Жиры", fat, { fat = it }, Modifier.weight(1f), "0", numeric = true)
            Field("Углев.", carbs, { carbs = it }, Modifier.weight(1f), "0", numeric = true)
        }
        PrimaryButton(
            "Добавить",
            {
                onSave(
                    draft(
                        Meal.SNACK, name.trim(), parseNumber(grams) ?: 0.0, calories.roundToInt().toDouble(),
                        parseNumber(protein) ?: 0.0, parseNumber(fat) ?: 0.0, parseNumber(carbs) ?: 0.0,
                    ),
                )
            },
            Modifier.fillMaxWidth(), enabled = name.isNotBlank() && calories > 0, icon = "✓",
        )
    }
}
