package com.hb.surfingcompose

import androidx.lifecycle.SavedStateHandle
import com.hb.surfingcompose.data.SurfaceResult
import com.hb.surfingcompose.data.responses.RecipeResponse
import com.hb.surfingcompose.domain.library.MemoryRecipeLibrary
import com.hb.surfingcompose.domain.models.*
import com.hb.surfingcompose.domain.repository.RecipesRepository
import com.hb.surfingcompose.domain.request.RecipesRequest
import com.hb.surfingcompose.domain.usecase.GetRecipesUseCase
import com.hb.surfingcompose.presentation.viewmodel.RecipesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaginationTest {
    @get:Rule val dispatcher = MainDispatcherRule()
    private class Repository : RecipesRepository {
        val requests = mutableListOf<RecipesRequest>()
        var failNext = false
        var waitMs = 0L
        var page: (RecipesRequest) -> List<RecipeModel> = { request -> (request.from until request.from + request.size).map(::recipe) }
        override suspend fun saveRecipes(list: List<RecipeResponse>) = Unit
        override suspend fun getRecipe(id: Int) = recipe(id)
        override suspend fun getRecipes(recipesRequest: RecipesRequest) = flow {
            requests += recipesRequest
            delay(waitMs)
            if (failNext) { failNext = false; emit(SurfaceResult.Error(Exception("Page failed"))) }
            else emit(SurfaceResult.Success(page(recipesRequest)))
        }
    }
    @Test fun appendDeduplicatesUsesRawOffsetAndStopsAtShortPage() = runTest {
        val repo = Repository()
        repo.page = { r -> when(r.from) { 0 -> (0..29).map(::recipe); 30 -> (29..58).map(::recipe); else -> listOf(recipe(59)) } }
        val vm = RecipesViewModel(GetRecipesUseCase(repo), SavedStateHandle())
        advanceUntilIdle()
        assertTrue(vm.uiState.value.hasMore)
        vm.loadMore(); advanceUntilIdle()
        assertEquals(59, vm.uiState.value.recipes.size)
        vm.loadMore(); advanceUntilIdle()
        assertEquals(listOf(0, 30, 60), repo.requests.map { it.from })
        assertEquals(60, vm.uiState.value.recipes.size)
        assertFalse(vm.uiState.value.hasMore)
        vm.loadMore(); advanceUntilIdle()
        assertEquals(3, repo.requests.size)
    }
    @Test fun appendErrorKeepsRecipesAndRetriesSameOffsetWithoutDuplicateRequests() = runTest {
        val repo = Repository()
        val vm = RecipesViewModel(GetRecipesUseCase(repo), SavedStateHandle())
        advanceUntilIdle()
        repo.failNext = true
        vm.loadMore(); advanceUntilIdle()
        assertEquals(30, vm.uiState.value.recipes.size)
        assertEquals("Page failed", vm.uiState.value.appendError)
        assertEquals("", vm.uiState.value.error)
        repo.waitMs = 100
        vm.loadMore(); vm.loadMore(); advanceUntilIdle()
        assertEquals(listOf(0, 30, 30), repo.requests.map { it.from })
        assertEquals(60, vm.uiState.value.recipes.size)
        assertEquals("", vm.uiState.value.appendError)
    }
    @Test fun searchAndCombinedFiltersCancelAppendAndResetOffset() = runTest {
        val repo = Repository()
        val vm = RecipesViewModel(GetRecipesUseCase(repo), SavedStateHandle())
        advanceUntilIdle()
        repo.waitMs = 1000
        vm.loadMore(); runCurrent()
        vm.searchRecipes("soup"); advanceUntilIdle()
        assertEquals(0, repo.requests.last().from)
        assertEquals(30, vm.uiState.value.recipes.size)
        vm.setFilter("diet", TagsModel("Vegan", "diet", "vegan")); advanceUntilIdle()
        vm.setFilter("time", TagsModel("Under 30 minutes", "time", "under_30_minutes")); advanceUntilIdle()
        vm.loadMore(); advanceUntilIdle()
        assertEquals(30, repo.requests.last().from)
        assertEquals("soup", repo.requests.last().nameOfFood)
        assertEquals(setOf("vegan", "under_30_minutes"), repo.requests.last().filterTags.map { it.name }.toSet())
        vm.clearFilters(); advanceUntilIdle()
        assertEquals(0, repo.requests.last().from)
        assertTrue(repo.requests.last().filterTags.isEmpty())
    }
    @Test fun recipeIdAndSharedProgressReloadWithoutSavingRecipeInBundle() = runTest {
        val repo = Repository()
        val saved = SavedStateHandle()
        val vm = RecipesViewModel(GetRecipesUseCase(repo), saved)
        vm.setRecipe(recipe(99))
        vm.setCompletedSteps(99, setOf(1, 3))
        val restored = RecipesViewModel(GetRecipesUseCase(repo), SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) }))
        advanceUntilIdle()
        assertEquals(recipe(99), restored.uiState.value.selectedRecipe)
        assertEquals(setOf(1, 3), restored.uiState.value.completedSteps)
        assertEquals(99, saved.get<Int>("selected_recipe_id"))
        assertFalse(saved.keys().any { saved.get<Any?>(it) is RecipeModel })
    }
    @Test fun undoRestoresCollectionMemberships() = runTest {
        val library = MemoryRecipeLibrary()
        val id = library.createCollection("Dinners")!!
        library.setMembership(recipe(1), id, true)
        val vm = RecipesViewModel(GetRecipesUseCase(Repository()), SavedStateHandle(), library)
        vm.toggleFavorite(recipe(1)); advanceUntilIdle()
        assertTrue(vm.uiState.value.library.recipes.isEmpty())
        vm.undoFavorite(vm.uiState.value.undoFavorite!!.token); advanceUntilIdle()
        assertEquals(setOf(1), vm.uiState.value.library.collections.single().recipeIds)
        assertEquals(recipe(1), vm.uiState.value.library.recipes.single())
    }
    companion object {
        private fun recipe(id: Int) = RecipeModel(id, "Recipe $id", nutrition = NutritionModel(), description = "", originalVideoUrl = "", thumbnailUrl = "")
    }
}
