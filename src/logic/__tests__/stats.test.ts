import { getAchievements, hunterQuestDone } from '../achievements';
import { addDays, daysBetween, formatDay, plural } from '../date';
import { autoCalories, nutritionGoals, scalePreset, sumFoods } from '../nutrition';
import { questStatuses, questsForDate } from '../quests';
import { bestStreak, computeTotals, currentStreak } from '../stats';
import { DEFAULT_PROFILE } from '../../store/defaults';
import { food, state, workout } from '../../test-utils/fixtures';

const TODAY = '2026-10-12';

describe('даты', () => {
  it('daysBetween и addDays', () => {
    expect(daysBetween('2026-10-10', TODAY)).toBe(2);
    expect(daysBetween('2026-03-28', '2026-03-30')).toBe(2);
    expect(addDays('2026-12-31', 1)).toBe('2027-01-01');
    expect(addDays('2026-03-01', -1)).toBe('2026-02-28');
  });

  it('formatDay и plural', () => {
    expect(formatDay(TODAY, TODAY)).toBe('Сегодня');
    expect(formatDay('2026-10-11', TODAY)).toBe('Вчера');
    expect(formatDay('2026-10-08', TODAY)).toBe('чт, 8 окт');
    expect(plural(1, ['день', 'дня', 'дней'])).toBe('день');
    expect(plural(3, ['день', 'дня', 'дней'])).toBe('дня');
    expect(plural(11, ['день', 'дня', 'дней'])).toBe('дней');
    expect(plural(22, ['день', 'дня', 'дней'])).toBe('дня');
  });
});

describe('серии', () => {
  it('текущая серия не сгорает, пока сегодня ещё ничего не записано', () => {
    const active = new Set(['2026-10-09', '2026-10-10', '2026-10-11']);
    expect(currentStreak(active, TODAY)).toBe(3);
    expect(currentStreak(new Set(['2026-10-10']), TODAY)).toBe(0);
  });

  it('лучшая серия', () => {
    expect(bestStreak(new Set(['2026-10-01', '2026-10-02', '2026-10-05', '2026-10-06', '2026-10-07']))).toBe(3);
    expect(bestStreak(new Set())).toBe(0);
  });
});

describe('питание', () => {
  it('норма калорий по Миффлину — Сан Жеору', () => {
    // 10*75 + 6.25*178 - 5*25 + 5 = 1742.5; * 1.55 = 2700.9
    expect(autoCalories(DEFAULT_PROFILE)).toBe(2700);
    expect(autoCalories({ ...DEFAULT_PROFILE, goal: 'lose' })).toBe(2300);
  });

  it('ручная норма перекрывает автоматическую', () => {
    const goals = nutritionGoals({ ...DEFAULT_PROFILE, customCalories: 2000 });
    expect(goals.calories).toBe(2000);
    expect(goals.protein * 4 + goals.fat * 9 + goals.carbs * 4).toBeCloseTo(2000, -1);
  });

  it('пересчёт порции и суммирование', () => {
    const m = scalePreset({ name: 'x', portion: 100, calories: 165, protein: 31, fat: 3.6, carbs: 0 }, 150);
    expect(m).toEqual({ calories: 248, protein: 46.5, fat: 5.4, carbs: 0 });
    expect(sumFoods([food(TODAY), food(TODAY)]).calories).toBe(1000);
  });
});

describe('ежедневные задания', () => {
  it('для одной даты задания одинаковые, первое — тренировка', () => {
    expect(questsForDate(TODAY).map((q) => q.id)).toEqual(questsForDate(TODAY).map((q) => q.id));
    expect(questsForDate(TODAY)[0].id).toBe('workout');
    expect(questsForDate(TODAY)).toHaveLength(3);
  });

  it('статусы выполнения и получения награды', () => {
    const ctx = { workouts: [workout(TODAY, 'squat')], foods: [], goals: nutritionGoals(DEFAULT_PROFILE) };
    const claims = [{ date: TODAY, questId: 'workout', xp: 50 }];
    const [base] = questStatuses(TODAY, ctx, claims);
    expect(base).toMatchObject({ done: true, claimed: true, current: 1, target: 1 });
  });
});

describe('итоги и достижения', () => {
  const s = state({
    workouts: [
      workout('2026-10-11', 'bench_press', { sets: 4, reps: 8, weight: 60 }),
      workout(TODAY, 'running', { durationMin: 30, distanceKm: 5 }),
    ],
    foods: [food('2026-10-10', { calories: 2700 })],
  });

  it('computeTotals', () => {
    const t = computeTotals(s, TODAY);
    expect(t).toMatchObject({
      workoutDays: 2,
      totalSets: 4,
      tonnage: 1920,
      cardioMinutes: 30,
      cardioKm: 5,
      foodDays: 1,
      onTargetDays: 1,
      currentStreak: 3,
      bestStreak: 3,
    });
  });

  it('достижения', () => {
    const a = getAchievements(computeTotals(s, TODAY), 1, s.workouts);
    expect(a.find((x) => x.id === 'first')?.done).toBe(true);
    expect(a.find((x) => x.id === 'sets100')).toMatchObject({ done: false, current: 4 });
  });

  it('задание Охотника', () => {
    const day = [
      workout(TODAY, 'push_ups', { sets: 10, reps: 10 }),
      workout(TODAY, 'sit_ups', { sets: 10, reps: 10 }),
      workout(TODAY, 'bw_squat', { sets: 10, reps: 10 }),
    ];
    expect(hunterQuestDone(day)).toBe(false);
    expect(hunterQuestDone([...day, workout(TODAY, 'running', { distanceKm: 10, durationMin: 60 })])).toBe(true);
  });
});
