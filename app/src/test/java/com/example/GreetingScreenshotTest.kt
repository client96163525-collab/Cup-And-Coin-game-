package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.GameSettings
import com.example.model.PlayerStats
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun game_homescreen_screenshot() {
        composeTestRule.setContent {
            MyApplicationTheme {
                HomeScreen(
                    stats = PlayerStats(bestScore = 2450, highestLevel = 8, gamesPlayed = 15, gamesWon = 12),
                    settings = GameSettings(),
                    isDailyCompleted = false,
                    isLuckySpinCompleted = false,
                    onSpinClaimed = {},
                    onStartGame = {},
                    onOpenHowToPlay = {},
                    onSelectCupTheme = {},
                    onSelectCoinTheme = {},
                    onSelectShuffleTheme = {},
                    onToggleSound = {},
                    onToggleVibration = {},
                    onToggleReducedMotion = {},
                    onAppThemeChange = {},
                    onResetProgress = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
