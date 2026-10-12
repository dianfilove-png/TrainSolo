import { useMemo, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';

import { confirm } from '@/components/confirm';
import { DaySwitcher } from '@/components/DaySwitcher';
import { useToast } from '@/components/Feedback';
import { FoodModal, MEAL_LABELS, MEALS, type FoodDraft } from '@/components/FoodModal';
import { Card, IconButton, ProgressBar, Screen } from '@/components/ui';
import { sumFoods } from '@/logic/nutrition';
import { XP_PER_FOOD } from '@/logic/xp';
import { useStore } from '@/store/AppStore';
import { colors } from '@/theme';
import type { MealType } from '@/types';

const MEAL_ICONS: Record<MealType, string> = { breakfast: '🍳', lunch: '🍲', dinner: '🍽️', snack: '🍎' };

export default function NutritionScreen() {
  const { state, today, goals, addFood, removeFood } = useStore();
  const toast = useToast();
  // null — «сегодня»: после полуночи экран сам переключится на новый день.
  const [picked, setPicked] = useState<string | null>(null);
  const date = picked && picked < today ? picked : today;
  const setDate = (d: string) => setPicked(d >= today ? null : d);
  const [meal, setMeal] = useState<MealType | null>(null);
  const [modalKey, setModalKey] = useState(0);
  const openMeal = (m: MealType) => {
    setMeal(m);
    setModalKey((k) => k + 1);
  };

  const foods = useMemo(() => state.foods.filter((f) => f.date === date), [state.foods, date]);
  const total = sumFoods(foods);
  const remaining = goals.calories - Math.round(total.calories);

  const recent = useMemo(() => {
    const seen = new Set<string>();
    const list: FoodDraft[] = [];
    for (const f of [...state.foods].sort((a, b) => b.createdAt - a.createdAt)) {
      if (seen.has(f.name)) continue;
      seen.add(f.name);
      list.push({ name: f.name, meal: f.meal, grams: f.grams, calories: f.calories, protein: f.protein, fat: f.fat, carbs: f.carbs });
      if (list.length >= 20) break;
    }
    return list;
  }, [state.foods]);

  const macros = [
    { key: 'protein', label: 'Белки', value: total.protein, goal: goals.protein, color: colors.protein },
    { key: 'fat', label: 'Жиры', value: total.fat, goal: goals.fat, color: colors.fat },
    { key: 'carbs', label: 'Углеводы', value: total.carbs, goal: goals.carbs, color: colors.carbs },
  ];

  const over = total.calories > goals.calories * 1.1;

  return (
    <Screen title="Питание" subtitle="Сколько съел сегодня — топливо для героя">
      <DaySwitcher date={date} today={today} onChange={setDate} />

      <Card>
        <View style={styles.calRow}>
          <View>
            <Text style={styles.calValue}>{Math.round(total.calories)}</Text>
            <Text style={styles.calLabel}>из {goals.calories} ккал</Text>
          </View>
          <Text style={[styles.remaining, { color: remaining >= 0 ? colors.fresh : colors.loaded }]}>
            {remaining >= 0 ? `Осталось ${remaining}` : `Перебор ${-remaining}`} ккал
          </Text>
        </View>
        <ProgressBar value={total.calories / goals.calories} color={over ? colors.loaded : colors.xp} height={12} />
        <View style={styles.macros}>
          {macros.map((m) => (
            <View key={m.key} style={styles.macro}>
              <Text style={[styles.macroLabel, { color: m.color }]}>{m.label}</Text>
              <Text style={styles.macroValue}>
                {Math.round(m.value)}
                <Text style={styles.macroGoal}> / {m.goal} г</Text>
              </Text>
              <ProgressBar value={m.value / m.goal} color={m.color} height={5} />
            </View>
          ))}
        </View>
      </Card>

      {MEALS.map((m) => {
        const items = foods.filter((f) => f.meal === m).sort((a, b) => a.createdAt - b.createdAt);
        const kcal = Math.round(sumFoods(items).calories);
        return (
          <Card key={m} style={{ gap: 8 }}>
            <View style={styles.mealHeader}>
              <Text style={styles.mealTitle}>
                {MEAL_ICONS[m]} {MEAL_LABELS[m]}
              </Text>
              <Text style={styles.mealKcal}>{kcal ? `${kcal} ккал` : ''}</Text>
              <IconButton icon="add-circle" color={colors.primary} label={`Добавить: ${MEAL_LABELS[m]}`} onPress={() => openMeal(m)} />
            </View>
            {items.map((f) => (
              <View key={f.id} style={styles.food}>
                <View style={{ flex: 1 }}>
                  <Text style={styles.foodName}>
                    {f.name}
                    {f.grams ? <Text style={styles.foodGrams}> · {f.grams} г</Text> : null}
                  </Text>
                  <Text style={styles.foodMacros}>
                    Б {Math.round(f.protein)} · Ж {Math.round(f.fat)} · У {Math.round(f.carbs)}
                  </Text>
                </View>
                <Text style={styles.foodKcal}>{Math.round(f.calories)}</Text>
                <IconButton
                  icon="trash-outline"
                  label="Удалить"
                  onPress={async () => {
                    if (await confirm('Удалить запись?', f.name)) removeFood(f.id);
                  }}
                />
              </View>
            ))}
          </Card>
        );
      })}

      <FoodModal
        key={modalKey}
        visible={meal !== null}
        meal={meal ?? 'breakfast'}
        recent={recent}
        onClose={() => setMeal(null)}
        onSave={(food) => {
          addFood({ ...food, date });
          setMeal(null);
          toast(`${food.name}: ${Math.round(food.calories)} ккал · +${XP_PER_FOOD} XP`);
        }}
      />
    </Screen>
  );
}

const styles = StyleSheet.create({
  calRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-end' },
  calValue: { color: colors.text, fontSize: 36, fontWeight: '900', lineHeight: 40 },
  calLabel: { color: colors.textDim, fontSize: 14 },
  remaining: { fontSize: 15, fontWeight: '700' },
  macros: { flexDirection: 'row', gap: 12 },
  macro: { flex: 1, gap: 4 },
  macroLabel: { fontSize: 12, fontWeight: '700' },
  macroValue: { color: colors.text, fontSize: 16, fontWeight: '800' },
  macroGoal: { color: colors.textMuted, fontSize: 12, fontWeight: '500' },
  mealHeader: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  mealTitle: { color: colors.text, fontSize: 16, fontWeight: '700', flex: 1 },
  mealKcal: { color: colors.textDim, fontSize: 14, fontWeight: '600' },
  food: { flexDirection: 'row', alignItems: 'center', gap: 10, paddingTop: 8, borderTopWidth: 1, borderTopColor: colors.border },
  foodName: { color: colors.text, fontSize: 15, fontWeight: '600' },
  foodGrams: { color: colors.textMuted, fontWeight: '400' },
  foodMacros: { color: colors.textMuted, fontSize: 12, marginTop: 2 },
  foodKcal: { color: colors.text, fontSize: 15, fontWeight: '700' },
});
