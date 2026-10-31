package com.hb.surfingcompose

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hb.surfingcompose.domain.models.InstructionsModel
import com.hb.surfingcompose.domain.models.NutritionModel
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.presentation.screens.RecipeDetailContent
import com.hb.surfingcompose.presentation.screens.RecipesContent
import com.hb.surfingcompose.presentation.theme.SurfingComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecipeScreensTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun checkedStepsSurviveStateRestorationAndCanBeUnchecked() {
        val restoration = StateRestorationTester(composeRule)
        restoration.setContent {
            SurfingComposeTheme(darkTheme = true) { RecipeDetailContent(recipe) }
        }
        composeRule.onNodeWithText("Step 1").performScrollTo().performClick()
        composeRule.onNodeWithText("Step 1").assertIsOn()
        restoration.emulateSavedInstanceStateRestore()
        composeRule.onNodeWithText("Step 1").performScrollTo().assertIsOn().performClick().assertIsOff()
        composeRule.onNodeWithText("0 of 2 steps completed").performScrollTo().assertExists()
    }

    @Test
    fun emptySearchProvidesAClearAction() {
        var requestedQuery: String? = null
        composeRule.setContent {
            SurfingComposeTheme(darkTheme = false) {
                RecipesContent(
                    recipes = emptyList(), query = "no-match", isLoading = false, error = "",
                    onSearch = { requestedQuery = it }, onRetry = {}, onRecipeClick = {}
                )
            }
        }
        composeRule.onNodeWithText("No recipes found").assertExists()
        composeRule.onNodeWithContentDescription("Clear search").performClick()
        composeRule.runOnIdle { assertEquals("", requestedQuery) }
    }

    @Test
    fun shimmerIsReplacedByRecipesAfterLoading() {
        val loading = mutableStateOf(true)
        composeRule.setContent {
            SurfingComposeTheme {
                RecipesContent(
                    recipes = if (loading.value) emptyList() else listOf(recipe),
                    query = "", isLoading = loading.value, error = "",
                    onSearch = {}, onRetry = {}, onRecipeClick = {}
                )
            }
        }
        composeRule.onNodeWithContentDescription("Loading recipes").assertExists()
        composeRule.onNodeWithText("No recipes found").assertDoesNotExist()
        composeRule.runOnIdle { loading.value = false }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(recipe.name).performScrollTo().assertExists()
        composeRule.onNodeWithContentDescription("Loading recipes").assertDoesNotExist()
    }

    private val recipe = RecipeModel(
        id = 42, name = "Creamy tomato pasta",
        description = "A comforting bowl with tomatoes, fresh basil, and a silky sauce.",
        thumbnailUrl = "", originalVideoUrl = "",
        nutrition = NutritionModel(fat = 17, calories = 420, sugar = 8, carbohydrates = 54, fiber = 6, protein = 16),
        instructions = listOf(
            InstructionsModel("Cook the pasta in salted water until al dente.", 1),
            InstructionsModel("Simmer the tomatoes, then toss with pasta and basil.", 2)
        )
    )
}
