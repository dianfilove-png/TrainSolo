import type { Exercise } from '../types';

export const EXERCISES: Exercise[] = [
  // Грудь
  { id: 'bench_press', name: 'Жим штанги лёжа', kind: 'strength', primary: ['chest'], secondary: ['triceps', 'shoulders'] },
  { id: 'incline_db_press', name: 'Жим гантелей на наклонной', kind: 'strength', primary: ['chest', 'shoulders'], secondary: ['triceps'] },
  { id: 'dumbbell_fly', name: 'Разводка гантелей', kind: 'strength', primary: ['chest'], secondary: ['shoulders'] },
  { id: 'push_ups', name: 'Отжимания', kind: 'bodyweight', primary: ['chest', 'triceps'], secondary: ['shoulders', 'abs'] },
  { id: 'dips', name: 'Отжимания на брусьях', kind: 'bodyweight', primary: ['triceps', 'chest'], secondary: ['shoulders'] },

  // Плечи
  { id: 'overhead_press', name: 'Жим штанги стоя', kind: 'strength', primary: ['shoulders'], secondary: ['triceps', 'traps'] },
  { id: 'db_shoulder_press', name: 'Жим гантелей сидя', kind: 'strength', primary: ['shoulders'], secondary: ['triceps'] },
  { id: 'lateral_raise', name: 'Махи гантелями в стороны', kind: 'strength', primary: ['shoulders'], secondary: ['traps'] },
  { id: 'face_pull', name: 'Тяга каната к лицу', kind: 'strength', primary: ['shoulders', 'traps'], secondary: [] },

  // Спина
  { id: 'deadlift', name: 'Становая тяга', kind: 'strength', primary: ['lowerBack', 'glutes', 'hamstrings'], secondary: ['traps', 'forearms', 'quads', 'lats'] },
  { id: 'barbell_row', name: 'Тяга штанги в наклоне', kind: 'strength', primary: ['lats', 'traps'], secondary: ['biceps', 'lowerBack', 'shoulders'] },
  { id: 'pull_ups', name: 'Подтягивания', kind: 'bodyweight', primary: ['lats', 'biceps'], secondary: ['forearms', 'traps'] },
  { id: 'lat_pulldown', name: 'Тяга верхнего блока', kind: 'strength', primary: ['lats'], secondary: ['biceps'] },
  { id: 'seated_row', name: 'Тяга нижнего блока', kind: 'strength', primary: ['lats', 'traps'], secondary: ['biceps'] },
  { id: 'shrugs', name: 'Шраги', kind: 'strength', primary: ['traps'], secondary: ['forearms'] },
  { id: 'hyperextension', name: 'Гиперэкстензия', kind: 'bodyweight', primary: ['lowerBack'], secondary: ['glutes', 'hamstrings'] },

  // Руки
  { id: 'barbell_curl', name: 'Подъём штанги на бицепс', kind: 'strength', primary: ['biceps'], secondary: ['forearms'] },
  { id: 'hammer_curl', name: 'Молотки с гантелями', kind: 'strength', primary: ['biceps', 'forearms'], secondary: [] },
  { id: 'triceps_pushdown', name: 'Разгибания на блоке', kind: 'strength', primary: ['triceps'], secondary: [] },
  { id: 'french_press', name: 'Французский жим', kind: 'strength', primary: ['triceps'], secondary: [] },
  { id: 'wrist_curl', name: 'Сгибания запястий', kind: 'strength', primary: ['forearms'], secondary: [] },

  // Ноги
  { id: 'squat', name: 'Приседания со штангой', kind: 'strength', primary: ['quads', 'glutes'], secondary: ['hamstrings', 'lowerBack', 'abs'] },
  { id: 'bw_squat', name: 'Приседания', kind: 'bodyweight', primary: ['quads', 'glutes'], secondary: ['hamstrings'] },
  { id: 'leg_press', name: 'Жим ногами', kind: 'strength', primary: ['quads', 'glutes'], secondary: ['hamstrings'] },
  { id: 'lunges', name: 'Выпады', kind: 'bodyweight', primary: ['quads', 'glutes'], secondary: ['hamstrings', 'calves'] },
  { id: 'romanian_deadlift', name: 'Румынская тяга', kind: 'strength', primary: ['hamstrings', 'glutes'], secondary: ['lowerBack'] },
  { id: 'leg_curl', name: 'Сгибание ног в тренажёре', kind: 'strength', primary: ['hamstrings'], secondary: [] },
  { id: 'leg_extension', name: 'Разгибание ног в тренажёре', kind: 'strength', primary: ['quads'], secondary: [] },
  { id: 'hip_thrust', name: 'Ягодичный мост со штангой', kind: 'strength', primary: ['glutes'], secondary: ['hamstrings'] },
  { id: 'glute_bridge', name: 'Ягодичный мостик', kind: 'bodyweight', primary: ['glutes'], secondary: ['hamstrings'] },
  { id: 'calf_raise', name: 'Подъём на носки', kind: 'strength', primary: ['calves'], secondary: [] },

  // Кор
  { id: 'sit_ups', name: 'Подъёмы корпуса', kind: 'bodyweight', primary: ['abs'], secondary: ['obliques'] },
  { id: 'plank', name: 'Планка', kind: 'bodyweight', primary: ['abs'], secondary: ['obliques', 'shoulders', 'lowerBack'], unit: 'sec' },
  { id: 'leg_raises', name: 'Подъём ног в висе', kind: 'bodyweight', primary: ['abs'], secondary: ['obliques', 'forearms'] },
  { id: 'russian_twist', name: 'Русские скручивания', kind: 'bodyweight', primary: ['obliques'], secondary: ['abs'] },
  { id: 'burpees', name: 'Бёрпи', kind: 'bodyweight', primary: ['quads', 'chest'], secondary: ['shoulders', 'abs', 'triceps'] },

  // Кардио
  { id: 'running', name: 'Бег', kind: 'cardio', primary: ['quads', 'calves'], secondary: ['hamstrings', 'glutes'] },
  { id: 'cycling', name: 'Велосипед', kind: 'cardio', primary: ['quads'], secondary: ['calves', 'hamstrings', 'glutes'] },
  { id: 'jump_rope', name: 'Скакалка', kind: 'cardio', primary: ['calves'], secondary: ['shoulders', 'forearms'] },
  { id: 'rowing', name: 'Гребной тренажёр', kind: 'cardio', primary: ['lats', 'quads'], secondary: ['biceps', 'hamstrings', 'lowerBack'] },
  { id: 'swimming', name: 'Плавание', kind: 'cardio', primary: ['lats', 'shoulders'], secondary: ['triceps', 'chest', 'abs'] },
  { id: 'walking', name: 'Ходьба', kind: 'cardio', primary: ['calves'], secondary: ['quads', 'glutes'] },
];

const BY_ID = new Map(EXERCISES.map((e) => [e.id, e]));

export function getExercise(id: string): Exercise | undefined {
  return BY_ID.get(id);
}
