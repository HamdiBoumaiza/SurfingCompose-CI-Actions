package com.hb.surfingcompose

import android.os.Bundle
import android.os.Parcel
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hb.surfingcompose.data.SurfaceResult
import com.hb.surfingcompose.data.responses.RecipeResponse
import com.hb.surfingcompose.domain.models.InstructionsModel
import com.hb.surfingcompose.domain.models.NutritionModel
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.domain.repository.RecipesRepository
import com.hb.surfingcompose.domain.request.RecipesRequest
import com.hb.surfingcompose.domain.usecase.GetRecipesUseCase
import com.hb.surfingcompose.presentation.ROUTE_LIST_RECIPES
import com.hb.surfingcompose.presentation.ROUTE_SPLASH_SCREEN
import com.hb.surfingcompose.presentation.screens.SplashScreen
import com.hb.surfingcompose.presentation.viewmodel.RecipesViewModel
import com.hb.surfingcompose.presentation.widgets.VideoPlayer
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecipeLifecycleTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun splashIsRemovedFromBackStack() {
        lateinit var navigation: NavHostController
        composeRule.setContent {
            navigation = rememberNavController()
            NavHost(navigation, startDestination = ROUTE_SPLASH_SCREEN) {
                composable(ROUTE_SPLASH_SCREEN) { SplashScreen(navigation) }
                composable(ROUTE_LIST_RECIPES) { Text("Recipes") }
            }
        }
        composeRule.mainClock.advanceTimeBy(2100)
        composeRule.waitUntil(5000) { navigation.currentDestination?.route == ROUTE_LIST_RECIPES }
        composeRule.runOnIdle { assertNull(navigation.previousBackStackEntry) }
    }

    @Test
    fun removingVideoReleasesPlaybackThread() {
        val visible = mutableStateOf(true)
        composeRule.setContent {
            if (visible.value) VideoPlayer("asset:///missing-test-video.mp4")
        }
        lateinit var player: ExoPlayer
        composeRule.runOnIdle {
            val view = findPlayerView(composeRule.activity.window.decorView)
            assertNotNull(view)
            player = view!!.player as ExoPlayer
            assertTrue(player.playbackLooper.thread.isAlive)
            visible.value = false
        }
        composeRule.waitForIdle()
        composeRule.waitUntil(5000) { !player.playbackLooper.thread.isAlive }
    }

    @Test
    fun selectedRecipeSurvivesSavedStateParcelRoundTrip() {
        val repository = object : RecipesRepository {
            override suspend fun saveRecipes(list: List<RecipeResponse>) = Unit
            override suspend fun getRecipes(recipesRequest: RecipesRequest) = flowOf(SurfaceResult.Success(emptyList<RecipeModel>()))
        }
        composeRule.runOnIdle {
            val state = SavedStateHandle()
            val original = RecipesViewModel(GetRecipesUseCase(repository), state)
            original.setRecipe(RecipeModel(
                id = 7, name = "Soup", instructions = listOf(InstructionsModel("Cook", 1)),
                nutrition = NutritionModel(), description = "Warm soup", originalVideoUrl = "", thumbnailUrl = ""
            ))
            val parcel = Parcel.obtain()
            try {
                parcel.writeBundle(state.savedStateProvider().saveState())
                parcel.setDataPosition(0)
                val restoredBundle: Bundle = parcel.readBundle(RecipeModel::class.java.classLoader)!!
                val restored = RecipesViewModel(
                    GetRecipesUseCase(repository), SavedStateHandle.createHandle(restoredBundle, null)
                )
                assertEquals(original.getRecipe(), restored.getRecipe())
            } finally {
                parcel.recycle()
            }
        }
    }

    private fun findPlayerView(view: View): PlayerView? {
        if (view is PlayerView) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findPlayerView(view.getChildAt(index))?.let { return it }
            }
        }
        return null
    }
}
