import type { AppState, FoodEntry, WorkoutEntry } from '../types';
import { DEFAULT_PROFILE } from '../store/defaults';

let n = 0;

export function workout(date: string, exerciseId: string, extra: Partial<WorkoutEntry> = {}): WorkoutEntry {
  return {
    id: `w${n++}`,
    date,
    exerciseId,
    sets: 3,
    reps: 10,
    weight: 0,
    durationMin: 0,
    distanceKm: 0,
    createdAt: 0,
    ...extra,
  };
}

export function food(date: string, extra: Partial<FoodEntry> = {}): FoodEntry {
  return {
    id: `f${n++}`,
    date,
    meal: 'lunch',
    name: 'Еда',
    grams: 100,
    calories: 500,
    protein: 30,
    fat: 10,
    carbs: 60,
    createdAt: 0,
    ...extra,
  };
}

export function state(extra: Partial<AppState> = {}): AppState {
  return {
    version: 1,
    profile: DEFAULT_PROFILE,
    workouts: [],
    foods: [],
    questClaims: [],
    activeProgramId: null,
    programProgress: {},
    ...extra,
  };
}
