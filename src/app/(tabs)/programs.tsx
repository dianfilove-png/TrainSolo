import { router } from 'expo-router';

import { ProgramCard } from '@/components/ProgramCard';
import { Screen } from '@/components/ui';
import { PROGRAMS } from '@/data/programs';
import { useStore } from '@/store/AppStore';

export default function ProgramsScreen() {
  const { state } = useStore();
  return (
    <Screen title="Программы" subtitle="Выбери путь развития своего героя">
      {PROGRAMS.map((p) => (
        <ProgramCard key={p.id} program={p} active={state.activeProgramId === p.id} onPress={() => router.push(`/program/${p.id}`)} />
      ))}
    </Screen>
  );
}
