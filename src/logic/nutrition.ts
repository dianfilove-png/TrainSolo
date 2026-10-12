import type { FoodPreset } from '../data/foods';
import type { Activity, FoodEntry, Goal, Profile } from '../types';

export interface Macros {
  calories: number;
  protein: number;
  fat: number;
  carbs: number;
}

const ACTIVITY_FACTOR: Record<Activity, number> = {
  low: 1.2,
  light: 1.375,
  moderate: 1.55,
  high: 1.725,
  extreme: 1.9,
};

const GOAL_FACTOR: Record<Goal, number> = { lose: 0.85, maintain: 1, gain: 1.1 };
const PROTEIN_PER_KG: Record<Goal, number> = { lose: 2, maintain: 1.6, gain: 1.8 };

export const ACTIVITY_LABELS: Record<Activity, string> = {
  low: 'Сидячий образ жизни',
  light: '1–2 тренировки в неделю',
  moderate: '3–4 тренировки в неделю',
  high: '5–6 тренировок в неделю',
  extreme: 'Каждый день / тяжёлый труд',
};

export const GOAL_LABELS: Record<Goal, string> = {
  lose: 'Похудеть',
  maintain: 'Поддерживать форму',
  gain: 'Набрать массу',
};

/** Базовый обмен по формуле Миффлина — Сан Жеора. */
export function bmr(p: Pick<Profile, 'sex' | 'age' | 'heightCm' | 'weightKg'>): number {
  const base = 10 * p.weightKg + 6.25 * p.heightCm - 5 * p.age;
  return p.sex === 'male' ? base + 5 : base - 161;
}

export function autoCalories(p: Profile): number {
  return Math.round((bmr(p) * ACTIVITY_FACTOR[p.activity] * GOAL_FACTOR[p.goal]) / 10) * 10;
}

export function nutritionGoals(p: Profile): Macros {
  const calories = p.customCalories && p.customCalories > 0 ? p.customCalories : autoCalories(p);
  const protein = Math.round(p.weightKg * PROTEIN_PER_KG[p.goal]);
  const fat = Math.round((calories * 0.25) / 9);
  const carbs = Math.max(0, Math.round((calories - protein * 4 - fat * 9) / 4));
  return { calories, protein, fat, carbs };
}

export function sumFoods(foods: FoodEntry[]): Macros {
  return foods.reduce<Macros>(
    (acc, f) => ({
      calories: acc.calories + f.calories,
      protein: acc.protein + f.protein,
      fat: acc.fat + f.fat,
      carbs: acc.carbs + f.carbs,
    }),
    { calories: 0, protein: 0, fat: 0, carbs: 0 },
  );
}

const round1 = (n: number) => Math.round(n * 10) / 10;

export function scalePreset(preset: FoodPreset, grams: number): Macros {
  const k = grams / 100;
  return {
    calories: Math.round(preset.calories * k),
    protein: round1(preset.protein * k),
    fat: round1(preset.fat * k),
    carbs: round1(preset.carbs * k),
  };
}
