import Ionicons from '@expo/vector-icons/Ionicons';
import { useMemo, useState } from 'react';
import { FlatList, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';

import { FOOD_PRESETS, type FoodPreset } from '../data/foods';
import { scalePreset, type Macros } from '../logic/nutrition';
import { colors, radius } from '../theme';
import type { FoodEntry, MealType } from '../types';
import { Sheet } from './Sheet';
import { Button, Chip, Field, Stepper, parseNumber } from './ui';

export const MEAL_LABELS: Record<MealType, string> = {
  breakfast: 'Завтрак',
  lunch: 'Обед',
  dinner: 'Ужин',
  snack: 'Перекус',
};

export const MEALS: MealType[] = ['breakfast', 'lunch', 'dinner', 'snack'];

export type FoodDraft = Omit<FoodEntry, 'id' | 'createdAt' | 'date'>;

type Tab = 'catalog' | 'recent' | 'manual';

interface Props {
  visible: boolean;
  meal: MealType;
  recent: FoodDraft[];
  onClose: () => void;
  onSave: (food: FoodDraft) => void;
}

export function FoodModal({ visible, meal: initialMeal, recent, onClose, onSave }: Props) {
  // Родитель меняет `key` при каждом открытии, поэтому состояние сбрасывается само.
  const [meal, setMeal] = useState<MealType>(initialMeal);
  const [tab, setTab] = useState<Tab>('catalog');
  const [preset, setPreset] = useState<FoodPreset | null>(null);

  const save = (food: Omit<FoodDraft, 'meal'>) => onSave({ ...food, meal });

  return (
    <Sheet visible={visible} title={preset ? preset.name : 'Добавить еду'} onClose={onClose}>
      <View style={{ flexShrink: 1, gap: 12 }}>
        <View style={styles.chips}>
          {MEALS.map((m) => (
            <Chip key={m} label={MEAL_LABELS[m]} active={meal === m} onPress={() => setMeal(m)} />
          ))}
        </View>

        {preset ? (
          <PresetForm preset={preset} onBack={() => setPreset(null)} onSave={save} />
        ) : (
          <>
            <View style={styles.tabs}>
              {(
                [
                  ['catalog', 'Каталог'],
                  ['recent', 'Недавние'],
                  ['manual', 'Вручную'],
                ] as [Tab, string][]
              ).map(([key, label]) => (
                <Pressable key={key} onPress={() => setTab(key)} style={[styles.tab, tab === key && styles.tabActive]}>
                  <Text style={[styles.tabText, tab === key && styles.tabTextActive]}>{label}</Text>
                </Pressable>
              ))}
            </View>
            {tab === 'catalog' ? <Catalog onPick={setPreset} /> : null}
            {tab === 'recent' ? <Recent items={recent} onPick={save} /> : null}
            {tab === 'manual' ? <ManualForm onSave={save} /> : null}
          </>
        )}
      </View>
    </Sheet>
  );
}

function MacroLine({ m }: { m: Macros }) {
  return (
    <Text style={styles.macroLine}>
      <Text style={{ color: colors.text, fontWeight: '700' }}>{Math.round(m.calories)} ккал</Text>
      {'  '}Б {m.protein} · Ж {m.fat} · У {m.carbs}
    </Text>
  );
}

function Catalog({ onPick }: { onPick: (p: FoodPreset) => void }) {
  const [query, setQuery] = useState('');
  const list = useMemo(() => {
    const q = query.trim().toLowerCase();
    return q ? FOOD_PRESETS.filter((f) => f.name.toLowerCase().includes(q)) : FOOD_PRESETS;
  }, [query]);
  return (
    <View style={{ flexShrink: 1, gap: 8 }}>
      <View style={styles.search}>
        <Ionicons name="search" size={18} color={colors.textMuted} />
        <TextInput style={styles.searchInput} value={query} onChangeText={setQuery} placeholder="Поиск продукта" placeholderTextColor={colors.textMuted} />
      </View>
      <FlatList
        data={list}
        keyExtractor={(f) => f.name}
        style={{ flexShrink: 1, minHeight: 200 }}
        keyboardShouldPersistTaps="handled"
        renderItem={({ item }) => (
          <Pressable style={({ pressed }) => [styles.item, pressed && { backgroundColor: colors.surfaceAlt }]} onPress={() => onPick(item)}>
            <View style={{ flex: 1, gap: 2 }}>
              <Text style={styles.itemTitle}>{item.name}</Text>
              <Text style={styles.itemSub}>
                {item.calories} ккал / 100 г · порция {item.portion} г
              </Text>
            </View>
            <Ionicons name="chevron-forward" size={20} color={colors.textMuted} />
          </Pressable>
        )}
        ListEmptyComponent={<Text style={styles.itemSub}>Нет такого продукта — добавь его во вкладке «Вручную»</Text>}
      />
    </View>
  );
}

function PresetForm({ preset, onBack, onSave }: { preset: FoodPreset; onBack: () => void; onSave: (f: Omit<FoodDraft, 'meal'>) => void }) {
  const [grams, setGrams] = useState(preset.portion);
  const macros = scalePreset(preset, grams);
  return (
    <View style={{ gap: 16 }}>
      <Stepper label="Вес порции" suffix="г" value={grams} step={10} onChange={setGrams} />
      <View style={styles.macroBox}>
        <MacroLine m={macros} />
      </View>
      <View style={{ flexDirection: 'row', gap: 12 }}>
        <Button title="Назад" variant="secondary" onPress={onBack} style={{ flex: 1 }} />
        <Button
          title="Добавить"
          icon="checkmark"
          disabled={grams <= 0}
          onPress={() => onSave({ name: preset.name, grams, ...macros })}
          style={{ flex: 2 }}
        />
      </View>
    </View>
  );
}

function Recent({ items, onPick }: { items: FoodDraft[]; onPick: (f: Omit<FoodDraft, 'meal'>) => void }) {
  if (items.length === 0) {
    return <Text style={[styles.itemSub, { textAlign: 'center', paddingVertical: 24 }]}>Здесь появятся продукты, которые ты уже записывал</Text>;
  }
  return (
    <FlatList
      data={items}
      keyExtractor={(f, i) => `${f.name}-${i}`}
      style={{ flexShrink: 1, minHeight: 200 }}
      renderItem={({ item }) => (
        <Pressable style={({ pressed }) => [styles.item, pressed && { backgroundColor: colors.surfaceAlt }]} onPress={() => onPick(item)}>
          <View style={{ flex: 1, gap: 2 }}>
            <Text style={styles.itemTitle}>{item.name}</Text>
            <Text style={styles.itemSub}>
              {item.grams ? `${item.grams} г · ` : ''}
              {Math.round(item.calories)} ккал · Б {item.protein} · Ж {item.fat} · У {item.carbs}
            </Text>
          </View>
          <Ionicons name="add-circle" size={26} color={colors.primary} />
        </Pressable>
      )}
    />
  );
}

function ManualForm({ onSave }: { onSave: (f: Omit<FoodDraft, 'meal'>) => void }) {
  const [name, setName] = useState('');
  const [calories, setCalories] = useState('');
  const [protein, setProtein] = useState('');
  const [fat, setFat] = useState('');
  const [carbs, setCarbs] = useState('');
  const [grams, setGrams] = useState('');
  const valid = name.trim().length > 0 && parseNumber(calories) > 0;

  return (
    <ScrollView style={{ flexShrink: 1 }} contentContainerStyle={{ gap: 12 }} keyboardShouldPersistTaps="handled">
      <Field label="Название" value={name} onChangeText={setName} placeholder="Например, борщ" />
      <View style={styles.row}>
        <Field label="Калории" value={calories} onChangeText={setCalories} placeholder="ккал" numeric />
        <Field label="Вес, г" value={grams} onChangeText={setGrams} placeholder="необяз." numeric />
      </View>
      <View style={styles.row}>
        <Field label="Белки, г" value={protein} onChangeText={setProtein} placeholder="0" numeric />
        <Field label="Жиры, г" value={fat} onChangeText={setFat} placeholder="0" numeric />
        <Field label="Углев., г" value={carbs} onChangeText={setCarbs} placeholder="0" numeric />
      </View>
      <Button
        title="Добавить"
        icon="checkmark"
        disabled={!valid}
        onPress={() =>
          onSave({
            name: name.trim(),
            grams: parseNumber(grams),
            calories: Math.round(parseNumber(calories)),
            protein: parseNumber(protein),
            fat: parseNumber(fat),
            carbs: parseNumber(carbs),
          })
        }
      />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: 6 },
  tabs: { flexDirection: 'row', backgroundColor: colors.surfaceAlt, borderRadius: radius.md, padding: 3 },
  tab: { flex: 1, paddingVertical: 8, borderRadius: radius.sm, alignItems: 'center' },
  tabActive: { backgroundColor: colors.primaryDim },
  tabText: { color: colors.textDim, fontWeight: '600', fontSize: 14 },
  tabTextActive: { color: colors.text },
  search: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    backgroundColor: colors.surfaceAlt,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.border,
    paddingHorizontal: 12,
  },
  searchInput: { flex: 1, color: colors.text, fontSize: 16, paddingVertical: 11 },
  item: { flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 12, paddingHorizontal: 6, borderBottomWidth: 1, borderBottomColor: colors.border },
  itemTitle: { color: colors.text, fontSize: 16, fontWeight: '600' },
  itemSub: { color: colors.textMuted, fontSize: 13 },
  macroBox: { backgroundColor: colors.surfaceAlt, borderRadius: radius.md, padding: 14, alignItems: 'center' },
  macroLine: { color: colors.textDim, fontSize: 15 },
  row: { flexDirection: 'row', gap: 10 },
});
