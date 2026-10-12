import { getExercise } from '../data/exercises';
import type { FoodEntry, QuestClaim, WorkoutEntry } from '../types';
import type { Macros } from './nutrition';
import { sumFoods } from './nutrition';

export interface DayContext {
  workouts: WorkoutEntry[];
  foods: FoodEntry[];
  goals: Macros;
}

export interface QuestDef {
  id: string;
  title: string;
  xp: number;
  unit: string;
  target: (ctx: DayContext) => number;
  measure: (ctx: DayContext) => number;
}

const repsOf = (ctx: DayContext, ids: string[]) =>
  ctx.workouts.filter((w) => ids.includes(w.exerciseId)).reduce((sum, w) => sum + w.sets * w.reps, 0);

const nonCardio = (ctx: DayContext) => ctx.workouts.filter((w) => getExercise(w.exerciseId)?.kind !== 'cardio');

const BASE_QUEST: QuestDef = {
  id: 'workout',
  title: 'Выполни тренировку',
  xp: 50,
  unit: '',
  target: () => 1,
  measure: (ctx) => (ctx.workouts.length > 0 ? 1 : 0),
};

const TRAINING_QUESTS: QuestDef[] = [
  {
    id: 'sets12',
    title: 'Сделай 12 подходов',
    xp: 60,
    unit: 'подх.',
    target: () => 12,
    measure: (ctx) => nonCardio(ctx).reduce((sum, w) => sum + w.sets, 0),
  },
  {
    id: 'cardio20',
    title: 'Кардио 20 минут',
    xp: 50,
    unit: 'мин',
    target: () => 20,
    measure: (ctx) =>
      ctx.workouts.filter((w) => getExercise(w.exerciseId)?.kind === 'cardio').reduce((s, w) => s + w.durationMin, 0),
  },
  {
    id: 'pushups50',
    title: '50 отжиманий',
    xp: 50,
    unit: 'раз',
    target: () => 50,
    measure: (ctx) => repsOf(ctx, ['push_ups']),
  },
  {
    id: 'squats50',
    title: '50 приседаний',
    xp: 50,
    unit: 'раз',
    target: () => 50,
    measure: (ctx) => repsOf(ctx, ['bw_squat', 'squat']),
  },
  {
    id: 'muscles4',
    title: 'Нагрузи 4 группы мышц',
    xp: 50,
    unit: 'гр.',
    target: () => 4,
    measure: (ctx) => {
      const set = new Set<string>();
      ctx.workouts.forEach((w) => getExercise(w.exerciseId)?.primary.forEach((m) => set.add(m)));
      return set.size;
    },
  },
  {
    id: 'core3',
    title: '3 подхода на пресс',
    xp: 40,
    unit: 'подх.',
    target: () => 3,
    measure: (ctx) =>
      nonCardio(ctx)
        .filter((w) => getExercise(w.exerciseId)?.primary.some((m) => m === 'abs' || m === 'obliques'))
        .reduce((sum, w) => sum + w.sets, 0),
  },
];

const NUTRITION_QUESTS: QuestDef[] = [
  {
    id: 'meals3',
    title: 'Запиши 3 приёма пищи',
    xp: 40,
    unit: '',
    target: () => 3,
    measure: (ctx) => new Set(ctx.foods.map((f) => f.meal)).size,
  },
  {
    id: 'protein',
    title: 'Набери норму белка',
    xp: 50,
    unit: 'г',
    target: (ctx) => ctx.goals.protein,
    measure: (ctx) => Math.round(sumFoods(ctx.foods).protein),
  },
  {
    id: 'calories',
    title: 'Попади в норму калорий ±10%',
    xp: 60,
    unit: '',
    target: () => 1,
    measure: (ctx) => {
      const ratio = sumFoods(ctx.foods).calories / ctx.goals.calories;
      return ratio >= 0.9 && ratio <= 1.1 ? 1 : 0;
    },
  },
  {
    id: 'breakfast',
    title: 'Не пропусти завтрак',
    xp: 30,
    unit: '',
    target: () => 1,
    measure: (ctx) => (ctx.foods.some((f) => f.meal === 'breakfast') ? 1 : 0),
  },
];

function hash(s: string): number {
  let h = 5381;
  for (let i = 0; i < s.length; i++) h = ((h << 5) + h + s.charCodeAt(i)) >>> 0;
  return h;
}

/** Три задания на день: тренировка + случайное тренировочное + случайное по питанию. Одинаковы для одной даты. */
export function questsForDate(date: string): QuestDef[] {
  const h = hash(date);
  return [
    BASE_QUEST,
    TRAINING_QUESTS[h % TRAINING_QUESTS.length],
    NUTRITION_QUESTS[Math.floor(h / 7) % NUTRITION_QUESTS.length],
  ];
}

export interface QuestStatus {
  def: QuestDef;
  current: number;
  target: number;
  done: boolean;
  claimed: boolean;
}

export function questStatuses(date: string, ctx: DayContext, claims: QuestClaim[]): QuestStatus[] {
  return questsForDate(date).map((def) => {
    const target = Math.max(1, def.target(ctx));
    const current = def.measure(ctx);
    return {
      def,
      current,
      target,
      done: current >= target,
      claimed: claims.some((c) => c.date === date && c.questId === def.id),
    };
  });
}
