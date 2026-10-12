import Ionicons from '@expo/vector-icons/Ionicons';
import { router } from 'expo-router';
import { useMemo, useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';

import { confirm } from '@/components/confirm';
import { DaySwitcher } from '@/components/DaySwitcher';
import { ExerciseModal, type ExerciseValues } from '@/components/ExerciseModal';
import { useToast } from '@/components/Feedback';
import { Button, Card, Chip, Empty, IconButton, Screen, SectionTitle } from '@/components/ui';
import { getExercise } from '@/data/exercises';
import { MUSCLES } from '@/data/muscles';
import { getProgram } from '@/data/programs';
import { formatAmount, formatNumber } from '@/logic/format';
import { workoutXp } from '@/logic/xp';
import { useStore } from '@/store/AppStore';
import { colors, radius } from '@/theme';
import type { PlannedExercise } from '@/types';

interface ModalConfig {
  exerciseId: string | null;
  planned?: PlannedExercise;
}

export default function WorkoutScreen() {
  const { state, today, addWorkout, removeWorkout, setProgramDay } = useStore();
  const toast = useToast();
  // null — «сегодня»: после полуночи экран сам переключится на новый день.
  const [picked, setPicked] = useState<string | null>(null);
  const date = picked && picked < today ? picked : today;
  const setDate = (d: string) => setPicked(d >= today ? null : d);
  const [modal, setModal] = useState<ModalConfig | null>(null);
  const [modalKey, setModalKey] = useState(0);
  const openModal = (config: ModalConfig) => {
    setModal(config);
    setModalKey((k) => k + 1);
  };

  const entries = useMemo(
    () => state.workouts.filter((w) => w.date === date).sort((a, b) => a.createdAt - b.createdAt),
    [state.workouts, date],
  );
  const doneIds = new Set(entries.map((e) => e.exerciseId));

  const summary = entries.reduce(
    (acc, e) => {
      const ex = getExercise(e.exerciseId);
      if (ex?.kind === 'cardio') acc.minutes += e.durationMin;
      else acc.sets += e.sets;
      if (ex?.kind === 'strength') acc.tonnage += e.sets * e.reps * e.weight;
      acc.xp += workoutXp(e);
      return acc;
    },
    { sets: 0, minutes: 0, tonnage: 0, xp: 0 },
  );

  const program = getProgram(state.activeProgramId);
  const dayIndex = program ? Math.min(state.programProgress[program.id] ?? 0, program.days.length - 1) : 0;
  const day = program?.days[dayIndex];

  const log = (exerciseId: string, v: ExerciseValues) => {
    addWorkout({ date, exerciseId, ...v });
    toast(`+${workoutXp({ exerciseId, sets: v.sets, durationMin: v.durationMin })} XP · ${getExercise(exerciseId)?.name}`);
  };

  const quickLog = (p: PlannedExercise) =>
    log(p.exerciseId, {
      sets: p.sets,
      reps: p.reps ?? 0,
      weight: p.weight ?? 0,
      durationMin: p.durationMin ?? 0,
      distanceKm: p.distanceKm ?? 0,
    });

  const finishDay = () => {
    if (!program) return;
    const next = (dayIndex + 1) % program.days.length;
    setProgramDay(program.id, next);
    toast(`День завершён! Следующий: ${program.days[next].name}`);
  };

  return (
    <Screen title="Тренировка" subtitle="Записывай всё, что сделал — мышцы на аватаре окрасятся">
      <DaySwitcher date={date} today={today} onChange={setDate} />

      <View style={styles.summary}>
        <SummaryItem value={entries.length} label="упражн." />
        <SummaryItem value={summary.sets} label="подходов" />
        <SummaryItem value={summary.minutes} label="мин кардио" />
        <SummaryItem value={formatNumber(summary.tonnage)} label="кг тоннаж" />
      </View>

      <Button title="Добавить упражнение" icon="add-circle" onPress={() => openModal({ exerciseId: null })} />

      {program && day ? (
        <Card glow={colors.primaryDim}>
          <SectionTitle right={<Text style={styles.programLink} onPress={() => router.push(`/program/${program.id}`)}>Подробнее</Text>}>
            {program.emoji} {program.name}
          </SectionTitle>
          {program.days.length > 1 ? (
            <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: 6 }}>
              {program.days.map((d, i) => (
                <Chip key={d.name} label={d.name} active={i === dayIndex} onPress={() => setProgramDay(program.id, i)} />
              ))}
            </ScrollView>
          ) : null}
          {day.exercises.map((p) => {
            const ex = getExercise(p.exerciseId);
            if (!ex) return null;
            const done = doneIds.has(p.exerciseId);
            return (
              <Pressable key={p.exerciseId} style={styles.planRow} onPress={() => openModal({ exerciseId: p.exerciseId, planned: p })}>
                <Ionicons name={done ? 'checkmark-circle' : 'ellipse-outline'} size={24} color={done ? colors.fresh : colors.textMuted} />
                <View style={{ flex: 1 }}>
                  <Text style={[styles.planName, done && styles.planDone]}>{ex.name}</Text>
                  <Text style={styles.planSub}>{formatAmount(ex, p)}</Text>
                </View>
                {done ? null : <Button small variant="secondary" title="Готово" onPress={() => quickLog(p)} />}
              </Pressable>
            );
          })}
          <Button
            title={program.days.length > 1 ? `Завершить день → ${program.days[(dayIndex + 1) % program.days.length].name}` : 'Завершить день'}
            variant="ghost"
            icon="flag"
            disabled={!day.exercises.some((p) => doneIds.has(p.exerciseId))}
            onPress={finishDay}
          />
        </Card>
      ) : (
        <Card>
          <Text style={styles.noProgram}>Выбери программу — и здесь появится план на день с чек-листом упражнений.</Text>
          <Button title="Выбрать программу" variant="secondary" icon="list" onPress={() => router.navigate('/programs')} />
        </Card>
      )}

      <Card>
        <SectionTitle right={summary.xp ? <Text style={styles.xp}>+{summary.xp} XP</Text> : null}>Выполнено</SectionTitle>
        {entries.length === 0 ? (
          <Empty icon="barbell-outline" text="Пока пусто. Добавь первое упражнение!" />
        ) : (
          entries.map((e) => {
            const ex = getExercise(e.exerciseId);
            if (!ex) return null;
            return (
              <View key={e.id} style={styles.entry}>
                <View style={{ flex: 1, gap: 2 }}>
                  <Text style={styles.entryName}>{ex.name}</Text>
                  <Text style={styles.entryAmount}>{formatAmount(ex, e)}</Text>
                  <Text style={styles.entryMuscles} numberOfLines={1}>
                    {ex.primary.map((m) => MUSCLES[m].name).join(', ')}
                  </Text>
                </View>
                <Text style={styles.entryXp}>+{workoutXp(e)}</Text>
                <IconButton
                  icon="trash-outline"
                  label="Удалить"
                  onPress={async () => {
                    if (await confirm('Удалить упражнение?', ex.name)) removeWorkout(e.id);
                  }}
                />
              </View>
            );
          })
        )}
      </Card>

      <ExerciseModal
        key={modalKey}
        visible={modal !== null}
        exerciseId={modal?.exerciseId}
        planned={modal?.planned}
        onClose={() => setModal(null)}
        onSave={(id, v) => {
          log(id, v);
          setModal(null);
        }}
      />
    </Screen>
  );
}

