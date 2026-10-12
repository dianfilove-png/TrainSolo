import type { AppState, Profile } from '../types';

export const DEFAULT_PROFILE: Profile = {
  name: 'Охотник',
  sex: 'male',
  age: 25,
  heightCm: 178,
  weightKg: 75,
  activity: 'moderate',
  goal: 'maintain',
  customCalories: null,
};

export const INITIAL_STATE: AppState = {
  version: 1,
  profile: DEFAULT_PROFILE,
  workouts: [],
  foods: [],
  questClaims: [],
  activeProgramId: null,
  programProgress: {},
};
