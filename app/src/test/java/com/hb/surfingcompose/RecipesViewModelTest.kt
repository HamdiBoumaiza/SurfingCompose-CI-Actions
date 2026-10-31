package com.hb.surfingcompose

import androidx.lifecycle.SavedStateHandle
import com.hb.surfingcompose.data.SurfaceResult
import com.hb.surfingcompose.data.responses.RecipeResponse
import com.hb.surfingcompose.domain.models.NutritionModel
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.domain.repository.RecipesRepository
import com.hb.surfingcompose.domain.request.RecipesRequest
import com.hb.surfingcompose.domain.usecase.GetRecipesUseCase
import com.hb.surfingcompose.presentation.viewmodel.RecipesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecipesViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private class FakeRepository : RecipesRepository {
        val requests = mutableListOf<String?>()
        val cached = mutableMapOf<Int, RecipeModel>()
        override suspend fun getRecipe(id: Int) = cached[id]
        var response: suspend (RecipesRequest) -> Flow<SurfaceResult<List<RecipeModel>>> = {
            flowOf(SurfaceResult.Success(emptyList()))
        }

        override suspend fun saveRecipes(list: List<RecipeResponse>) = Unit
        override suspend fun getRecipes(recipesRequest: RecipesRequest): Flow<SurfaceResult<List<RecipeModel>>> {
            requests += recipesRequest.nameOfFood
            return response(recipesRequest)
        }
    }

    @Test
    fun tagFilterSurvivesRecreationAndClearingPreservesSearch() = runTest {
        val requests = mutableListOf<RecipesRequest>()
        val repository = FakeRepository().apply {
            response = { requests += it; flowOf(SurfaceResult.Success(emptyList())) }
        }
        val state = SavedStateHandle()
        val viewModel = RecipesViewModel(GetRecipesUseCase(repository), state)
        viewModel.searchRecipes("soup")
        val tag = com.hb.surfingcompose.domain.models.TagsModel("Vegetarian", "diet", "vegetarian")
        viewModel.filterByTag(tag)
        advanceUntilIdle()
        assertEquals(tag, requests.last().tag)
        assertEquals("soup", requests.last().nameOfFood)
        val restored = RecipesViewModel(GetRecipesUseCase(repository), state)
        advanceUntilIdle()
        assertEquals(tag, restored.uiState.value.selectedTag)
        restored.filterByTag(null)
        advanceUntilIdle()
        assertNull(requests.last().tag)
        assertEquals("soup", requests.last().nameOfFood)
    }

    @Test
    fun initialLoadAndUnchangedQueryOnlyFetchOnce() = runTest {
        val repository = FakeRepository()
        val viewModel = RecipesViewModel(GetRecipesUseCase(repository), SavedStateHandle())
        advanceUntilIdle()
        viewModel.searchRecipes("")
        advanceUntilIdle()
        assertEquals(listOf(""), repository.requests)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun newerSearchCancelsPreviousRequest() = runTest {
        val repository = FakeRepository()
        val viewModel = RecipesViewModel(GetRecipesUseCase(repository), SavedStateHandle())
        advanceUntilIdle()
        var cancelled = false
        repository.response = { request ->
            flow {
                try {
                    delay(if (request.nameOfFood == "old") 1000 else 100)
                    emit(SurfaceResult.Success(listOf(recipe(request.nameOfFood.orEmpty()))))
                } finally {
                    if (request.nameOfFood == "old") cancelled = true
                }
            }
        }
        viewModel.searchRecipes("old")
        runCurrent()
        viewModel.searchRecipes("new")
        advanceUntilIdle()
        assertTrue(cancelled)
        assertEquals("new", viewModel.uiState.value.recipes.single().name)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("", viewModel.uiState.value.error)
    }

    @Test
    fun staleResponseCannotOverwriteNewerResults() = runTest {
        val repository = FakeRepository()
        val viewModel = RecipesViewModel(GetRecipesUseCase(repository), SavedStateHandle())
        advanceUntilIdle()
        repository.response = { request ->
            // Simulate a dependency that completes work despite cancellation.
            if (request.nameOfFood == "old") withContext(NonCancellable) { delay(1000) }
            flowOf(SurfaceResult.Success(listOf(recipe(request.nameOfFood.orEmpty()))))
        }
        viewModel.searchRecipes("old")
        runCurrent()
        viewModel.searchRecipes("new")
        advanceUntilIdle()
        assertEquals("new", viewModel.uiState.value.recipes.single().name)
    }

    @Test
    fun retryClearsErrorShowsLoadingAndKeepsSearchQuery() = runTest {
        val repository = FakeRepository()
        val viewModel = RecipesViewModel(GetRecipesUseCase(repository), SavedStateHandle())
        advanceUntilIdle()
        repository.response = { flowOf(SurfaceResult.Error(Exception("Network failed"))) }
        viewModel.searchRecipes("soup")
        advanceUntilIdle()
        assertEquals("Network failed", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)

        repository.response = { flow { delay(100); emit(SurfaceResult.Success(listOf(recipe("soup")))) } }
        viewModel.getRecipesFlow()
        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals("", viewModel.uiState.value.error)
        advanceUntilIdle()
        assertEquals("soup", repository.requests.last())
        assertEquals("soup", viewModel.uiState.value.recipes.single().name)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun unexpectedFailureBecomesRetryableError() = runTest {
        val repository = FakeRepository().apply { response = { throw IllegalStateException("Read failed") } }
        val viewModel = RecipesViewModel(GetRecipesUseCase(repository), SavedStateHandle())
        advanceUntilIdle()
        assertEquals("Read failed", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun selectedRecipeAndQueryAreRestoredFromSavedState() = runTest {
        val repository = FakeRepository()
        val state = SavedStateHandle()
        val original = RecipesViewModel(GetRecipesUseCase(repository), state)
        repository.cached[1] = recipe("soup")
        original.setRecipe(recipe("soup"))
        original.searchRecipes("soup")
        advanceUntilIdle()

        val restoredState = SavedStateHandle(state.keys().associateWith { state.get<Any?>(it) })
        val restored = RecipesViewModel(GetRecipesUseCase(repository), restoredState)
        advanceUntilIdle()
        assertEquals(original.getRecipe(), restored.getRecipe())
        assertEquals(1, restoredState.get<Int>("selected_recipe_id"))
        assertFalse(restoredState.contains("selected_recipe"))
        assertEquals("soup", restored.uiState.value.query)
        assertEquals("soup", repository.requests.last())
    }

    private fun recipe(name: String) = RecipeModel(
        id = 1, name = name, nutrition = NutritionModel(), description = "", originalVideoUrl = "", thumbnailUrl = ""
    )
}
