import Ionicons from '@expo/vector-icons/Ionicons';
import { router } from 'expo-router';
import { useMemo, useState } from 'react';
import { Pressable, StyleSheet, Text, useWindowDimensions, View } from 'react-native';

import { BodyMap, StatusLegend } from '@/components/BodyMap';
import { useToast } from '@/components/Feedback';
import { Button, Card, ProgressBar, Screen, SectionTitle } from '@/components/ui';
import { getProgram } from '@/data/programs';
import { MUSCLES, MUSCLE_IDS } from '@/data/muscles';
import { plural } from '@/logic/date';
import { sumFoods } from '@/logic/nutrition';
import { questStatuses } from '@/logic/quests';
import { STATUS_INFO, type MuscleState } from '@/logic/recovery';
import { characterStats } from '@/logic/stats';
import { useStore } from '@/store/AppStore';
import { colors, radius } from '@/theme';
import type { MuscleId } from '@/types';

function describeMuscle(s: MuscleState): string {
  if (s.daysSince === null) return 'Ещё не тренировалась. Самое время начать!';
  if (s.daysSince === 0) return `Нагружена сегодня (${Math.max(1, Math.round(s.sets))} подх.). Дай ей отдохнуть — восстановится послезавтра.`;
  if (s.daysSince === 1) return 'Тренировалась вчера и сейчас восстанавливается. Будет готова завтра.';
  return `Последняя нагрузка ${s.daysSince} ${plural(s.daysSince, ['день', 'дня', 'дней'])} назад. Готова к бою!`;
}

