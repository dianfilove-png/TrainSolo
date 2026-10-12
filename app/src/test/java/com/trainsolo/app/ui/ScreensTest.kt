package com.trainsolo.app.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.trainsolo.app.model.FoodEntry
import com.trainsolo.app.model.Meal
import com.trainsolo.app.store.AppStore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Рендерит все экраны с тестовыми данными и сохраняет скриншоты в build/screens. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h1600dp-xhdpi")
class ScreensTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private fun shot(name: String) {
        rule.waitForIdle()
        val dir = File("build/screens").apply { mkdirs() }
        // Модальные окна живут в отдельных окнах — снимаем их корень, если он открыт.
        val root = rule.activity.window.decorView.rootView
        val bitmap = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
        root.draw(Canvas(bitmap))
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun allScreens() {
        val store = AppStore(ApplicationProvider.getApplicationContext())
        val today = store.today
        store.addWorkout(today, "bench_press", 4, 8, 60.0, 0, 0.0)
        store.addWorkout(today, "overhead_press", 3, 10, 35.0, 0, 0.0)
        store.addWorkout(today.minusDays(1), "squat", 4, 8, 70.0, 0, 0.0)
        store.addWorkout(today.minusDays(1), "romanian_deadlift", 3, 10, 60.0, 0, 0.0)
        store.addFood(FoodEntry("", today, Meal.BREAKFAST, "Овсянка на воде", 250.0, 220.0, 7.5, 4.3, 37.5, 0))
        store.addFood(FoodEntry("", today, Meal.LUNCH, "Куриная грудка", 150.0, 248.0, 46.5, 5.4, 0.0, 0))
        store.setActiveProgram("ppl")

        rule.setContent { TrainSoloTheme { App(store) } }
        rule.onNodeWithText("Состояние мышц").assertIsDisplayed()
        shot("1-hero")

        rule.onNodeWithText("Тренировка").performClick()
        rule.onNodeWithText("Push / Pull / Legs", substring = true).assertIsDisplayed()
        shot("2-workout")

        rule.onNodeWithText("Добавить упражнение", substring = true).performClick()
        rule.onAllNodes(hasSetTextAction())[0].performTextInput("ногами")
        rule.onNodeWithText("Жим ногами").performClick()
        rule.onNodeWithText("Награда: +30 XP", substring = true).assertIsDisplayed()
        rule.onNodeWithText("Записать", substring = true).performClick()
        rule.waitForIdle()
        check(store.state.workouts.any { it.exerciseId == "leg_press" }) { "упражнение не записалось" }

        rule.onNodeWithText("Программы").performClick()
        shot("4-programs")
        rule.onNodeWithText("Путь Охотника").performClick()
        shot("5-program-detail")

        rule.onNodeWithText("Питание").performClick()
        rule.onNodeWithContentDescription("Добавить: Ужин").performClick()
        rule.onAllNodes(hasSetTextAction())[0].performTextInput("Банан")
        rule.onAllNodesWithText("Банан")[1].performClick()
        rule.onAllNodes(androidx.compose.ui.test.hasText("Добавить", substring = true) and androidx.compose.ui.test.hasClickAction())[0].performClick()
        rule.waitForIdle()
        check(store.state.foods.any { it.name == "Банан" && it.meal == Meal.DINNER }) { "еда не записалась" }
        shot("6-nutrition")

        rule.onNodeWithText("Профиль").performClick()
        shot("7-profile")
    }
}
