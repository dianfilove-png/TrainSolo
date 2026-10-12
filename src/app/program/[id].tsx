import { router, Stack, useLocalSearchParams } from 'expo-router';
import { ScrollView, StyleSheet, Text, View } from 'react-native';

import { useToast } from '@/components/Feedback';
import { LEVEL_COLORS, programMuscles } from '@/components/ProgramCard';
import { Badge, Button, Card, Empty } from '@/components/ui';
import { getExercise } from '@/data/exercises';
import { MUSCLES } from '@/data/muscles';
import { getProgram } from '@/data/programs';
import { formatAmount } from '@/logic/format';
import { useStore } from '@/store/AppStore';
import { colors } from '@/theme';

export default function ProgramScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { state, setActiveProgram, setProgramDay } = useStore();
  const toast = useToast();
  const program = getProgram(id);

  if (!program) {
    return (
      <View style={{ flex: 1, backgroundColor: colors.bg, padding: 24 }}>
        <Empty icon="alert-circle-outline" text="Программа не найдена" />
      </View>
    );
  }

  const active = state.activeProgramId === program.id;

  return (
    <ScrollView style={{ backgroundColor: colors.bg }} contentContainerStyle={styles.content}>
      <Stack.Screen options={{ title: program.name }} />
      <View style={{ gap: 10 }}>
        <Text style={styles.title}>
          {program.emoji} {program.name}
        </Text>
        <View style={styles.badges}>
          <Badge label={program.level} color={LEVEL_COLORS[program.level]} />
          <Badge label={program.daysPerWeek} color={colors.textDim} />
          <Badge label={`${program.days.length} ${program.days.length === 1 ? 'день' : 'дня'} в цикле`} color={colors.textDim} />
        </View>
        <Text style={styles.description}>{program.description}</Text>
        <Text style={styles.muscles}>
          Нагружает: {programMuscles(program).map((m) => MUSCLES[m].name.toLowerCase()).join(', ')}
        </Text>
      </View>

      {active ? (
        <Button
          title="Остановить программу"
          variant="danger"
          icon="stop-circle-outline"
          onPress={() => {
            setActiveProgram(null);
            toast('Программа остановлена');
          }}
        />
      ) : (
        <Button
          title="Начать программу"
          icon="play"
          onPress={() => {
            setActiveProgram(program.id);
            setProgramDay(program.id, 0);
            toast(`${program.emoji} Программа «${program.name}» активна`);
            router.navigate('/workout');
          }}
        />
      )}

      {program.days.map((day, i) => (
        <Card key={day.name}>
          <Text style={styles.dayTitle}>
            {i + 1}. {day.name}
          </Text>
          {day.exercises.map((p) => {
            const ex = getExercise(p.exerciseId);
            if (!ex) return null;
            return (
              <View key={p.exerciseId} style={styles.row}>
                <View style={{ flex: 1 }}>
                  <Text style={styles.exName}>{ex.name}</Text>
                  <Text style={styles.exMuscles}>{ex.primary.map((m) => MUSCLES[m].name).join(', ')}</Text>
                </View>
                <Text style={styles.amount}>{formatAmount(ex, p)}</Text>
              </View>
            );
          })}
        </Card>
      ))}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  content: { padding: 16, gap: 16, paddingBottom: 40, width: '100%', maxWidth: 640, alignSelf: 'center' },
  title: { color: colors.text, fontSize: 26, fontWeight: '800' },
  badges: { flexDirection: 'row', flexWrap: 'wrap', gap: 6 },
  description: { color: colors.textDim, fontSize: 15, lineHeight: 22 },
  muscles: { color: colors.textMuted, fontSize: 13, lineHeight: 18 },
  dayTitle: { color: colors.primary, fontSize: 16, fontWeight: '800' },
  row: { flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 4, borderTopWidth: 1, borderTopColor: colors.border, paddingTop: 10 },
  exName: { color: colors.text, fontSize: 15, fontWeight: '600' },
  exMuscles: { color: colors.textMuted, fontSize: 12 },
  amount: { color: colors.text, fontSize: 14, fontWeight: '700' },
});
