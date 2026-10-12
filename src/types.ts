export type MuscleId =
  | 'chest'
  | 'shoulders'
  | 'biceps'
  | 'triceps'
  | 'forearms'
  | 'abs'
  | 'obliques'
  | 'traps'
  | 'lats'
  | 'lowerBack'
  | 'glutes'
  | 'quads'
  | 'hamstrings'
  | 'calves';

/** strength — подходы × повторы × вес; bodyweight — подходы × повторы; cardio — минуты (+ км). */
export type ExerciseKind = 'strength' | 'bodyweight' | 'cardio';

export interface Exercise {
  id: string;
  name: string;
  kind: ExerciseKind;
  primary: MuscleId[];
  secondary: MuscleId[];
  /** Для статических упражнений (планка) повторы — это секунды. */
  unit?: 'reps' | 'sec';
}

/** Одна запись в журнале тренировок. */
export interface WorkoutEntry {
  id: string;
  /** Локальная дата в формате YYYY-MM-DD. */
  date: string;
  exerciseId: string;
  sets: number;
  reps: number;
  weight: number;
  durationMin: number;
  distanceKm: number;
  createdAt: number;
}

export type MealType = 'breakfast' | 'lunch' | 'dinner' | 'snack';

export interface FoodEntry {
  id: string;
  date: string;
  meal: MealType;
  name: string;
  grams: number;
  calories: number;
  protein: number;
  fat: number;
  carbs: number;
  createdAt: number;
}

export type Sex = 'male' | 'female';
export type Activity = 'low' | 'light' | 'moderate' | 'high' | 'extreme';
export type Goal = 'lose' | 'maintain' | 'gain';

export interface Profile {
  name: string;
  sex: Sex;
  age: number;
  heightCm: number;
  weightKg: number;
  activity: Activity;
  goal: Goal;
  /** Ручная норма калорий; null — считать автоматически. */
  customCalories: number | null;
}

export interface QuestClaim {
  date: string;
  questId: string;
  xp: number;
}

export interface AppState {
  version: 1;
  profile: Profile;
  workouts: WorkoutEntry[];
  foods: FoodEntry[];
  questClaims: QuestClaim[];
  activeProgramId: string | null;
  /** Индекс следующего дня для каждой программы. */
  programProgress: Record<string, number>;
}

export interface PlannedExercise {
  exerciseId: string;
  sets: number;
  reps?: number;
  weight?: number;
  durationMin?: number;
  distanceKm?: number;
}

export interface ProgramDay {
  name: string;
  exercises: PlannedExercise[];
}

export interface Program {
  id: string;
  name: string;
  emoji: string;
  level: 'Новичок' | 'Средний' | 'Продвинутый' | 'Хардкор';
  daysPerWeek: string;
  description: string;
  days: ProgramDay[];
}
