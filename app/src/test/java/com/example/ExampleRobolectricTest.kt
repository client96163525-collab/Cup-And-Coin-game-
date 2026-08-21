package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.GameRepository
import com.example.model.CoinTheme
import com.example.model.CupTheme
import com.example.model.GameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("3 Cup Coin", appName)
    }

    @Test
    fun `verify game repository stats tracking`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = GameRepository(context)
        repository.resetProgress()

        assertEquals(0, repository.stats.value.gamesPlayed)
        assertEquals(0, repository.stats.value.bestScore)

        repository.recordGameRound(isWin = true, score = 150, level = 2, combo = 2)

        assertEquals(1, repository.stats.value.gamesPlayed)
        assertEquals(1, repository.stats.value.gamesWon)
        assertEquals(0, repository.stats.value.gamesLost)
        assertEquals(150, repository.stats.value.bestScore)
        assertEquals(2, repository.stats.value.highestLevel)
        assertEquals(2, repository.stats.value.bestCombo)
        assertEquals(100, repository.stats.value.winRatePercent)
    }

    @Test
    fun `verify game modes available`() {
        assertEquals(5, GameMode.entries.size)
        assertTrue(GameMode.entries.contains(GameMode.CLASSIC))
        assertTrue(GameMode.entries.contains(GameMode.TIME_ATTACK))
        assertTrue(GameMode.entries.contains(GameMode.ENDLESS))
        assertTrue(GameMode.entries.contains(GameMode.PERFECT_RUN))
        assertTrue(GameMode.entries.contains(GameMode.DAILY_CHALLENGE))
    }
}
