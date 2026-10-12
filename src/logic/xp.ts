import { getExercise } from '../data/exercises';
import type { AppState, WorkoutEntry } from '../types';

export const XP_PER_SET = 10;
export const XP_PER_CARDIO_MIN = 2;
export const XP_PER_FOOD = 5;
/** Опыт за еду начисляется максимум за столько записей в день. */
export const FOOD_XP_ENTRIES_PER_DAY = 6;

export function workoutXp(entry: Pick<WorkoutEntry, 'exerciseId' | 'sets' | 'durationMin'>): number {
  const exercise = getExercise(entry.exerciseId);
  if (exercise?.kind === 'cardio') return Math.round(entry.durationMin * XP_PER_CARDIO_MIN);
  return Math.max(0, entry.sets) * XP_PER_SET;
}

export function totalXp(state: Pick<AppState, 'workouts' | 'foods' | 'questClaims'>): number {
  let xp = 0;
  for (const w of state.workouts) xp += workoutXp(w);

  const foodPerDay = new Map<string, number>();
  for (const f of state.foods) foodPerDay.set(f.date, (foodPerDay.get(f.date) ?? 0) + 1);
  for (const count of foodPerDay.values()) xp += Math.min(count, FOOD_XP_ENTRIES_PER_DAY) * XP_PER_FOOD;

  for (const q of state.questClaims) xp += q.xp;
  return xp;
}

/** Сколько всего опыта нужно, чтобы достичь уровня `level` (уровень 1 — 0 XP). */
export function xpForLevel(level: number): number {
  const n = Math.max(0, level - 1);
  return 100 * n + 25 * n * Math.max(0, n - 1);
}

export interface Rank {
  letter: string;
  title: string;
  color: string;
  minLevel: number;
}

export const RANKS: Rank[] = [
  { letter: 'E', title: 'Новобранец', color: '#94A3B8', minLevel: 1 },
  { letter: 'D', title: 'Боец', color: '#34D399', minLevel: 5 },
  { letter: 'C', title: 'Воин', color: '#38BDF8', minLevel: 10 },
  { letter: 'B', title: 'Ветеран', color: '#A78BFA', minLevel: 20 },
  { letter: 'A', title: 'Элита', color: '#F472B6', minLevel: 30 },
  { letter: 'S', title: 'Монарх', color: '#FBBF24', minLevel: 45 },
];

export function rankForLevel(level: number): Rank {
  let rank = RANKS[0];
  for (const r of RANKS) if (level >= r.minLevel) rank = r;
  return rank;
}

export interface LevelInfo {
  level: number;
  totalXp: number;
  /** Опыт, набранный на текущем уровне. */
  current: number;
  /** Сколько опыта нужно для перехода на следующий уровень. */
  needed: number;
  progress: number;
  rank: Rank;
}

export function levelInfo(xp: number): LevelInfo {
  let level = 1;
  while (xpForLevel(level + 1) <= xp) level++;
  const start = xpForLevel(level);
  const needed = xpForLevel(level + 1) - start;
  const current = xp - start;
  return { level, totalXp: xp, current, needed, progress: current / needed, rank: rankForLevel(level) };
}
