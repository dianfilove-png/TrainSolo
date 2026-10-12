import { createContext, useCallback, useContext, useEffect, useRef, useState, type ReactNode } from 'react';
import { Animated, Modal, Platform, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { useStore } from '../store/AppStore';
import { colors, radius } from '../theme';
import { rankForLevel } from '../logic/xp';
import { Button } from './ui';

const useNativeDriver = Platform.OS !== 'web';

const FeedbackContext = createContext<{ toast: (text: string) => void }>({ toast: () => {} });

export function useToast() {
  return useContext(FeedbackContext).toast;
}

export function FeedbackProvider({ children }: { children: ReactNode }) {
  const insets = useSafeAreaInsets();
  const [message, setMessage] = useState<string | null>(null);
  const [anim] = useState(() => new Animated.Value(0));
  const hideTimer = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  const toast = useCallback(
    (text: string) => {
      setMessage(text);
      clearTimeout(hideTimer.current);
      Animated.spring(anim, { toValue: 1, useNativeDriver, friction: 7 }).start();
      hideTimer.current = setTimeout(() => {
        Animated.timing(anim, { toValue: 0, duration: 220, useNativeDriver }).start(() => setMessage(null));
      }, 1800);
    },
    [anim],
  );

  useEffect(() => () => clearTimeout(hideTimer.current), []);

  return (
    <FeedbackContext.Provider value={{ toast }}>
      {children}
      {message ? (
        <Animated.View
          pointerEvents="none"
          style={[
            styles.toast,
            {
              top: insets.top + 10,
              opacity: anim,
              transform: [{ translateY: anim.interpolate({ inputRange: [0, 1], outputRange: [-30, 0] }) }],
            },
          ]}
        >
          <Text style={styles.toastText}>{message}</Text>
        </Animated.View>
      ) : null}
      <LevelUpWatcher />
    </FeedbackContext.Provider>
  );
}

/**
 * Показывает окно «Уровень повышен», когда уровень превышает последний просмотренный.
 * Провайдер монтируется после загрузки данных, поэтому стартовый уровень окна не вызывает.
 */
function LevelUpWatcher() {
  const { level } = useStore();
  const [seenLevel, setSeenLevel] = useState(level.level);
  const [scale] = useState(() => new Animated.Value(0.6));
  const showing = level.level > seenLevel;

  // Уровень мог упасть (удалили записи или сбросили прогресс) — запоминаем новый минимум.
  if (level.level < seenLevel) setSeenLevel(level.level);

  useEffect(() => {
    if (!showing) return;
    scale.setValue(0.6);
    Animated.spring(scale, { toValue: 1, friction: 5, useNativeDriver }).start();
  }, [showing, level.level, scale]);

  if (!showing) return null;
  const rank = level.rank;
  const newRank = rankForLevel(seenLevel).letter !== rank.letter;
  const close = () => setSeenLevel(level.level);
  return (
    <Modal transparent animationType="fade" visible onRequestClose={close}>
      <View style={styles.backdrop}>
        <Animated.View style={[styles.levelCard, { borderColor: rank.color, transform: [{ scale }] }]}>
          <Text style={styles.system}>[ СИСТЕМА ]</Text>
          <Text style={styles.levelUp}>УРОВЕНЬ ПОВЫШЕН!</Text>
          <Text style={[styles.levelNumber, { color: rank.color }]}>{level.level}</Text>
          {newRank ? (
            <Text style={styles.rankText}>
              Новый ранг: <Text style={{ color: rank.color, fontWeight: '900' }}>{rank.letter}</Text> — {rank.title}
            </Text>
          ) : (
            <Text style={styles.rankText}>Ты стал сильнее. Продолжай в том же духе.</Text>
          )}
          <Button title="Продолжить" onPress={close} style={{ alignSelf: 'stretch' }} />
        </Animated.View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  toast: {
    position: 'absolute',
    alignSelf: 'center',
    backgroundColor: '#0B1A2E',
    borderColor: colors.xp,
    borderWidth: 1,
    borderRadius: 999,
    paddingHorizontal: 18,
    paddingVertical: 10,
    shadowColor: colors.xp,
    shadowOpacity: 0.4,
    shadowRadius: 12,
    elevation: 6,
  },
  toastText: { color: colors.xp, fontWeight: '800', fontSize: 15 },
  backdrop: { flex: 1, backgroundColor: 'rgba(2,6,15,0.85)', alignItems: 'center', justifyContent: 'center', padding: 24 },
  levelCard: {
    width: '100%',
    maxWidth: 360,
    backgroundColor: colors.surface,
    borderWidth: 1.5,
    borderRadius: radius.xl,
    padding: 24,
    alignItems: 'center',
    gap: 10,
  },
  system: { color: colors.primary, fontSize: 13, fontWeight: '800', letterSpacing: 3 },
  levelUp: { color: colors.text, fontSize: 24, fontWeight: '900', letterSpacing: 1 },
  levelNumber: { fontSize: 72, fontWeight: '900', lineHeight: 80 },
  rankText: { color: colors.textDim, fontSize: 15, textAlign: 'center', marginBottom: 8 },
});