function SummaryItem({ value, label }: { value: number | string; label: string }) {
  return (
    <View style={styles.summaryItem}>
      <Text style={styles.summaryValue}>{value}</Text>
      <Text style={styles.summaryLabel}>{label}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  summary: { flexDirection: 'row', gap: 8 },
  summaryItem: {
    flex: 1,
    backgroundColor: colors.surface,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.border,
    paddingVertical: 10,
    alignItems: 'center',
  },
  summaryValue: { color: colors.text, fontSize: 18, fontWeight: '800' },
  summaryLabel: { color: colors.textMuted, fontSize: 11, marginTop: 2 },
  programLink: { color: colors.primary, fontWeight: '600', fontSize: 14 },
  planRow: { flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 4 },
  planName: { color: colors.text, fontSize: 15, fontWeight: '600' },
  planDone: { color: colors.textMuted, textDecorationLine: 'line-through' },
  planSub: { color: colors.textDim, fontSize: 13 },
  noProgram: { color: colors.textDim, fontSize: 14, lineHeight: 20 },
  xp: { color: colors.xp, fontWeight: '700' },
  entry: { flexDirection: 'row', alignItems: 'center', gap: 10, paddingVertical: 8, borderTopWidth: 1, borderTopColor: colors.border },
  entryName: { color: colors.text, fontSize: 15, fontWeight: '700' },
  entryAmount: { color: colors.primary, fontSize: 14, fontWeight: '600' },
  entryMuscles: { color: colors.textMuted, fontSize: 12 },
  entryXp: { color: colors.xp, fontWeight: '700', fontSize: 13 },
});
