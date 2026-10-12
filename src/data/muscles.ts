import type { MuscleId } from '../types';

export const MUSCLES: Record<MuscleId, { name: string }> = {
  chest: { name: 'Грудь' },
  shoulders: { name: 'Плечи' },
  biceps: { name: 'Бицепс' },
  triceps: { name: 'Трицепс' },
  forearms: { name: 'Предплечья' },
  abs: { name: 'Пресс' },
  obliques: { name: 'Косые мышцы' },
  traps: { name: 'Трапеции' },
  lats: { name: 'Широчайшие' },
  lowerBack: { name: 'Поясница' },
  glutes: { name: 'Ягодицы' },
  quads: { name: 'Квадрицепсы' },
  hamstrings: { name: 'Бицепс бедра' },
  calves: { name: 'Икры' },
};

export const MUSCLE_IDS = Object.keys(MUSCLES) as MuscleId[];
