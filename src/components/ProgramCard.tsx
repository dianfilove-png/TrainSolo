import Ionicons from '@expo/vector-icons/Ionicons';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { getExercise } from '../data/exercises';
import { MUSCLES } from '../data/muscles';
import { colors, radius } from '../theme';
import type { MuscleId, Program } from '../types';
import { Badge } from './ui';

export const LEVEL_COLORS: Record<Program['level'], string> = {
  Новичок: colors.fresh,
  Средний: colors.primary,
  Продвинутый: colors.accent,
  Хардкор: colors.loaded,
};

/** Мышцы, которые программа нагружает как основные. */
export function programMuscles(program: Program): MuscleId[] {
  const set = new Set<MuscleId>();
  program.days.forEach((d) => d.exercises.forEach((p) => getExercise(p.exerciseId)?.primary.forEach((m) => set.add(m))));
  return [...set];
}

export function ProgramCard({ program, active, onPress }: { program: Program; active: boolean; onPress: () => void }) {
  return (
    <Pressable
      onPress={onPress}
      style={({ pressed }) => [styles.card, active && { borderColor: colors.primary }, pressed && { opacity: 0.8 }]}
    >
      <View style={styles.top}>
        <Text style={styles.emoji}>{program.emoji}</Text>
        <View style={{ flex: 1, gap: 4 }}>
          <Text style={styles.name}>{program.name}</Text>
          <View style={styles.badges}>
            <Badge label={program.level} color={LEVEL_COLORS[program.level]} />
            <Badge label={program.daysPerWeek} color={colors.textDim} />
            {active ? <Badge label="Активна" color={colors.primary} /> : null}
          </View>
        </View>
        <Ionicons name="chevron-forward" size={20} color={colors.textMuted} />
      </View>
      <Text style={styles.description} numberOfLines={3}>
        {program.description}
      </Text>
      <Text style={styles.muscles} numberOfLines={2}>
        {programMuscles(program)
          .map((m) => MUSCLES[m].name)
          .join(' · ')}
      </Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.surface,
    borderRadius: radius.lg,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 16,
    gap: 10,
  },
  top: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  emoji: { fontSize: 32 },
  name: { color: colors.text, fontSize: 17, fontWeight: '800' },
  badges: { flexDirection: 'row', flexWrap: 'wrap', gap: 6 },
  description: { color: colors.textDim, fontSize: 14, lineHeight: 20 },
  muscles: { color: colors.textMuted, fontSize: 12 },
});
