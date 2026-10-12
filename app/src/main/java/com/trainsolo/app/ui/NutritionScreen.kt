package com.trainsolo.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trainsolo.app.logic.Goals
import com.trainsolo.app.logic.XP_PER_FOOD
import com.trainsolo.app.logic.num
import com.trainsolo.app.logic.sumFoods
import com.trainsolo.app.model.FoodEntry
import com.trainsolo.app.model.Meal
import com.trainsolo.app.store.AppStore
import kotlin.math.roundToInt

@Composable
fun NutritionScreen(store: AppStore, goals: Goals) {
    val state = store.state
    val today = store.today
    val toast = LocalToast.current
    val (date, setDate) = rememberSelectedDate(today)
    var meal by remember { mutableStateOf<Meal?>(null) }
    var nonce by remember { mutableIntStateOf(0) }
    var toDelete by remember { mutableStateOf<FoodEntry?>(null) }

    val foods = state.foods.filter { it.date == date }
    val total = sumFoods(foods)
    val remaining = goals.calories - total.calories.roundToInt()
    val recent = remember(state.foods) {
        state.foods.sortedByDescending { it.createdAt }.distinctBy { it.name }.take(20)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle("Питание", "Сколько съел сегодня — топливо для героя")
        DaySwitcher(date, today, setDate)

        Card {
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text("${total.calories.roundToInt()}", color = C.text, fontSize = 36.sp, fontWeight = FontWeight.Black)
                    Text("из ${goals.calories} ккал", color = C.textDim, fontSize = 14.sp)
                }
                Text(
                    if (remaining >= 0) "Осталось $remaining ккал" else "Перебор ${-remaining} ккал",
                    color = if (remaining >= 0) C.fresh else C.loaded, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                )
            }
            ProgressBar((total.calories / goals.calories).toFloat(), if (total.calories > goals.calories * 1.1) C.loaded else C.xp, 12)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    Triple("Белки", total.protein to goals.protein, C.protein),
                    Triple("Жиры", total.fat to goals.fat, C.fat),
                    Triple("Углеводы", total.carbs to goals.carbs, C.carbs),
                ).forEach { (label, values, color) ->
                    MacroColumn(label, values.first, values.second, color)
                }
            }
        }

        Meal.entries.forEach { m ->
            val items = foods.filter { it.meal == m }.sortedBy { it.createdAt }
            val kcal = sumFoods(items).calories.roundToInt()
            Card {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${m.emoji} ${m.title}", color = C.text, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    if (kcal > 0) Text("$kcal ккал", color = C.textDim, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = { nonce++; meal = m }) { Icon(Icons.Filled.Add, "Добавить: ${m.title}", tint = C.primary) }
                }
                items.forEach { f ->
                    HorizontalDivider(color = C.border)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                buildAnnotatedString {
                                    append(f.name)
                                    if (f.grams > 0) withStyle(SpanStyle(color = C.textMuted, fontWeight = FontWeight.Normal)) { append(" · ${num(f.grams)} г") }
                                },
                                color = C.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                            )
                            Text("Б ${f.protein.roundToInt()} · Ж ${f.fat.roundToInt()} · У ${f.carbs.roundToInt()}", color = C.textMuted, fontSize = 12.sp)
                        }
                        Text("${f.calories.roundToInt()}", color = C.text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { toDelete = f }) { Icon(Icons.Filled.Delete, "Удалить", tint = C.textDim) }
                    }
                }
            }
        }
    }

    meal?.let { m ->
        androidx.compose.runtime.key(nonce) {
            FoodSheet(m, recent, onDismiss = { meal = null }) { f ->
                store.addFood(f.copy(date = date))
                meal = null
                toast("${f.name}: ${f.calories.roundToInt()} ккал · +$XP_PER_FOOD XP")
            }
        }
    }
    toDelete?.let { f ->
        ConfirmDialog("Удалить запись?", f.name, "Удалить", { store.removeFood(f.id) }, { toDelete = null })
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.MacroColumn(label: String, value: Double, goal: Int, color: Color) {
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(
            buildAnnotatedString {
                append("${value.roundToInt()}")
                withStyle(SpanStyle(color = C.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)) { append(" / $goal г") }
            },
            color = C.text, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold,
        )
        ProgressBar((value / goal).toFloat(), color, 5)
    }
}
