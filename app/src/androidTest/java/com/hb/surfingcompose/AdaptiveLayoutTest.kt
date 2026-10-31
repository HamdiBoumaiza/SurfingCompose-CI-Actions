package com.hb.surfingcompose

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hb.surfingcompose.data.SurfaceResult
import com.hb.surfingcompose.data.responses.RecipeResponse
import com.hb.surfingcompose.domain.models.*
import com.hb.surfingcompose.domain.repository.RecipesRepository
import com.hb.surfingcompose.domain.request.RecipesRequest
import com.hb.surfingcompose.domain.usecase.GetRecipesUseCase
import com.hb.surfingcompose.presentation.activities.RecipesNavigation
import com.hb.surfingcompose.presentation.theme.SurfingComposeTheme
import com.hb.surfingcompose.presentation.viewmodel.RecipesViewModel
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AdaptiveLayoutTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    @Test fun browsingAndCookingAdaptToAvailableWidth() {
        val recipes = listOf("Tomato & basil soup", "Avocado chicken salad", "Lemon pasta", "Roasted vegetable bowl").mapIndexed { index, name ->
            RecipeModel(index + 1, name, nutrition = NutritionModel(), description = "A simple meal to enjoy at home.", originalVideoUrl = "", thumbnailUrl = "",
                prepTimeMinutes = 10 + index * 5, servings = 4,
                instructions = listOf(InstructionsModel("Chop the vegetables, then warm the olive oil in a large pan. Add the vegetables and stir gently until soft. Add the stock and simmer until the vegetables are tender. Blend carefully, taste and adjust the seasoning, then serve with fresh basil and crusty bread.", 1), InstructionsModel("Serve and enjoy.", 2)))
        }
        val repository = object : RecipesRepository {
            override suspend fun saveRecipes(list: List<RecipeResponse>) = Unit
            override suspend fun getRecipe(id: Int) = recipes.firstOrNull { it.id == id }
            override suspend fun getRecipes(recipesRequest: RecipesRequest) = flowOf(SurfaceResult.Success(recipes))
        }
        var expanded = false
        rule.setContent {
            expanded = LocalConfiguration.current.screenWidthDp >= 840
            val vm = remember { RecipesViewModel(GetRecipesUseCase(repository), SavedStateHandle()) }
            val density = LocalDensity.current
            val fontScale = if (InstrumentationRegistry.getArguments().getString("largeFonts") == "true") 2f else density.fontScale
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                SurfingComposeTheme { RecipesNavigation(rememberNavController(), vm) }
            }
        }
        rule.onNodeWithTag("recipe_card_1").assertIsDisplayed()
        if (expanded) rule.onNodeWithTag("detail_pane").assertIsDisplayed()
        capture("browse")
        rule.onNodeWithTag("recipe_card_1").performClick()
        rule.onNodeWithTag("recipe_detail").assertIsDisplayed()
        if (expanded) {
            rule.onNodeWithTag("recipe_grid").assertIsDisplayed()
            rule.onNodeWithTag("recipe_card_1").assertIsSelected()
        }
        capture("detail")
        rule.onNodeWithText("Start cooking").performScrollTo().performClick()
        rule.onNodeWithText("Complete and next").assertIsDisplayed()
        rule.onNodeWithText("Timer settings").performClick()
        rule.onNodeWithText("Start timer").performClick()
        rule.onNodeWithText("Pause").assertIsDisplayed()
        rule.onNodeWithText("Complete and next").assertIsDisplayed()
        capture("cooking")
    }
    private fun capture(name: String) {
        rule.waitForIdle()
        val bitmap = rule.onRoot().captureToImage().asAndroidBitmap()
        File(rule.activity.cacheDir, "review-$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