export default function HeroScreen() {
  const { state, today, level, muscles, totals, goals, claimQuest } = useStore();
  const toast = useToast();
  const [selected, setSelected] = useState<MuscleId | null>(null);
  const { width } = useWindowDimensions();
  const figureWidth = Math.max(110, Math.min(170, (Math.min(width, 640) - 72) / 2));

  const todayWorkouts = useMemo(() => state.workouts.filter((w) => w.date === today), [state.workouts, today]);
  const todayFoods = useMemo(() => state.foods.filter((f) => f.date === today), [state.foods, today]);
  const quests = questStatuses(today, { workouts: todayWorkouts, foods: todayFoods, goals }, state.questClaims);
  const stats = characterStats(totals);
  const eaten = Math.round(sumFoods(todayFoods).calories);
  const todaySets = todayWorkouts.reduce((s, w) => s + w.sets, 0);
  const program = getProgram(state.activeProgramId);
  const isNew = state.workouts.length === 0 && state.foods.length === 0;
  const { rank } = level;

  const byStatus = (status: MuscleState['status']) =>
    MUSCLE_IDS.filter((m) => muscles[m].status === status).map((m) => MUSCLES[m].name);
  const loaded = byStatus('loaded');
  const recovering = byStatus('recovering');

  return (
    <Screen>
      <Card glow={rank.color + '88'} style={styles.heroCard}>
        <View style={styles.heroRow}>
          <View style={[styles.rankBox, { borderColor: rank.color, shadowColor: rank.color }]}>
            <Text style={[styles.rankLetter, { color: rank.color }]}>{rank.letter}</Text>
          </View>
          <View style={{ flex: 1, gap: 2 }}>
            <Text style={styles.heroName}>{state.profile.name}</Text>
            <Text style={styles.heroTitle}>
              Ранг {rank.letter} · {rank.title}
            </Text>
          </View>
          <View style={{ alignItems: 'flex-end' }}>
            <Text style={styles.levelLabel}>УРОВЕНЬ</Text>
            <Text style={styles.levelValue}>{level.level}</Text>
          </View>
        </View>
        <ProgressBar value={level.progress} color={colors.xp} height={10} />
        <View style={styles.xpRow}>
          <Text style={styles.xpText}>
            {level.current} / {level.needed} XP
          </Text>
          <Text style={styles.streak}>
            🔥 Серия: {totals.currentStreak} {plural(totals.currentStreak, ['день', 'дня', 'дней'])}
          </Text>
        </View>
        <View style={styles.statsRow}>
          {stats.map((s) => (
            <View key={s.key} style={styles.statBox}>
              <Text style={[styles.statLabel, { color: s.color }]}>{s.label}</Text>
              <Text style={styles.statValue}>{s.value}</Text>
            </View>
          ))}
        </View>
      </Card>

      {isNew ? (
        <Card glow={colors.primary}>
          <Text style={styles.system}>[ СИСТЕМА ]</Text>
          <Text style={styles.welcome}>
            Добро пожаловать, {state.profile.name}! Записывай тренировки и еду, выполняй ежедневные задания и прокачивай героя от ранга E до S.
          </Text>
          <Button title="Выбрать программу" icon="list" onPress={() => router.navigate('/programs')} />
        </Card>
      ) : null}

      <Card>
        <SectionTitle>Состояние мышц</SectionTitle>
        <BodyMap
          muscles={muscles}
          selected={selected}
          onSelect={(m) => setSelected((cur) => (cur === m ? null : m))}
          figureWidth={figureWidth}
          aura={rank.color}
        />
        <StatusLegend muscles={muscles} />
        {selected ? (
          <View style={[styles.info, { borderColor: STATUS_INFO[muscles[selected].status].color }]}>
            <View style={styles.infoHeader}>
              <Text style={styles.infoTitle}>{MUSCLES[selected].name}</Text>
              <Text style={[styles.infoStatus, { color: STATUS_INFO[muscles[selected].status].color }]}>
                {STATUS_INFO[muscles[selected].status].label}
              </Text>
            </View>
            <Text style={styles.infoText}>{describeMuscle(muscles[selected])}</Text>
          </View>
        ) : (
          <View style={{ gap: 4 }}>
            {loaded.length ? (
              <Text style={styles.summary}>
                <Text style={{ color: colors.loaded, fontWeight: '700' }}>Сегодня: </Text>
                {loaded.join(', ')}
              </Text>
            ) : null}
            {recovering.length ? (
              <Text style={styles.summary}>
                <Text style={{ color: colors.recovering, fontWeight: '700' }}>Вчера: </Text>
                {recovering.join(', ')}
              </Text>
            ) : null}
            <Text style={styles.hint}>
              {loaded.length || recovering.length ? 'Нажми на мышцу, чтобы узнать подробности' : 'Все мышцы восстановлены — нажми на любую, чтобы узнать подробности'}
            </Text>
          </View>
        )}
      </Card>

      <Card>
        <SectionTitle>Ежедневные задания</SectionTitle>
        {quests.map((q) => (
          <View key={q.def.id} style={styles.quest}>
            <Ionicons
              name={q.claimed ? 'checkmark-circle' : q.done ? 'gift' : 'ellipse-outline'}
              size={24}
              color={q.claimed ? colors.fresh : q.done ? colors.xp : colors.textMuted}
            />
            <View style={{ flex: 1, gap: 6 }}>
              <View style={styles.questTop}>
                <Text style={[styles.questTitle, q.claimed && { color: colors.textMuted }]}>{q.def.title}</Text>
                <Text style={styles.questProgress}>
                  {Math.min(q.current, q.target)}/{q.target} {q.def.unit}
                </Text>
              </View>
              <ProgressBar value={q.current / q.target} color={q.done ? colors.fresh : colors.primary} height={6} />
            </View>
            {q.done && !q.claimed ? (
              <Button
                small
                title={`+${q.def.xp}`}
                icon="sparkles"
                onPress={() => {
                  claimQuest({ date: today, questId: q.def.id, xp: q.def.xp });
                  toast(`Задание выполнено! +${q.def.xp} XP`);
                }}
              />
            ) : (
              <Text style={[styles.questXp, q.claimed && { color: colors.textMuted }]}>{q.claimed ? 'Получено' : `${q.def.xp} XP`}</Text>
            )}
          </View>
        ))}
      </Card>

      <View style={styles.twoCols}>
        <Pressable style={styles.miniCard} onPress={() => router.navigate('/workout')}>
          <Ionicons name="barbell" size={22} color={colors.primary} />
          <Text style={styles.miniValue}>{todaySets}</Text>
          <Text style={styles.miniLabel}>
            {plural(todaySets, ['подход', 'подхода', 'подходов'])} сегодня
            {program ? `\n${program.emoji} ${program.name}` : ''}
          </Text>
        </Pressable>
        <Pressable style={styles.miniCard} onPress={() => router.navigate('/nutrition')}>
          <Ionicons name="restaurant" size={22} color={colors.carbs} />
          <Text style={styles.miniValue}>{eaten}</Text>
          <Text style={styles.miniLabel}>из {goals.calories} ккал</Text>
          <ProgressBar value={eaten / goals.calories} color={eaten > goals.calories * 1.1 ? colors.loaded : colors.carbs} height={5} />
        </Pressable>
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  heroCard: { backgroundColor: '#0B1322' },
  heroRow: { flexDirection: 'row', alignItems: 'center', gap: 14 },
  rankBox: {
    width: 56,
    height: 56,
    borderRadius: radius.md,
    borderWidth: 2,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.bg,
    shadowOpacity: 0.6,
    shadowRadius: 10,
    shadowOffset: { width: 0, height: 0 },
    elevation: 4,
  },
  rankLetter: { fontSize: 30, fontWeight: '900' },
  heroName: { color: colors.text, fontSize: 22, fontWeight: '800' },
  heroTitle: { color: colors.textDim, fontSize: 14 },
  levelLabel: { color: colors.textMuted, fontSize: 10, fontWeight: '800', letterSpacing: 1.5 },
  levelValue: { color: colors.xp, fontSize: 32, fontWeight: '900', lineHeight: 36 },
  xpRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  xpText: { color: colors.xp, fontWeight: '700', fontSize: 13 },
  streak: { color: colors.textDim, fontWeight: '600', fontSize: 13 },
  statsRow: { flexDirection: 'row', gap: 8 },
  statBox: { flex: 1, backgroundColor: colors.surfaceAlt, borderRadius: radius.sm, paddingVertical: 8, alignItems: 'center' },
  statLabel: { fontSize: 11, fontWeight: '900', letterSpacing: 1 },
  statValue: { color: colors.text, fontSize: 20, fontWeight: '800' },
  system: { color: colors.primary, fontSize: 13, fontWeight: '800', letterSpacing: 3 },
  welcome: { color: colors.text, fontSize: 15, lineHeight: 22 },
  info: { borderWidth: 1, borderRadius: radius.md, padding: 12, gap: 6, backgroundColor: colors.surfaceAlt },
  infoHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', gap: 8 },
  infoTitle: { color: colors.text, fontSize: 17, fontWeight: '800' },
  infoStatus: { fontSize: 13, fontWeight: '700' },
  infoText: { color: colors.textDim, fontSize: 14, lineHeight: 20 },
  summary: { color: colors.text, fontSize: 14, lineHeight: 20 },
  hint: { color: colors.textMuted, fontSize: 13, textAlign: 'center', marginTop: 4 },
  quest: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  questTop: { flexDirection: 'row', justifyContent: 'space-between', gap: 8 },
  questTitle: { color: colors.text, fontSize: 15, fontWeight: '600', flex: 1 },
  questProgress: { color: colors.textMuted, fontSize: 12, fontWeight: '600' },
  questXp: { color: colors.xp, fontSize: 13, fontWeight: '700', minWidth: 58, textAlign: 'right' },
  twoCols: { flexDirection: 'row', gap: 12 },
  miniCard: {
    flex: 1,
    backgroundColor: colors.surface,
    borderRadius: radius.lg,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 14,
    gap: 4,
  },
  miniValue: { color: colors.text, fontSize: 26, fontWeight: '800' },
  miniLabel: { color: colors.textDim, fontSize: 13 },
});
