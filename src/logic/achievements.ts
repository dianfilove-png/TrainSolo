import { MUSCLE_IDS } from '../data/muscles';
import type { WorkoutEntry } from '../types';
import type { Totals } from './stats';

export interface Achievement {
  id: string;
  emoji: string;
  title: string;
  description: string;
  current: number;
  target: number;
  done: boolean;
}

/** Выполнено ли «Ежедневное задание Охотника» хотя бы в один день. */
export function hunterQuestDone(workouts: WorkoutEntry[]): boolean {
  const days = new Map<string, { push: number; sit: number; squat: number; km: number }>();
  for (const w of workouts) {
    const d = days.get(w.date) ?? { push: 0, sit: 0, squat: 0, km: 0 };
    const reps = w.sets * w.reps;
    if (w.exerciseId === 'push_ups') d.push += reps;
    if (w.exerciseId === 'sit_ups') d.sit += reps;
    if (w.exerciseId === 'bw_squat' || w.exerciseId === 'squat') d.squat += reps;
    if (w.exerciseId === 'running') d.km += w.distanceKm;
    days.set(w.date, d);
  }
  return [...days.values()].some((d) => d.push >= 100 && d.sit >= 100 && d.squat >= 100 && d.km >= 10);
}

export function getAchievements(t: Totals, level: number, workouts: WorkoutEntry[]): Achievement[] {
  const make = (id: string, emoji: string, title: string, description: string, current: number, target: number) => ({
    id,
    emoji,
    title,
    description,
    current: Math.min(current, target),
    target,
    done: current >= target,
  });

  return [
    make('first', '🩸', 'Первая кровь', 'Запиши первую тренировку', t.workoutDays, 1),
    make('days10', '📅', 'Десятка', '10 тренировочных дней', t.workoutDays, 10),
    make('sets100', '💯', 'Сотня подходов', 'Сделай 100 подходов', t.totalSets, 100),
    make('ton10', '🏗️', 'Десять тонн', 'Подними суммарно 10\u00A0000 кг', t.tonnage, 10000),
    make('streak7', '🔥', 'Неделя в строю', '7 активных дней подряд', t.bestStreak, 7),
    make('food7', '🥗', 'Дневник питания', 'Записывай еду 7 дней', t.foodDays, 7),
    make('anatomy', '🧠', 'Анатом', 'Нагрузи все группы мышц', t.musclesTrained.size, MUSCLE_IDS.length),
    make('marathon', '🏃', 'Марафонец', 'Пробеги суммарно 42,2 км', t.cardioKm, 42.2),
    make('rankC', '⭐', 'Ранг C', 'Достигни 10 уровня', level, 10),
    make('hunter', '🗡️', 'Задание Системы', '100 отжиманий, 100 подъёмов корпуса, 100 приседаний и 10 км за день', hunterQuestDone(workouts) ? 1 : 0, 1),
  ];
}
