import Ionicons from '@expo/vector-icons/Ionicons';
import { useMemo, useState } from 'react';
import { FlatList, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';

import { EXERCISES, getExercise } from '../data/exercises';
import { MUSCLES, MUSCLE_IDS } from '../data/muscles';
import { workoutXp } from '../logic/xp';
import { colors, radius } from '../theme';
import type { Exercise, MuscleId, PlannedExercise } from '../types';
import { Sheet } from './Sheet';
import { Badge, Button, Chip, Stepper } from './ui';

export interface ExerciseValues {
  sets: number;
  reps: number;
  weight: number;
  durationMin: number;
  distanceKm: number;
}

export function defaultValues(exercise: Exercise, planned?: Partial<PlannedExercise>): ExerciseValues {
  const cardio = exercise.kind === 'cardio';
  return {
    sets: planned?.sets ?? (cardio ? 1 : 3),
    reps: planned?.reps ?? (cardio ? 0 : exercise.unit === 'sec' ? 30 : 10),
    weight: planned?.weight ?? (exercise.kind === 'strength' ? 20 : 0),
    durationMin: planned?.durationMin ?? (cardio ? 30 : 0),
    distanceKm: planned?.distanceKm ?? 0,
  };
}

export function MuscleTags({ exercise }: { exercise: Exercise }) {
  return (
    <View style={styles.tags}>
      {exercise.primary.map((m) => (
        <Badge key={m} label={MUSCLES[m].name} color={colors.loaded} />
      ))}
      {exercise.secondary.map((m) => (
        <Badge key={m} label={MUSCLES[m].name} color={colors.textDim} />
      ))}
    </View>
  );
}

interface Props {
  visible: boolean;
  /** Если задано — сразу открывается форма ввода для этого упражнения. */
  exerciseId?: string | null;
  planned?: Partial<PlannedExercise>;
  onClose: () => void;
  onSave: (exerciseId: string, values: ExerciseValues) => void;
}

export function ExerciseModal({ visible, exerciseId, planned, onClose, onSave }: Props) {
  // Родитель меняет `key` при каждом открытии, поэтому начальное состояние берётся из пропсов.
  const [picked, setPicked] = useState<Exercise | null>(() => (exerciseId ? getExercise(exerciseId) ?? null : null));
  const [values, setValues] = useState<ExerciseValues | null>(() => (picked ? defaultValues(picked, planned) : null));
  const [fixed] = useState(Boolean(exerciseId));

  const pick = (ex: Exercise) => {
    setPicked(ex);
    setValues(defaultValues(ex));
  };

  return (
    <Sheet visible={visible} title={picked ? picked.name : 'Выбери упражнение'} onClose={onClose}>
      {picked && values ? (
        <LogForm
          exercise={picked}
          values={values}
          onChange={setValues}
          onBack={fixed ? undefined : () => setPicked(null)}
          onSave={() => onSave(picked.id, values)}
        />
      ) : (
        <Picker onPick={pick} />
      )}
    </Sheet>
  );
}

function Picker({ onPick }: { onPick: (ex: Exercise) => void }) {
  const [query, setQuery] = useState('');
  const [muscle, setMuscle] = useState<MuscleId | 'cardio' | null>(null);

  const list = useMemo(() => {
    const q = query.trim().toLowerCase();
    return EXERCISES.filter((e) => {
      if (q && !e.name.toLowerCase().includes(q)) return false;
      if (muscle === 'cardio') return e.kind === 'cardio';
      if (muscle) return e.primary.includes(muscle) || e.secondary.includes(muscle);
      return true;
    });
  }, [query, muscle]);

  return (
    <View style={{ flexShrink: 1, gap: 10 }}>
      <View style={styles.search}>
        <Ionicons name="search" size={18} color={colors.textMuted} />
        <TextInput
          style={styles.searchInput}
          value={query}
          onChangeText={setQuery}
          placeholder="Поиск упражнения"
          placeholderTextColor={colors.textMuted}
        />
      </View>
      <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: 6 }} style={{ flexGrow: 0 }}>
        <Chip label="Все" active={muscle === null} onPress={() => setMuscle(null)} />
        <Chip label="Кардио" active={muscle === 'cardio'} onPress={() => setMuscle('cardio')} />
        {MUSCLE_IDS.map((m) => (
          <Chip key={m} label={MUSCLES[m].name} active={muscle === m} onPress={() => setMuscle(m)} />
        ))}
      </ScrollView>
      <FlatList
        data={list}
        keyExtractor={(e) => e.id}
        style={{ flexShrink: 1, minHeight: 200 }}
        keyboardShouldPersistTaps="handled"
        renderItem={({ item }) => (
          <Pressable style={({ pressed }) => [styles.item, pressed && { backgroundColor: colors.surfaceAlt }]} onPress={() => onPick(item)}>
            <View style={{ flex: 1, gap: 4 }}>
              <Text style={styles.itemTitle}>{item.name}</Text>
              <Text style={styles.itemSub} numberOfLines={1}>
                {item.kind === 'cardio' ? 'Кардио · ' : ''}
                {item.primary.map((m) => MUSCLES[m].name).join(', ')}
              </Text>
            </View>
            <Ionicons name="add-circle" size={26} color={colors.primary} />
          </Pressable>
        )}
        ListEmptyComponent={<Text style={styles.itemSub}>Ничего не найдено</Text>}
      />
    </View>
  );
}

