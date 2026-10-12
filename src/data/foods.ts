export interface FoodPreset {
  name: string;
  /** Обычная порция в граммах. */
  portion: number;
  /** Значения на 100 г. */
  calories: number;
  protein: number;
  fat: number;
  carbs: number;
}

export const FOOD_PRESETS: FoodPreset[] = [
  { name: 'Яйцо куриное', portion: 55, calories: 157, protein: 12.7, fat: 11.5, carbs: 0.7 },
  { name: 'Овсянка на воде', portion: 250, calories: 88, protein: 3, fat: 1.7, carbs: 15 },
  { name: 'Гречка варёная', portion: 200, calories: 110, protein: 4.2, fat: 1.1, carbs: 21.3 },
  { name: 'Рис варёный', portion: 200, calories: 130, protein: 2.7, fat: 0.3, carbs: 28 },
  { name: 'Макароны варёные', portion: 200, calories: 112, protein: 3.5, fat: 0.4, carbs: 23 },
  { name: 'Картофель отварной', portion: 200, calories: 82, protein: 2, fat: 0.4, carbs: 16.7 },
  { name: 'Куриная грудка', portion: 150, calories: 165, protein: 31, fat: 3.6, carbs: 0 },
  { name: 'Говядина тушёная', portion: 150, calories: 232, protein: 16.8, fat: 18.3, carbs: 0 },
  { name: 'Лосось', portion: 150, calories: 208, protein: 20, fat: 13, carbs: 0 },
  { name: 'Тунец консервированный', portion: 100, calories: 116, protein: 26, fat: 1, carbs: 0 },
  { name: 'Творог 5%', portion: 200, calories: 121, protein: 17.2, fat: 5, carbs: 1.8 },
  { name: 'Греческий йогурт', portion: 150, calories: 66, protein: 9, fat: 2, carbs: 3.6 },
  { name: 'Молоко 2.5%', portion: 250, calories: 52, protein: 2.8, fat: 2.5, carbs: 4.7 },
  { name: 'Кефир 1%', portion: 250, calories: 40, protein: 3, fat: 1, carbs: 4 },
  { name: 'Сыр', portion: 30, calories: 356, protein: 24, fat: 29, carbs: 0 },
  { name: 'Хлеб цельнозерновой', portion: 40, calories: 247, protein: 13, fat: 3.4, carbs: 41 },
  { name: 'Банан', portion: 120, calories: 89, protein: 1.1, fat: 0.3, carbs: 22.8 },
  { name: 'Яблоко', portion: 180, calories: 52, protein: 0.3, fat: 0.2, carbs: 13.8 },
  { name: 'Овощной салат', portion: 200, calories: 30, protein: 1.3, fat: 0.2, carbs: 5.5 },
  { name: 'Авокадо', portion: 100, calories: 160, protein: 2, fat: 14.7, carbs: 8.5 },
  { name: 'Орехи грецкие', portion: 30, calories: 654, protein: 15.2, fat: 65.2, carbs: 13.7 },
  { name: 'Арахисовая паста', portion: 20, calories: 588, protein: 25, fat: 50, carbs: 20 },
  { name: 'Протеиновый коктейль', portion: 30, calories: 380, protein: 75, fat: 5, carbs: 8 },
  { name: 'Шоколад тёмный', portion: 25, calories: 546, protein: 4.9, fat: 31, carbs: 61 },
  { name: 'Пицца', portion: 150, calories: 266, protein: 11, fat: 10, carbs: 33 },
  { name: 'Бургер', portion: 220, calories: 254, protein: 13, fat: 12, carbs: 24 },
];
