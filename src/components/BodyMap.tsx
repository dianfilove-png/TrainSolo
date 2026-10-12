import { Pressable, StyleSheet, Text, View } from 'react-native';
import Svg, { Defs, Ellipse, G, Path, RadialGradient, Stop } from 'react-native-svg';

import { MUSCLES, MUSCLE_IDS } from '../data/muscles';
import { STATUS_INFO, type MuscleState, type MuscleStatus } from '../logic/recovery';
import { colors } from '../theme';
import type { MuscleId } from '../types';
import { BACK_SHAPES, BODY_VIEWBOX, FRONT_SHAPES, SILHOUETTE, type MuscleShape } from './bodyPaths';

const MIRROR = 'translate(200,0) scale(-1,1)';

interface FigureProps {
  id: string;
  shapes: MuscleShape[];
  front: boolean;
  muscles: Record<MuscleId, MuscleState>;
  selected: MuscleId | null;
  onSelect?: (m: MuscleId) => void;
  width: number;
  aura: string;
}

function Figure({ id, shapes, front, muscles, selected, onSelect, width, aura }: FigureProps) {
  const renderMuscles = (mirror: boolean) => (
    <G transform={mirror ? MIRROR : undefined}>
      {shapes.map((s, i) => {
        const isSelected = selected === s.muscle;
        return (
          <Path
            key={i}
            d={s.d}
            fill={STATUS_INFO[muscles[s.muscle].status].color}
            fillOpacity={selected && !isSelected ? 0.4 : 0.92}
            stroke={isSelected ? '#FFFFFF' : colors.bg}
            strokeWidth={isSelected ? 1.8 : 0.8}
            onPress={onSelect ? () => onSelect(s.muscle) : undefined}
          />
        );
      })}
    </G>
  );

  return (
    <Svg viewBox={BODY_VIEWBOX} width={width} height={width * 2}>
      <Defs>
        <RadialGradient id={`aura-${id}`} cx="50%" cy="45%" rx="50%" ry="50%">
          <Stop offset="0" stopColor={aura} stopOpacity={0.28} />
          <Stop offset="1" stopColor={aura} stopOpacity={0} />
        </RadialGradient>
      </Defs>
      <Ellipse cx={100} cy={190} rx={100} ry={200} fill={`url(#aura-${id})`} />
      <Ellipse cx={100} cy={393} rx={42} ry={5} fill="#000" fillOpacity={0.5} />

      {/* Сначала контур с толстой обводкой, затем заливка поверх — так не видно шва по центру. */}
      <Path d={SILHOUETTE} fill={colors.body} stroke={colors.bodyStroke} strokeWidth={2.4} />
      <Path d={SILHOUETTE} transform={MIRROR} fill={colors.body} stroke={colors.bodyStroke} strokeWidth={2.4} />
      <Path d={SILHOUETTE} fill={colors.body} />
      <Path d={SILHOUETTE} transform={MIRROR} fill={colors.body} />

      <Ellipse cx={100} cy={25} rx={17} ry={21} fill={colors.body} stroke={colors.bodyStroke} strokeWidth={1.2} />
      {front ? (
        <>
          <Ellipse cx={93} cy={26} rx={3.4} ry={1.6} fill={aura} />
          <Ellipse cx={107} cy={26} rx={3.4} ry={1.6} fill={aura} />
        </>
      ) : null}

      {renderMuscles(false)}
      {renderMuscles(true)}
    </Svg>
  );
}

interface BodyMapProps {
  muscles: Record<MuscleId, MuscleState>;
  selected?: MuscleId | null;
  onSelect?: (m: MuscleId) => void;
  figureWidth?: number;
  aura?: string;
}

export function BodyMap({ muscles, selected = null, onSelect, figureWidth = 150, aura = colors.primary }: BodyMapProps) {
  return (
    <View style={styles.row}>
      <View style={styles.figure}>
        <Figure id="front" shapes={FRONT_SHAPES} front muscles={muscles} selected={selected} onSelect={onSelect} width={figureWidth} aura={aura} />
        <Text style={styles.caption}>Спереди</Text>
      </View>
      <View style={styles.figure}>
        <Figure id="back" shapes={BACK_SHAPES} front={false} muscles={muscles} selected={selected} onSelect={onSelect} width={figureWidth} aura={aura} />
        <Text style={styles.caption}>Сзади</Text>
      </View>
    </View>
  );
}

export function StatusLegend({ muscles }: { muscles: Record<MuscleId, MuscleState> }) {
  const count = (s: MuscleStatus) => MUSCLE_IDS.filter((m) => muscles[m].status === s).length;
  return (
    <View style={styles.legend}>
      {(['loaded', 'recovering', 'fresh'] as MuscleStatus[]).map((s) => (
        <View key={s} style={styles.legendItem}>
          <View style={[styles.dot, { backgroundColor: STATUS_INFO[s].color }]} />
          <Text style={styles.legendText}>
            {STATUS_INFO[s].short} · {count(s)}
          </Text>
        </View>
      ))}
    </View>
  );
}

/** Сетка всех мышц с их состоянием — дублирует карту в текстовом виде и позволяет выбрать мышцу. */
export function MuscleChips({
  muscles,
  selected,
  onSelect,
}: {
  muscles: Record<MuscleId, MuscleState>;
  selected: MuscleId | null;
  onSelect: (m: MuscleId) => void;
}) {
  return (
    <View style={styles.chips}>
      {MUSCLE_IDS.map((m) => {
        const color = STATUS_INFO[muscles[m].status].color;
        const active = selected === m;
        return (
          <Pressable
            key={m}
            onPress={() => onSelect(m)}
            style={[styles.muscleChip, { borderColor: active ? color : colors.border, backgroundColor: active ? color + '22' : colors.surfaceAlt }]}
          >
            <View style={[styles.dotSmall, { backgroundColor: color }]} />
            <Text style={styles.muscleChipText}>{MUSCLES[m].name}</Text>
          </Pressable>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', justifyContent: 'center', gap: 4 },
  figure: { alignItems: 'center' },
  caption: { color: colors.textMuted, fontSize: 12, fontWeight: '600', marginTop: 2, letterSpacing: 1, textTransform: 'uppercase' },
  legend: { flexDirection: 'row', justifyContent: 'center', gap: 14, flexWrap: 'wrap' },
  legendItem: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  dot: { width: 10, height: 10, borderRadius: 5 },
  dotSmall: { width: 8, height: 8, borderRadius: 4 },
  legendText: { color: colors.textDim, fontSize: 13, fontWeight: '600' },
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: 6 },
  muscleChip: { flexDirection: 'row', alignItems: 'center', gap: 6, paddingHorizontal: 10, paddingVertical: 6, borderRadius: 999, borderWidth: 1 },
  muscleChipText: { color: colors.text, fontSize: 12, fontWeight: '600' },
});
