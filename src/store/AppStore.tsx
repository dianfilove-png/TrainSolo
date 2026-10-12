import AsyncStorage from '@react-native-async-storage/async-storage';
import { createContext, useCallback, useContext, useEffect, useMemo, useReducer, useState, type ReactNode } from 'react';
import { AppState as RNAppState } from 'react-native';

import { toDateKey } from '../logic/date';
import { makeId } from '../logic/id';
import { nutritionGoals, type Macros } from '../logic/nutrition';
import { getMuscleStates, type MuscleState } from '../logic/recovery';
import { computeTotals, type Totals } from '../logic/stats';
import { levelInfo, totalXp, type LevelInfo } from '../logic/xp';
import type { AppState, FoodEntry, MuscleId, Profile, QuestClaim, WorkoutEntry } from '../types';
import { INITIAL_STATE } from './defaults';

const STORAGE_KEY = 'trainsolo/state/v1';

type Action =
  | { type: 'hydrate'; state: AppState }
  | { type: 'addWorkout'; entry: WorkoutEntry }
  | { type: 'removeWorkout'; id: string }
  | { type: 'addFood'; entry: FoodEntry }
  | { type: 'removeFood'; id: string }
  | { type: 'claimQuest'; claim: QuestClaim }
  | { type: 'updateProfile'; patch: Partial<Profile> }
  | { type: 'setActiveProgram'; id: string | null }
  | { type: 'setProgramDay'; programId: string; dayIndex: number }
  | { type: 'reset' };

function reducer(state: AppState, action: Action): AppState {
  switch (action.type) {
    case 'hydrate':
      return action.state;
    case 'addWorkout':
      return { ...state, workouts: [...state.workouts, action.entry] };
    case 'removeWorkout':
      return { ...state, workouts: state.workouts.filter((w) => w.id !== action.id) };
    case 'addFood':
      return { ...state, foods: [...state.foods, action.entry] };
    case 'removeFood':
      return { ...state, foods: state.foods.filter((f) => f.id !== action.id) };
    case 'claimQuest': {
      const { date, questId } = action.claim;
      if (state.questClaims.some((c) => c.date === date && c.questId === questId)) return state;
      return { ...state, questClaims: [...state.questClaims, action.claim] };
    }
    case 'updateProfile':
      return { ...state, profile: { ...state.profile, ...action.patch } };
    case 'setActiveProgram':
      return { ...state, activeProgramId: action.id };
    case 'setProgramDay':
      return { ...state, programProgress: { ...state.programProgress, [action.programId]: action.dayIndex } };
    case 'reset':
      return INITIAL_STATE;
  }
}

/** Подставляет значения по умолчанию для полей, которых нет в сохранённых данных. */
function migrate(raw: unknown): AppState {
  if (!raw || typeof raw !== 'object') return INITIAL_STATE;
  const saved = raw as Partial<AppState>;
  return {
    ...INITIAL_STATE,
    ...saved,
    version: 1,
    profile: { ...INITIAL_STATE.profile, ...saved.profile },
  };
}

/** Текущая дата, которая обновляется после полуночи и при возвращении в приложение. */
function useToday(): string {
  const [today, setToday] = useState(toDateKey);
  useEffect(() => {
    const refresh = () => setToday(toDateKey());
    const timer = setInterval(refresh, 60_000);
    const sub = RNAppState.addEventListener('change', (s) => s === 'active' && refresh());
    return () => {
      clearInterval(timer);
      sub.remove();
    };
  }, []);
  return today;
}

export type NewWorkout = Omit<WorkoutEntry, 'id' | 'createdAt'>;
export type NewFood = Omit<FoodEntry, 'id' | 'createdAt'>;

interface Store {
  state: AppState;
  hydrated: boolean;
  today: string;
  level: LevelInfo;
  goals: Macros;
  muscles: Record<MuscleId, MuscleState>;
  totals: Totals;
  addWorkout: (entry: NewWorkout) => void;
  removeWorkout: (id: string) => void;
  addFood: (entry: NewFood) => void;
  removeFood: (id: string) => void;
  claimQuest: (claim: QuestClaim) => void;
  updateProfile: (patch: Partial<Profile>) => void;
  setActiveProgram: (id: string | null) => void;
  setProgramDay: (programId: string, dayIndex: number) => void;
  reset: () => void;
}

const StoreContext = createContext<Store | null>(null);

export function AppStoreProvider({ children }: { children: ReactNode }) {
  const [state, dispatch] = useReducer(reducer, INITIAL_STATE);
  const [hydrated, setHydrated] = useState(false);
  const today = useToday();

  useEffect(() => {
    AsyncStorage.getItem(STORAGE_KEY)
      .then((json) => {
        if (json) dispatch({ type: 'hydrate', state: migrate(JSON.parse(json)) });
      })
      .catch((e) => console.warn('Не удалось загрузить данные', e))
      .finally(() => setHydrated(true));
  }, []);

  useEffect(() => {
    if (!hydrated) return;
    const timer = setTimeout(() => {
      AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(state)).catch((e) => console.warn('Не удалось сохранить данные', e));
    }, 250);
    return () => clearTimeout(timer);
  }, [state, hydrated]);

  const addWorkout = useCallback(
    (entry: NewWorkout) => dispatch({ type: 'addWorkout', entry: { ...entry, id: makeId(), createdAt: Date.now() } }),
    [],
  );
  const addFood = useCallback(
    (entry: NewFood) => dispatch({ type: 'addFood', entry: { ...entry, id: makeId(), createdAt: Date.now() } }),
    [],
  );
  const removeWorkout = useCallback((id: string) => dispatch({ type: 'removeWorkout', id }), []);
  const removeFood = useCallback((id: string) => dispatch({ type: 'removeFood', id }), []);
  const claimQuest = useCallback((claim: QuestClaim) => dispatch({ type: 'claimQuest', claim }), []);
  const updateProfile = useCallback((patch: Partial<Profile>) => dispatch({ type: 'updateProfile', patch }), []);
  const setActiveProgram = useCallback((id: string | null) => dispatch({ type: 'setActiveProgram', id }), []);
  const setProgramDay = useCallback(
    (programId: string, dayIndex: number) => dispatch({ type: 'setProgramDay', programId, dayIndex }),
    [],
  );
  const reset = useCallback(() => dispatch({ type: 'reset' }), []);

  const level = useMemo(() => levelInfo(totalXp(state)), [state]);
  const goals = useMemo(() => nutritionGoals(state.profile), [state.profile]);
  const muscles = useMemo(() => getMuscleStates(state.workouts, today), [state.workouts, today]);
  const totals = useMemo(() => computeTotals(state, today), [state, today]);

  const value = useMemo<Store>(
    () => ({
      state,
      hydrated,
      today,
      level,
      goals,
      muscles,
      totals,
      addWorkout,
      removeWorkout,
      addFood,
      removeFood,
      claimQuest,
      updateProfile,
      setActiveProgram,
      setProgramDay,
      reset,
    }),
    [state, hydrated, today, level, goals, muscles, totals, addWorkout, removeWorkout, addFood, removeFood, claimQuest, updateProfile, setActiveProgram, setProgramDay, reset],
  );

  return <StoreContext.Provider value={value}>{children}</StoreContext.Provider>;
}

export function useStore(): Store {
  const store = useContext(StoreContext);
  if (!store) throw new Error('useStore должен вызываться внутри AppStoreProvider');
  return store;
}
