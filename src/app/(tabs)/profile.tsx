import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';

import { confirm } from '@/components/confirm';
import { useToast } from '@/components/Feedback';
import { Button, Card, Chip, Field, ProgressBar, Screen, SectionTitle, Stepper, parseNumber } from '@/components/ui';
import { getAchievements } from '@/logic/achievements';
import { formatNumber } from '@/logic/format';
import { ACTIVITY_LABELS, GOAL_LABELS, autoCalories } from '@/logic/nutrition';
import { useStore } from '@/store/AppStore';
import { colors, radius } from '@/theme';
import type { Activity, Goal } from '@/types';

export default function ProfileScreen() {
  const { state, level, goals, totals, updateProfile, reset } = useStore();
  const toast = useToast();
  const p = state.profile;
  const [customKcal, setCustomKcal] = useState(p.customCalories ? String(p.customCalories) : '');

  const achievements = getAchievements(totals, level.level, state.workouts);
  const unlocked = achievements.filter((a) => a.done).length;

  const stats: [string, string][] = [
    ['Всего опыта', `${formatNumber(level.totalXp)} XP`],
    ['Тренировочных дней', String(totals.workoutDays)],
    ['Подходов', formatNumber(totals.totalSets)],
    ['Тоннаж', `${formatNumber(totals.tonnage)} кг`],
    ['Кардио', `${formatNumber(totals.cardioMinutes)} мин · ${String(totals.cardioKm).replace('.', ',')} км`],
    ['Лучшая серия', `${totals.bestStreak} дн.`],
    ['Дней с дневником питания', String(totals.foodDays)],
  ];

  return (
    <Screen title="Профиль" subtitle={`Ур. ${level.level} · Ранг ${level.rank.letter}`}>
      <Card>
        <SectionTitle>Герой</SectionTitle>
        <Field label="Имя" value={p.name} onChangeText={(name) => updateProfile({ name })} placeholder="Охотник" />
        <View style={styles.chips}>
          <Chip label="Мужчина" active={p.sex === 'male'} onPress={() => updateProfile({ sex: 'male' })} />
          <Chip label="Женщина" active={p.sex === 'female'} onPress={() => updateProfile({ sex: 'female' })} />
        </View>
        <View style={styles.row}>
          <Stepper label="Возраст" value={p.age} min={10} onChange={(age) => updateProfile({ age: Math.round(age) })} />
          <Stepper label="Рост" suffix="см" value={p.heightCm} min={100} onChange={(heightCm) => updateProfile({ heightCm })} />
        </View>
        <Stepper label="Вес" suffix="кг" value={p.weightKg} min={30} step={0.5} decimals onChange={(weightKg) => updateProfile({ weightKg })} />
      </Card>

      <Card>
        <SectionTitle>Активность и цель</SectionTitle>
        <Text style={styles.label}>Активность</Text>
        <View style={styles.chips}>
          {(Object.keys(ACTIVITY_LABELS) as Activity[]).map((a) => (
            <Chip key={a} label={ACTIVITY_LABELS[a]} active={p.activity === a} onPress={() => updateProfile({ activity: a })} />
          ))}
        </View>
        <Text style={styles.label}>Цель</Text>
        <View style={styles.chips}>
          {(Object.keys(GOAL_LABELS) as Goal[]).map((g) => (
            <Chip key={g} label={GOAL_LABELS[g]} active={p.goal === g} color={colors.xp} onPress={() => updateProfile({ goal: g })} />
          ))}
        </View>
        <Text style={styles.label}>Дневная норма</Text>
        <View style={styles.goals}>
          <GoalItem label="Калории" value={`${goals.calories}`} color={colors.xp} />
          <GoalItem label="Белки" value={`${goals.protein} г`} color={colors.protein} />
          <GoalItem label="Жиры" value={`${goals.fat} г`} color={colors.fat} />
          <GoalItem label="Углеводы" value={`${goals.carbs} г`} color={colors.carbs} />
        </View>
        <Field
          label={`Своя норма калорий (авто: ${autoCalories(p)})`}
          value={customKcal}
          numeric
          placeholder="Пусто — считать автоматически"
          onChangeText={(t) => {
            setCustomKcal(t);
            const n = Math.round(parseNumber(t));
            updateProfile({ customCalories: n >= 800 ? n : null });
          }}
        />
        <Text style={styles.note}>Норма считается по формуле Миффлина — Сан Жеора с учётом активности и цели.</Text>
      </Card>

      <Card>
        <SectionTitle right={<Text style={styles.counter}>{unlocked}/{achievements.length}</Text>}>Достижения</SectionTitle>
        <View style={styles.grid}>
          {achievements.map((a) => (
            <View key={a.id} style={[styles.achievement, a.done && { borderColor: colors.xp, backgroundColor: '#1F1A0B' }]}>
              <Text style={[styles.achEmoji, !a.done && { opacity: 0.35 }]}>{a.emoji}</Text>
              <Text style={[styles.achTitle, !a.done && { color: colors.textDim }]}>{a.title}</Text>
              <Text style={styles.achDesc}>{a.description}</Text>
              {!a.done && a.target > 1 ? <ProgressBar value={a.current / a.target} height={4} color={colors.textDim} /> : null}
            </View>
          ))}
        </View>
      </Card>

      <Card>
        <SectionTitle>Статистика</SectionTitle>
        {stats.map(([label, value]) => (
          <View key={label} style={styles.statRow}>
            <Text style={styles.statLabel}>{label}</Text>
            <Text style={styles.statValue}>{value}</Text>
          </View>
        ))}
      </Card>

      <Button
        title="Сбросить весь прогресс"
        variant="danger"
        icon="refresh"
        onPress={async () => {
          if (await confirm('Сбросить прогресс?', 'Все тренировки, питание и опыт будут удалены без возможности восстановления.', 'Сбросить')) {
            reset();
            setCustomKcal('');
            toast('Прогресс сброшен');
          }
        }}
      />
    </Screen>
  );
}

function GoalItem({ label, value, color }: { label: string; value: string; color: string }) {
  return (
    <View style={styles.goalItem}>
      <Text style={[styles.goalLabel, { color }]}>{label}</Text>
      <Text style={styles.goalValue}>{value}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', gap: 12 },
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: 6 },
  goals: { flexDirection: 'row', gap: 8 },
  goalItem: { flex: 1, backgroundColor: colors.surfaceAlt, borderRadius: radius.sm, paddingVertical: 8, alignItems: 'center' },
  goalLabel: { fontSize: 11, fontWeight: '700' },
  goalValue: { color: colors.text, fontSize: 16, fontWeight: '800' },
  label: { color: colors.textDim, fontSize: 13, fontWeight: '600', marginBottom: -4 },
  note: { color: colors.textMuted, fontSize: 12 },
  counter: { color: colors.xp, fontWeight: '700' },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  achievement: {
    width: '48%',
    flexGrow: 1,
    backgroundColor: colors.surfaceAlt,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 10,
    gap: 4,
  },
  achEmoji: { fontSize: 24 },
  achTitle: { color: colors.text, fontSize: 14, fontWeight: '800' },
  achDesc: { color: colors.textMuted, fontSize: 12, lineHeight: 16 },
  statRow: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 4 },
  statLabel: { color: colors.textDim, fontSize: 14 },
  statValue: { color: colors.text, fontSize: 14, fontWeight: '700' },
});
