import type { Program } from '../types';

export const PROGRAMS: Program[] = [
  {
    id: 'fullbody_beginner',
    name: 'Full Body для новичка',
    emoji: '🌱',
    level: 'Новичок',
    daysPerWeek: '3 дня в неделю',
    description:
      'Всё тело за одну тренировку. Базовые движения, умеренный объём — идеально, чтобы втянуться и выучить технику.',
    days: [
      {
        name: 'День A',
        exercises: [
          { exerciseId: 'squat', sets: 3, reps: 10, weight: 40 },
          { exerciseId: 'bench_press', sets: 3, reps: 10, weight: 40 },
          { exerciseId: 'barbell_row', sets: 3, reps: 10, weight: 30 },
          { exerciseId: 'plank', sets: 3, reps: 30 },
        ],
      },
      {
        name: 'День B',
        exercises: [
          { exerciseId: 'romanian_deadlift', sets: 3, reps: 10, weight: 40 },
          { exerciseId: 'overhead_press', sets: 3, reps: 10, weight: 25 },
          { exerciseId: 'lat_pulldown', sets: 3, reps: 10, weight: 35 },
          { exerciseId: 'lunges', sets: 3, reps: 10 },
          { exerciseId: 'sit_ups', sets: 3, reps: 15 },
        ],
      },
      {
        name: 'День C',
        exercises: [
          { exerciseId: 'leg_press', sets: 3, reps: 12, weight: 80 },
          { exerciseId: 'incline_db_press', sets: 3, reps: 10, weight: 14 },
          { exerciseId: 'seated_row', sets: 3, reps: 12, weight: 35 },
          { exerciseId: 'barbell_curl', sets: 2, reps: 12, weight: 20 },
          { exerciseId: 'triceps_pushdown', sets: 2, reps: 12, weight: 20 },
        ],
      },
    ],
  },
  {
    id: 'ppl',
    name: 'Push / Pull / Legs',
    emoji: '⚔️',
    level: 'Средний',
    daysPerWeek: '3–6 дней в неделю',
    description:
      'Классический сплит: жимы, тяги и ноги в разные дни. Каждая группа успевает восстановиться — следи, чтобы на аватаре она снова стала зелёной.',
    days: [
      {
        name: 'Push — жимы',
        exercises: [
          { exerciseId: 'bench_press', sets: 4, reps: 8, weight: 60 },
          { exerciseId: 'overhead_press', sets: 3, reps: 10, weight: 35 },
          { exerciseId: 'incline_db_press', sets: 3, reps: 10, weight: 20 },
          { exerciseId: 'lateral_raise', sets: 3, reps: 15, weight: 8 },
          { exerciseId: 'triceps_pushdown', sets: 3, reps: 12, weight: 25 },
        ],
      },
      {
        name: 'Pull — тяги',
        exercises: [
          { exerciseId: 'deadlift', sets: 3, reps: 5, weight: 80 },
          { exerciseId: 'pull_ups', sets: 4, reps: 8 },
          { exerciseId: 'barbell_row', sets: 3, reps: 10, weight: 50 },
          { exerciseId: 'face_pull', sets: 3, reps: 15, weight: 15 },
          { exerciseId: 'barbell_curl', sets: 3, reps: 12, weight: 25 },
          { exerciseId: 'hammer_curl', sets: 3, reps: 12, weight: 12 },
        ],
      },
      {
        name: 'Legs — ноги',
        exercises: [
          { exerciseId: 'squat', sets: 4, reps: 8, weight: 70 },
          { exerciseId: 'romanian_deadlift', sets: 3, reps: 10, weight: 60 },
          { exerciseId: 'leg_press', sets: 3, reps: 12, weight: 120 },
          { exerciseId: 'leg_curl', sets: 3, reps: 12, weight: 30 },
          { exerciseId: 'calf_raise', sets: 4, reps: 15, weight: 40 },
        ],
      },
    ],
  },
  {
    id: 'upper_lower',
    name: 'Верх / Низ',
    emoji: '🏋️',
    level: 'Средний',
    daysPerWeek: '4 дня в неделю',
    description: 'Два дня на верх тела и два на низ. Хороший баланс силы и объёма.',
    days: [
      {
        name: 'Верх A',
        exercises: [
          { exerciseId: 'bench_press', sets: 4, reps: 6, weight: 65 },
          { exerciseId: 'barbell_row', sets: 4, reps: 8, weight: 55 },
          { exerciseId: 'db_shoulder_press', sets: 3, reps: 10, weight: 18 },
          { exerciseId: 'lat_pulldown', sets: 3, reps: 10, weight: 50 },
          { exerciseId: 'barbell_curl', sets: 2, reps: 12, weight: 25 },
          { exerciseId: 'french_press', sets: 2, reps: 12, weight: 20 },
        ],
      },
      {
        name: 'Низ A',
        exercises: [
          { exerciseId: 'squat', sets: 4, reps: 6, weight: 80 },
          { exerciseId: 'romanian_deadlift', sets: 3, reps: 8, weight: 70 },
          { exerciseId: 'leg_extension', sets: 3, reps: 12, weight: 40 },
          { exerciseId: 'calf_raise', sets: 4, reps: 12, weight: 50 },
          { exerciseId: 'leg_raises', sets: 3, reps: 12 },
        ],
      },
      {
        name: 'Верх B',
        exercises: [
          { exerciseId: 'overhead_press', sets: 4, reps: 6, weight: 40 },
          { exerciseId: 'pull_ups', sets: 4, reps: 8 },
          { exerciseId: 'incline_db_press', sets: 3, reps: 10, weight: 22 },
          { exerciseId: 'seated_row', sets: 3, reps: 10, weight: 55 },
          { exerciseId: 'lateral_raise', sets: 3, reps: 15, weight: 8 },
          { exerciseId: 'shrugs', sets: 3, reps: 12, weight: 60 },
        ],
      },
      {
        name: 'Низ B',
        exercises: [
          { exerciseId: 'deadlift', sets: 3, reps: 5, weight: 100 },
          { exerciseId: 'leg_press', sets: 3, reps: 10, weight: 140 },
          { exerciseId: 'leg_curl', sets: 3, reps: 12, weight: 35 },
          { exerciseId: 'hip_thrust', sets: 3, reps: 10, weight: 80 },
          { exerciseId: 'russian_twist', sets: 3, reps: 20 },
        ],
      },
    ],
  },
  {
    id: 'home_bodyweight',
    name: 'Дома без железа',
    emoji: '🏠',
    level: 'Новичок',
    daysPerWeek: '3 дня в неделю',
    description: 'Только вес собственного тела. Нужен коврик и немного места — никаких оправданий.',
    days: [
      {
        name: 'Круг 1',
        exercises: [
          { exerciseId: 'push_ups', sets: 4, reps: 12 },
          { exerciseId: 'bw_squat', sets: 4, reps: 20 },
          { exerciseId: 'glute_bridge', sets: 3, reps: 15 },
          { exerciseId: 'plank', sets: 3, reps: 45 },
        ],
      },
      {
        name: 'Круг 2',
        exercises: [
          { exerciseId: 'lunges', sets: 3, reps: 12 },
          { exerciseId: 'dips', sets: 3, reps: 10 },
          { exerciseId: 'hyperextension', sets: 3, reps: 15 },
          { exerciseId: 'russian_twist', sets: 3, reps: 20 },
        ],
      },
      {
        name: 'Круг 3',
        exercises: [
          { exerciseId: 'burpees', sets: 4, reps: 10 },
          { exerciseId: 'push_ups', sets: 3, reps: 15 },
          { exerciseId: 'sit_ups', sets: 3, reps: 20 },
          { exerciseId: 'jump_rope', sets: 1, durationMin: 10 },
        ],
      },
    ],
  },
  {
    id: 'hunter',
    name: 'Путь Охотника',
    emoji: '🗡️',
    level: 'Хардкор',
    daysPerWeek: 'Каждый день',
    description:
      'Ежедневное задание Системы: 100 отжиманий, 100 подъёмов корпуса, 100 приседаний и 10 км бега. Выполнишь — получишь особое достижение.',
    days: [
      {
        name: 'Ежедневное задание',
        exercises: [
          { exerciseId: 'push_ups', sets: 10, reps: 10 },
          { exerciseId: 'sit_ups', sets: 10, reps: 10 },
          { exerciseId: 'bw_squat', sets: 10, reps: 10 },
          { exerciseId: 'running', sets: 1, durationMin: 60, distanceKm: 10 },
        ],
      },
    ],
  },
  {
    id: 'cardio_burn',
    name: 'Кардио и жиросжигание',
    emoji: '🔥',
    level: 'Новичок',
    daysPerWeek: '3 дня в неделю',
    description: 'Выносливость, сердце и расход калорий. Отлично сочетается с дефицитом в разделе «Питание».',
    days: [
      {
        name: 'Бег + кор',
        exercises: [
          { exerciseId: 'running', sets: 1, durationMin: 30, distanceKm: 5 },
          { exerciseId: 'plank', sets: 3, reps: 40 },
          { exerciseId: 'russian_twist', sets: 3, reps: 20 },
        ],
      },
      {
        name: 'Интервалы',
        exercises: [
          { exerciseId: 'jump_rope', sets: 1, durationMin: 10 },
          { exerciseId: 'burpees', sets: 5, reps: 10 },
          { exerciseId: 'bw_squat', sets: 4, reps: 20 },
          { exerciseId: 'sit_ups', sets: 3, reps: 20 },
        ],
      },
      {
        name: 'Длинное кардио',
        exercises: [
          { exerciseId: 'cycling', sets: 1, durationMin: 45 },
          { exerciseId: 'walking', sets: 1, durationMin: 30 },
        ],
      },
    ],
  },
];

export function getProgram(id: string | null | undefined): Program | undefined {
  return PROGRAMS.find((p) => p.id === id);
}
