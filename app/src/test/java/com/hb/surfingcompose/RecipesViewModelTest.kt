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
    fun initialLoadAndUnchangedQueryOnlyFetchOnce() = runTest {
        val repository = FakeRepository()
        val viewModel = RecipesViewModel(GetRecipesUseCase(repository), SavedStateHandle())
        advanceUntilIdle()
        viewModel.searchRecipes("")
        advanceUntilIdle()
        assertEquals(listOf(""), repository.requests)
        assertFalse(viewModel.isLoading.value)
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
        assertEquals("new", viewModel.recipeList.value.single().name)
        assertFalse(viewModel.isLoading.value)
        assertEquals("", viewModel.errorMessage.value)
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
        assertEquals("new", viewModel.recipeList.value.single().name)
    }

    @Test
    fun retryClearsErrorShowsLoadingAndKeepsSearchQuery() = runTest {
        val repository = FakeRepository()
        val viewModel = RecipesViewModel(GetRecipesUseCase(repository), SavedStateHandle())
        advanceUntilIdle()
        repository.response = { flowOf(SurfaceResult.Error(Exception("Network failed"))) }
        viewModel.searchRecipes("soup")
        advanceUntilIdle()
        assertEquals("Network failed", viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)

        repository.response = { flow { delay(100); emit(SurfaceResult.Success(listOf(recipe("soup")))) } }
        viewModel.getRecipesFlow()
        assertTrue(viewModel.isLoading.value)
        assertEquals("", viewModel.errorMessage.value)
        advanceUntilIdle()
        assertEquals("soup", repository.requests.last())
        assertEquals("soup", viewModel.recipeList.value.single().name)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun unexpectedFailureBecomesRetryableError() = runTest {
        val repository = FakeRepository().apply { response = { throw IllegalStateException("Read failed") } }
        val viewModel = RecipesViewModel(GetRecipesUseCase(repository), SavedStateHandle())
        advanceUntilIdle()
        assertEquals("Read failed", viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun selectedRecipeAndQueryAreRestoredFromSavedState() = runTest {
        val repository = FakeRepository()
        val state = SavedStateHandle()
        val original = RecipesViewModel(GetRecipesUseCase(repository), state)
        original.setRecipe(recipe("soup"))
        original.searchRecipes("soup")
        advanceUntilIdle()

        val restoredState = SavedStateHandle(state.keys().associateWith { state.get<Any?>(it) })
        val restored = RecipesViewModel(GetRecipesUseCase(repository), restoredState)
        advanceUntilIdle()
        assertEquals(original.getRecipe(), restored.getRecipe())
        assertEquals("soup", restored.searchQuery.value)
        assertEquals("soup", repository.requests.last())
    }

    private fun recipe(name: String) = RecipeModel(
        id = 1, name = name, nutrition = NutritionModel(), description = "", originalVideoUrl = "", thumbnailUrl = ""
    )
}
