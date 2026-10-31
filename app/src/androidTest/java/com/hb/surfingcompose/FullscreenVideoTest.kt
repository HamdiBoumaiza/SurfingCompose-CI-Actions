package com.hb.surfingcompose

import android.graphics.Point
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hb.surfingcompose.domain.models.InstructionsModel
import com.hb.surfingcompose.domain.models.NutritionModel
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.presentation.screens.RecipeDetailContent
import com.hb.surfingcompose.presentation.theme.SurfingComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FullscreenVideoTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun watchOpensFullscreenAndCloseOrBackReleasesPlayback() {
        val recipe = RecipeModel(
            id = 42, name = "Pasta", description = "", thumbnailUrl = "",
            originalVideoUrl = "asset:///missing-test-video.mp4", nutrition = NutritionModel(),
            instructions = listOf(InstructionsModel("Cook the pasta.", 1))
        )
        composeRule.setContent {
            SurfingComposeTheme { RecipeDetailContent(recipe) }
        }
        composeRule.onNodeWithText("Step 1").performScrollTo().performClick()
        val watchLabel = composeRule.activity.getString(R.string.see_how_to)
        composeRule.onNodeWithText(watchLabel).performScrollTo().performClick()
        val firstPlayer = assertFullscreenPlayer()
        composeRule.onNodeWithContentDescription("Close video").performClick()
        composeRule.waitUntil(5000) { !firstPlayer.playbackLooper.thread.isAlive }
        composeRule.onNodeWithText("Step 1").performScrollTo().assertIsOn()

        composeRule.onNodeWithText(watchLabel).performScrollTo().performClick()
        val secondPlayer = assertFullscreenPlayer()
        pressBack()
        composeRule.waitUntil(5000) { !secondPlayer.playbackLooper.thread.isAlive }
        composeRule.onNodeWithContentDescription("Close video").assertDoesNotExist()
        composeRule.onNodeWithText(watchLabel).assertExists()
    }

    private fun assertFullscreenPlayer(): ExoPlayer {
        lateinit var player: ExoPlayer
        onView(isAssignableFrom(PlayerView::class.java)).inRoot(isDialog()).check { view, error ->
            if (error != null) throw error
            val playerView = view as PlayerView
            val screenSize = Point()
            @Suppress("DEPRECATION")
            playerView.display.getRealSize(screenSize)
            // Dialog constraints use whole dp, so allow one dp of pixel rounding.
            val rounding = playerView.resources.displayMetrics.density.toDouble()
            assertEquals(screenSize.x.toDouble(), playerView.width.toDouble(), rounding)
            assertEquals(screenSize.y.toDouble(), playerView.height.toDouble(), rounding)
            assertEquals(playerView.rootView.width, playerView.width)
            assertEquals(playerView.rootView.height, playerView.height)
            player = playerView.player as ExoPlayer
            assertTrue(player.playWhenReady)
        }
        return player
    }
}
