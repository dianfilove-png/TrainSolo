import Ionicons from '@expo/vector-icons/Ionicons';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { addDays, formatDay } from '../logic/date';
import { colors, radius } from '../theme';

export function DaySwitcher({ date, today, onChange }: { date: string; today: string; onChange: (d: string) => void }) {
  const isToday = date >= today;
  return (
    <View style={styles.row}>
      <Pressable accessibilityRole="button" accessibilityLabel="Предыдущий день" hitSlop={8} style={styles.arrow} onPress={() => onChange(addDays(date, -1))}>
        <Ionicons name="chevron-back" size={22} color={colors.text} />
      </Pressable>
      <Pressable onPress={() => onChange(today)} style={styles.center}>
        <Ionicons name="calendar-outline" size={16} color={colors.primary} />
        <Text style={styles.label}>{formatDay(date, today)}</Text>
      </Pressable>
      <Pressable
        accessibilityRole="button"
        accessibilityLabel="Следующий день"
        hitSlop={8}
        style={[styles.arrow, isToday && { opacity: 0.25 }]}
        disabled={isToday}
        onPress={() => onChange(addDays(date, 1))}
      >
        <Ionicons name="chevron-forward" size={22} color={colors.text} />
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.surface,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.border,
  },
  arrow: { padding: 10 },
  center: { flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8 },
  label: { color: colors.text, fontSize: 16, fontWeight: '700' },
});
