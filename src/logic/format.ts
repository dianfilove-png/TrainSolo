import type { Exercise } from '../types';

type Amount = { sets: number; reps?: number; weight?: number; durationMin?: number; distanceKm?: number };

const num = (n: number) => String(Math.round(n * 10) / 10).replace('.', ',');

/** «4×8 · 60 кг», «3×30 сек», «30 мин · 5 км». */
export function formatAmount(exercise: Exercise, a: Amount): string {
  if (exercise.kind === 'cardio') {
    const parts = [`${a.durationMin ?? 0} мин`];
    if (a.distanceKm) parts.push(`${num(a.distanceKm)} км`);
    return parts.join(' · ');
  }
  const base = `${a.sets}×${a.reps ?? 0}${exercise.unit === 'sec' ? ' сек' : ''}`;
  return exercise.kind === 'strength' && a.weight ? `${base} · ${num(a.weight)} кг` : base;
}

export function formatNumber(n: number): string {
  return Math.round(n).toLocaleString('ru-RU');
}
