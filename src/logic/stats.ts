import { getExercise } from '../data/exercises';
import { MUSCLE_IDS } from '../data/muscles';
import type { AppState, MuscleId } from '../types';
import { addDays, daysBetween } from './date';
import { nutritionGoals, sumFoods } from './nutrition';

export interface Totals {
  workoutDays: number;
  totalSets: number;
  /** Суммарный тоннаж (подходы × повторы × вес), кг. */
  tonnage: number;
  cardioMinutes: number;
  cardioKm: number;
  foodDays: number;
  /** Дни, когда калории попали в коридор 90–110% от нормы. */
  onTargetDays: number;
  activeDays: number;
  currentStreak: number;
  bestStreak: number;
  musclesTrained: Set<MuscleId>;
}

export function computeTotals(state: AppState, today: string): Totals {
  const workoutDates = new Set<string>();
  const foodDates = new Set<string>();
  const musclesTrained = new Set<MuscleId>();
  let totalSets = 0;
  let tonnage = 0;
  let cardioMinutes = 0;
  let cardioKm = 0;

  for (const w of state.workouts) {
    const exercise = getExercise(w.exerciseId);
    if (!exercise) continue;
    workoutDates.add(w.date);
    exercise.primary.forEach((m) => musclesTrained.add(m));
    if (exercise.kind === 'cardio') {
      cardioMinutes += w.durationMin;
      cardioKm += w.distanceKm;
    } else {
      totalSets += w.sets;
      if (exercise.kind === 'strength') tonnage += w.sets * w.reps * w.weight;
    }
  }

  const foodsByDate = new Map<string, AppState['foods']>();
  for (const f of state.foods) {
    foodDates.add(f.date);
    foodsByDate.set(f.date, [...(foodsByDate.get(f.date) ?? []), f]);
  }
  const goalCalories = nutritionGoals(state.profile).calories;
  let onTargetDays = 0;
  for (const foods of foodsByDate.values()) {
    const ratio = sumFoods(foods).calories / goalCalories;
    if (ratio >= 0.9 && ratio <= 1.1) onTargetDays++;
  }

  const active = new Set([...workoutDates, ...foodDates]);
  return {
    workoutDays: workoutDates.size,
    totalSets,
    tonnage: Math.round(tonnage),
    cardioMinutes,
    cardioKm: Math.round(cardioKm * 10) / 10,
    foodDays: foodDates.size,
    onTargetDays,
    activeDays: active.size,
    currentStreak: currentStreak(active, today),
    bestStreak: bestStreak(active),
    musclesTrained,
  };
}

/** Серия подряд идущих активных дней. Если сегодня ещё ничего не записано, серия не сгорает. */
export function currentStreak(active: Set<string>, today: string): number {
  let day = active.has(today) ? today : addDays(today, -1);
  let streak = 0;
  while (active.has(day)) {
    streak++;
    day = addDays(day, -1);
  }
  return streak;
}

export function bestStreak(active: Set<string>): number {
  const days = [...active].sort();
  let best = 0;
  let run = 0;
  for (let i = 0; i < days.length; i++) {
    run = i > 0 && daysBetween(days[i - 1], days[i]) === 1 ? run + 1 : 1;
    best = Math.max(best, run);
  }
  return best;
}

export interface CharacterStat {
  key: string;
  label: string;
  value: number;
  color: string;
  hint: string;
}

export function characterStats(t: Totals): CharacterStat[] {
  return [
    { key: 'str', label: 'СИЛ', value: 1 + Math.floor(t.totalSets / 20), color: '#F87171', hint: '+1 за каждые 20 подходов' },
    { key: 'end', label: 'ВЫН', value: 1 + Math.floor(t.cardioMinutes / 30), color: '#38BDF8', hint: '+1 за каждые 30 минут кардио' },
    { key: 'dis', label: 'ДИС', value: 1 + Math.floor(t.activeDays / 3), color: '#A78BFA', hint: '+1 за каждые 3 активных дня' },
    { key: 'vit', label: 'ЖИЗ', value: 1 + t.onTargetDays, color: '#34D399', hint: '+1 за день в норме калорий' },
  ];
}

export function allMusclesTrained(t: Totals): boolean {
  return MUSCLE_IDS.every((m) => t.musclesTrained.has(m));
}
