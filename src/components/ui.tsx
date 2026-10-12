import Ionicons from '@expo/vector-icons/Ionicons';
import type { ComponentProps, ReactNode } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, TextInput, View, type StyleProp, type ViewStyle } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { colors, radius, spacing } from '../theme';

export type IconName = ComponentProps<typeof Ionicons>['name'];

export function Screen({ children, title, subtitle, right }: { children: ReactNode; title?: string; subtitle?: string; right?: ReactNode }) {
  const insets = useSafeAreaInsets();
  return (
    <ScrollView
      style={styles.screen}
      contentContainerStyle={[styles.screenContent, { paddingTop: insets.top + spacing.md }]}
      keyboardShouldPersistTaps="handled"
    >
      {title ? (
        <View style={styles.header}>
          <View style={{ flex: 1 }}>
            <Text style={styles.title}>{title}</Text>
            {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : null}
          </View>
          {right}
        </View>
      ) : null}
      {children}
    </ScrollView>
  );
}

export function Card({ children, style, glow }: { children: ReactNode; style?: StyleProp<ViewStyle>; glow?: string }) {
  return <View style={[styles.card, glow ? { borderColor: glow } : null, style]}>{children}</View>;
}

export function SectionTitle({ children, right }: { children: ReactNode; right?: ReactNode }) {
  return (
    <View style={styles.sectionRow}>
      <Text style={styles.sectionTitle}>{children}</Text>
      {right}
    </View>
  );
}

export function ProgressBar({ value, color = colors.primary, height = 8 }: { value: number; color?: string; height?: number }) {
  const pct = Math.max(0, Math.min(1, value)) * 100;
  return (
    <View style={[styles.track, { height, borderRadius: height / 2 }]}>
      <View style={{ width: `${pct}%`, height, borderRadius: height / 2, backgroundColor: color }} />
    </View>
  );
}

type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';

export function Button({
  title,
  onPress,
  icon,
  variant = 'primary',
  disabled,
  small,
  style,
}: {
  title: string;
  onPress: () => void;
  icon?: IconName;
  variant?: ButtonVariant;
  disabled?: boolean;
  small?: boolean;
  style?: StyleProp<ViewStyle>;
}) {
  const palette: Record<ButtonVariant, { bg: string; fg: string; border: string }> = {
    primary: { bg: colors.primary, fg: '#03121F', border: colors.primary },
    secondary: { bg: colors.surfaceAlt, fg: colors.text, border: colors.border },
    ghost: { bg: 'transparent', fg: colors.primary, border: 'transparent' },
    danger: { bg: 'transparent', fg: colors.danger, border: colors.danger },
  };
  const p = palette[variant];
  return (
    <Pressable
      accessibilityRole="button"
      onPress={onPress}
      disabled={disabled}
      style={({ pressed }) => [
        styles.button,
        small && styles.buttonSmall,
        { backgroundColor: p.bg, borderColor: p.border, opacity: disabled ? 0.4 : pressed ? 0.75 : 1 },
        style,
      ]}
    >
      {icon ? <Ionicons name={icon} size={small ? 15 : 18} color={p.fg} /> : null}
      <Text style={[styles.buttonText, small && styles.buttonTextSmall, { color: p.fg }]}>{title}</Text>
    </Pressable>
  );
}

export function IconButton({ icon, onPress, color = colors.textDim, label }: { icon: IconName; onPress: () => void; color?: string; label: string }) {
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={label}
      onPress={onPress}
      hitSlop={10}
      style={({ pressed }) => [styles.iconButton, pressed && { opacity: 0.6 }]}
    >
      <Ionicons name={icon} size={20} color={color} />
    </Pressable>
  );
}

export function Chip({ label, active, onPress, color = colors.primary }: { label: string; active?: boolean; onPress?: () => void; color?: string }) {
  return (
    <Pressable
      onPress={onPress}
      disabled={!onPress}
      style={[styles.chip, active && { backgroundColor: color + '26', borderColor: color }]}
    >
      <Text style={[styles.chipText, active && { color }]}>{label}</Text>
    </Pressable>
  );
}

export function Badge({ label, color }: { label: string; color: string }) {
  return (
    <View style={[styles.badge, { backgroundColor: color + '22', borderColor: color + '66' }]}>
      <Text style={[styles.badgeText, { color }]}>{label}</Text>
    </View>
  );
}

const parseNumber = (text: string) => {
  const n = parseFloat(text.replace(',', '.'));
  return Number.isFinite(n) ? n : 0;
};

