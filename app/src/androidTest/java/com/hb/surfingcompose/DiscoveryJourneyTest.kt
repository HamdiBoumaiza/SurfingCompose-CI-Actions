package com.hb.surfingcompose

import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hb.surfingcompose.data.SurfaceResult
import com.hb.surfingcompose.data.responses.RecipeResponse
import com.hb.surfingcompose.domain.library.MemoryRecipeLibrary
import com.hb.surfingcompose.domain.models.*
import com.hb.surfingcompose.domain.repository.RecipesRepository
import com.hb.surfingcompose.domain.request.RecipesRequest
import com.hb.surfingcompose.domain.usecase.GetRecipesUseCase
import com.hb.surfingcompose.presentation.activities.RecipesNavigation
import com.hb.surfingcompose.presentation.theme.SurfingComposeTheme
import com.hb.surfingcompose.presentation.viewmodel.RecipesViewModel
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiscoveryJourneyTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val soup = RecipeModel(1, "Tomato soup", nutrition = NutritionModel(), description = "Warm and simple.", originalVideoUrl = "", thumbnailUrl = "",
        instructions = listOf(InstructionsModel("Chop the tomatoes.", 1), InstructionsModel("Simmer and serve.", 2)), prepTimeMinutes = 10, servings = 4)

    @Test fun paginationAndDiscoverFiltersUseNewPages() {
        val requests = mutableListOf<RecipesRequest>()
        val repository = object : RecipesRepository {
            override suspend fun saveRecipes(list: List<RecipeResponse>) = Unit
            override suspend fun getRecipes(recipesRequest: RecipesRequest) = flowOf(SurfaceResult.Success(
                if (recipesRequest.from == 0) (0..29).map { soup.copy(id = it, name = "Recipe $it") }
                else listOf(soup.copy(id = 30, name = "Next page recipe"))
            )).also { requests += recipesRequest }
        }
        rule.setContent {
            val vm = remember { RecipesViewModel(GetRecipesUseCase(repository), SavedStateHandle()) }
            SurfingComposeTheme { RecipesNavigation(rememberNavController(), vm) }
        }
        rule.onAllNodesWithText("10 min prep · Serves 4")[0].assertExists() // first visible card only
        rule.onNodeWithTag("recipe_grid").performScrollToNode(hasText("Load more recipes"))
        rule.onNodeWithText("Load more recipes").performClick()
        rule.onNodeWithTag("recipe_grid").performScrollToNode(hasText("Next page recipe"))
        rule.onNodeWithText("Next page recipe").assertIsDisplayed()
        rule.runOnIdle { assertEquals(listOf(0, 30), requests.map { it.from }) }
        rule.onNodeWithTag("recipe_grid").performScrollToIndex(0)
        rule.onNodeWithText("Diet").performClick()
        rule.onNodeWithText("Vegan").performClick()
        rule.onNodeWithText("Vegan").assertIsDisplayed()
        rule.runOnIdle { assertEquals(0, requests.last().from); assertEquals("vegan", requests.last().filterTags.single().name) }
    }

    @Test fun newRenameCollectionAndFavoriteUndo() {
        lateinit var vm: RecipesViewModel
        val repository = object : RecipesRepository {
            override suspend fun saveRecipes(list: List<RecipeResponse>) = Unit
            override suspend fun getRecipes(recipesRequest: RecipesRequest) = flowOf(SurfaceResult.Success(listOf(soup)))
        }
        rule.setContent {
            vm = remember { RecipesViewModel(GetRecipesUseCase(repository), SavedStateHandle(), MemoryRecipeLibrary()) }
            SurfingComposeTheme { RecipesNavigation(rememberNavController(), vm) }
        }
        rule.onNodeWithContentDescription("Save to favorites").performScrollTo().performClick()
        rule.onNodeWithText("My recipes").performClick()
        rule.onNodeWithText("New collection").performClick()
        rule.onNodeWithText("Collection name").performTextInput("Dinners")
        rule.onNodeWithText("Save").performClick()
        rule.onNodeWithText("Rename collection").performClick()
        rule.onNodeWithText("Collection name").performTextReplacement("Weeknight dinners")
        rule.onNodeWithText("Save").performClick()
        rule.onNodeWithText("Weeknight dinners").assertExists()
        rule.onNodeWithText("All favorites").performClick()
        rule.onNodeWithContentDescription("Remove from favorites").performScrollTo().performClick()
        rule.onNodeWithText("Undo").performClick()
        rule.onNodeWithText(soup.name).performScrollTo().assertExists()
        rule.runOnIdle { assertEquals(listOf(soup), vm.uiState.value.library.recipes) }
    }

    @Test fun detailAndCookingShareCheckmarks() {
        lateinit var vm: RecipesViewModel
        lateinit var navigation: NavHostController
        val repository = object : RecipesRepository {
            override suspend fun saveRecipes(list: List<RecipeResponse>) = Unit
            override suspend fun getRecipe(id: Int) = soup
            override suspend fun getRecipes(recipesRequest: RecipesRequest) = flowOf(SurfaceResult.Success(listOf(soup)))
        }
        rule.setContent {
            vm = remember { RecipesViewModel(GetRecipesUseCase(repository), SavedStateHandle()) }
            navigation = rememberNavController()
            SurfingComposeTheme { RecipesNavigation(navigation, vm) }
        }
        rule.onNodeWithText(soup.name).performScrollTo().performClick()
        rule.onNodeWithText("Step 1").performScrollTo().performClick()
        rule.onNodeWithText("Start cooking").performScrollTo().performClick()
        rule.onNodeWithText("Step 2 of 2").assertExists()
        rule.onNodeWithText("Finish").assertIsDisplayed().performClick()
        rule.onNodeWithText("All done!").assertExists()
        rule.onNodeWithText("Done").performClick()
        rule.onNodeWithText("2 of 2 steps completed").performScrollTo().assertExists()
        rule.runOnIdle { assertEquals(setOf(1, 2), vm.uiState.value.completedSteps) }
    }
}
