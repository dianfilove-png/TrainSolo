import { getMuscleStates, statusForDays } from '../recovery';
import { workout } from '../../test-utils/fixtures';

const TODAY = '2026-10-12';

describe('statusForDays', () => {
  it('сегодня — красный, вчера — жёлтый, потом — зелёный', () => {
    expect(statusForDays(0)).toBe('loaded');
    expect(statusForDays(1)).toBe('recovering');
    expect(statusForDays(2)).toBe('fresh');
    expect(statusForDays(10)).toBe('fresh');
    expect(statusForDays(null)).toBe('fresh');
  });
});

describe('getMuscleStates', () => {
  it('без тренировок все мышцы зелёные', () => {
    const states = getMuscleStates([], TODAY);
    expect(Object.values(states).every((s) => s.status === 'fresh')).toBe(true);
  });

  it('жим лёжа сегодня нагружает грудь, трицепс и плечи', () => {
    const states = getMuscleStates([workout(TODAY, 'bench_press', { sets: 4 })], TODAY);
    expect(states.chest).toMatchObject({ status: 'loaded', daysSince: 0, sets: 4 });
    expect(states.triceps).toMatchObject({ status: 'loaded', sets: 2 });
    expect(states.shoulders.status).toBe('loaded');
    expect(states.quads.status).toBe('fresh');
  });

  it('вчерашняя тренировка — жёлтая, позавчерашняя — зелёная', () => {
    const states = getMuscleStates(
      [workout('2026-10-11', 'squat'), workout('2026-10-10', 'pull_ups')],
      TODAY,
    );
    expect(states.quads.status).toBe('recovering');
    expect(states.lats.status).toBe('fresh');
    expect(states.lats.daysSince).toBe(2);
  });

  it('учитывается самая свежая нагрузка и суммируются подходы за этот день', () => {
    const states = getMuscleStates(
      [
        workout('2026-10-01', 'bench_press', { sets: 5 }),
        workout(TODAY, 'push_ups', { sets: 3 }),
        workout(TODAY, 'dips', { sets: 2 }),
      ],
      TODAY,
    );
    expect(states.chest).toMatchObject({ status: 'loaded', lastTrained: TODAY, sets: 5 });
  });

  it('записи из будущего игнорируются', () => {
    const states = getMuscleStates([workout('2026-10-13', 'squat')], TODAY);
    expect(states.quads.status).toBe('fresh');
  });

  it('работает через границу месяца', () => {
    const states = getMuscleStates([workout('2026-09-30', 'squat')], '2026-10-01');
    expect(states.quads.status).toBe('recovering');
  });
});
