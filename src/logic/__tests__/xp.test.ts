import { levelInfo, rankForLevel, totalXp, workoutXp, xpForLevel } from '../xp';
import { food, state, workout } from '../../test-utils/fixtures';

describe('опыт', () => {
  it('силовые дают 10 XP за подход, кардио — 2 XP за минуту', () => {
    expect(workoutXp(workout('2026-10-12', 'bench_press', { sets: 4 }))).toBe(40);
    expect(workoutXp(workout('2026-10-12', 'running', { durationMin: 30 }))).toBe(60);
  });

  it('за еду начисляется не больше 6 записей в день', () => {
    const foods = Array.from({ length: 10 }, () => food('2026-10-12'));
    expect(totalXp(state({ foods }))).toBe(30);
  });

  it('суммирует тренировки, еду и награды за задания', () => {
    const s = state({
      workouts: [workout('2026-10-12', 'squat', { sets: 5 })],
      foods: [food('2026-10-12'), food('2026-10-11')],
      questClaims: [{ date: '2026-10-12', questId: 'workout', xp: 50 }],
    });
    expect(totalXp(s)).toBe(50 + 10 + 50);
  });
});

describe('уровни', () => {
  it('пороги уровней растут', () => {
    expect(xpForLevel(1)).toBe(0);
    expect(xpForLevel(2)).toBe(100);
    expect(xpForLevel(3)).toBe(250);
    expect(xpForLevel(4)).toBe(450);
  });

  it('levelInfo считает прогресс внутри уровня', () => {
    expect(levelInfo(0)).toMatchObject({ level: 1, current: 0, needed: 100 });
    expect(levelInfo(99).level).toBe(1);
    expect(levelInfo(100)).toMatchObject({ level: 2, current: 0, needed: 150 });
    expect(levelInfo(300)).toMatchObject({ level: 3, current: 50, needed: 200 });
  });

  it('ранги от E до S', () => {
    expect(rankForLevel(1).letter).toBe('E');
    expect(rankForLevel(5).letter).toBe('D');
    expect(rankForLevel(19).letter).toBe('C');
    expect(rankForLevel(60).letter).toBe('S');
  });
});