/** Числовое поле с кнопками − / +. */
export function Stepper({
  label,
  value,
  onChange,
  step = 1,
  min = 0,
  decimals = false,
  suffix,
}: {
  label: string;
  value: number;
  onChange: (v: number) => void;
  step?: number;
  min?: number;
  decimals?: boolean;
  suffix?: string;
}) {
  const clamp = (v: number) => Math.max(min, Math.round(v * 100) / 100);
  return (
    <View style={styles.stepper}>
      <Text style={styles.inputLabel}>
        {label}
        {suffix ? <Text style={{ color: colors.textMuted }}> · {suffix}</Text> : null}
      </Text>
      <View style={styles.stepperRow}>
        <Pressable accessibilityRole="button" accessibilityLabel={`Уменьшить: ${label}`} style={styles.stepBtn} onPress={() => onChange(clamp(value - step))}>
          <Ionicons name="remove" size={18} color={colors.text} />
        </Pressable>
        <TextInput
          style={styles.stepInput}
          value={String(value)}
          onChangeText={(t) => onChange(clamp(parseNumber(t)))}
          keyboardType={decimals ? 'decimal-pad' : 'number-pad'}
          selectTextOnFocus
        />
        <Pressable accessibilityRole="button" accessibilityLabel={`Увеличить: ${label}`} style={styles.stepBtn} onPress={() => onChange(clamp(value + step))}>
          <Ionicons name="add" size={18} color={colors.text} />
        </Pressable>
      </View>
    </View>
  );
}

export function Field({
  label,
  value,
  onChangeText,
  placeholder,
  numeric,
}: {
  label: string;
  value: string;
  onChangeText: (t: string) => void;
  placeholder?: string;
  numeric?: boolean;
}) {
  return (
    <View style={{ flex: 1, gap: 6 }}>
      <Text style={styles.inputLabel}>{label}</Text>
      <TextInput
        style={styles.input}
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor={colors.textMuted}
        keyboardType={numeric ? 'decimal-pad' : 'default'}
      />
    </View>
  );
}

export function Empty({ icon, text }: { icon: IconName; text: string }) {
  return (
    <View style={styles.empty}>
      <Ionicons name={icon} size={28} color={colors.textMuted} />
      <Text style={styles.emptyText}>{text}</Text>
    </View>
  );
}

export { parseNumber };

export const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: colors.bg },
  screenContent: { padding: spacing.lg, paddingBottom: 40, gap: spacing.lg, width: '100%', maxWidth: 640, alignSelf: 'center' },
  header: { flexDirection: 'row', alignItems: 'center', gap: spacing.md },
  title: { color: colors.text, fontSize: 28, fontWeight: '800', letterSpacing: 0.3 },
  subtitle: { color: colors.textDim, fontSize: 14, marginTop: 2 },
  card: {
    backgroundColor: colors.surface,
    borderRadius: radius.lg,
    borderWidth: 1,
    borderColor: colors.border,
    padding: spacing.lg,
    gap: spacing.md,
  },
  sectionRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  sectionTitle: { color: colors.text, fontSize: 17, fontWeight: '700' },
  track: { backgroundColor: colors.surfaceAlt, overflow: 'hidden', width: '100%' },
  button: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    paddingVertical: 13,
    paddingHorizontal: 18,
    borderRadius: radius.md,
    borderWidth: 1,
  },
  buttonSmall: { paddingVertical: 7, paddingHorizontal: 12, borderRadius: radius.sm, gap: 5 },
  buttonText: { fontSize: 16, fontWeight: '700' },
  buttonTextSmall: { fontSize: 13 },
  iconButton: { padding: 4 },
  chip: {
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 999,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.surface,
  },
  chipText: { color: colors.textDim, fontSize: 13, fontWeight: '600' },
  badge: { paddingHorizontal: 8, paddingVertical: 2, borderRadius: 999, borderWidth: 1, alignSelf: 'flex-start' },
  badgeText: { fontSize: 11, fontWeight: '700' },
  stepper: { flex: 1, gap: 6 },
  stepperRow: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.surfaceAlt, borderRadius: radius.md, borderWidth: 1, borderColor: colors.border },
  stepBtn: { paddingHorizontal: 12, paddingVertical: 12 },
  stepInput: { flex: 1, minWidth: 30, color: colors.text, fontSize: 18, fontWeight: '700', textAlign: 'center', paddingVertical: 8 },
  inputLabel: { color: colors.textDim, fontSize: 13, fontWeight: '600' },
  input: {
    backgroundColor: colors.surfaceAlt,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.border,
    color: colors.text,
    fontSize: 16,
    paddingHorizontal: 14,
    paddingVertical: 12,
  },
  empty: { alignItems: 'center', gap: 8, paddingVertical: 18 },
  emptyText: { color: colors.textMuted, fontSize: 14, textAlign: 'center' },
});