function LogForm({
  exercise,
  values,
  onChange,
  onBack,
  onSave,
}: {
  exercise: Exercise;
  values: ExerciseValues;
  onChange: (v: ExerciseValues) => void;
  onBack?: () => void;
  onSave: () => void;
}) {
  const set = (patch: Partial<ExerciseValues>) => onChange({ ...values, ...patch });
  const xp = workoutXp({ exerciseId: exercise.id, sets: values.sets, durationMin: values.durationMin });
  const valid = exercise.kind === 'cardio' ? values.durationMin > 0 : values.sets > 0 && values.reps > 0;

  return (
    <ScrollView style={{ flexShrink: 1 }} contentContainerStyle={{ gap: 16 }} keyboardShouldPersistTaps="handled">
      <View style={{ gap: 6 }}>
        <Text style={styles.hint}>Нагружаемые мышцы (красные — основные):</Text>
        <MuscleTags exercise={exercise} />
      </View>

      {exercise.kind === 'cardio' ? (
        <View style={styles.row}>
          <Stepper label="Время" suffix="мин" value={values.durationMin} step={5} onChange={(v) => set({ durationMin: v })} />
          <Stepper label="Дистанция" suffix="км" value={values.distanceKm} step={0.5} decimals onChange={(v) => set({ distanceKm: v })} />
        </View>
      ) : (
        <>
          <View style={styles.row}>
            <Stepper label="Подходы" value={values.sets} onChange={(v) => set({ sets: Math.round(v) })} />
            <Stepper
              label={exercise.unit === 'sec' ? 'Секунды' : 'Повторы'}
              value={values.reps}
              step={exercise.unit === 'sec' ? 5 : 1}
              onChange={(v) => set({ reps: Math.round(v) })}
            />
          </View>
          {exercise.kind === 'strength' ? (
            <Stepper label="Вес" suffix="кг" value={values.weight} step={2.5} decimals onChange={(v) => set({ weight: v })} />
          ) : null}
        </>
      )}

      <View style={styles.xpRow}>
        <Ionicons name="sparkles" size={16} color={colors.xp} />
        <Text style={styles.xpText}>Награда: +{xp} XP</Text>
      </View>

      <View style={styles.row}>
        {onBack ? <Button title="Назад" variant="secondary" onPress={onBack} style={{ flex: 1 }} /> : null}
        <Button title="Записать" icon="checkmark" onPress={onSave} disabled={!valid} style={{ flex: 2 }} />
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  tags: { flexDirection: 'row', flexWrap: 'wrap', gap: 6 },
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
  hint: { color: colors.textDim, fontSize: 13 },
  row: { flexDirection: 'row', gap: 12 },
  xpRow: { flexDirection: 'row', alignItems: 'center', gap: 6, justifyContent: 'center' },
  xpText: { color: colors.xp, fontWeight: '700', fontSize: 15 },
});
