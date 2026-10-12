import { getExercise } from '../data/exercises';
import { MUSCLE_IDS } from '../data/muscles';
import { colors } from '../theme';
import type { MuscleId, WorkoutEntry } from '../types';
import { daysBetween } from './date';

/** loaded — тренировалась сегодня, recovering — вчера, fresh — восстановилась. */
export type MuscleStatus = 'loaded' | 'recovering' | 'fresh';

export interface MuscleState {
  status: MuscleStatus;
  lastTrained: string | null;
  daysSince: number | null;
  /** Подходы за последний день нагрузки: целевые мышцы — 1, вспомогательные — 0.5. */
  sets: number;
}

export const STATUS_INFO: Record<MuscleStatus, { label: string; short: string; color: string }> = {
  loaded: { label: 'Нагружена сегодня', short: 'Сегодня', color: colors.loaded },
  recovering: { label: 'Восстанавливается', short: 'Вчера', color: colors.recovering },
  fresh: { label: 'Готова к нагрузке', short: 'Готовы', color: colors.fresh },
};

export function statusForDays(daysSince: number | null): MuscleStatus {
  if (daysSince === 0) return 'loaded';
  if (daysSince === 1) return 'recovering';
  return 'fresh';
}

export function getMuscleStates(workouts: WorkoutEntry[], today: string): Record<MuscleId, MuscleState> {
  const last = {} as Record<MuscleId, { date: string; sets: number }>;

  const touch = (muscle: MuscleId, date: string, sets: number) => {
    const prev = last[muscle];
    if (!prev || date > prev.date) last[muscle] = { date, sets };
    else if (date === prev.date) prev.sets += sets;
  };

  for (const entry of workouts) {
    if (entry.date > today) continue;
    const exercise = getExercise(entry.exerciseId);
    if (!exercise) continue;
    const sets = Math.max(1, entry.sets);
    exercise.primary.forEach((m) => touch(m, entry.date, sets));
    exercise.secondary.forEach((m) => touch(m, entry.date, sets / 2));
  }

  const result = {} as Record<MuscleId, MuscleState>;
  for (const id of MUSCLE_IDS) {
    const info = last[id];
    const daysSince = info ? daysBetween(info.date, today) : null;
    result[id] = {
      status: statusForDays(daysSince),
      lastTrained: info?.date ?? null,
      daysSince,
      sets: info?.sets ?? 0,
    };
  }
  return result;
}
