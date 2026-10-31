package com.hb.surfingcompose.presentation.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hb.surfingcompose.data.SurfaceResult
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.domain.request.RecipesRequest
import com.hb.surfingcompose.domain.usecase.GetRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val getRecipesUseCase: GetRecipesUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val recipeList = mutableStateOf<List<RecipeModel>>(emptyList())
    val errorMessage = mutableStateOf("")
    val isLoading = mutableStateOf(true)

    val searchQuery = mutableStateOf(savedStateHandle.get<String>(SEARCH_QUERY).orEmpty())
    private val currentRecipe = mutableStateOf(savedStateHandle.get<RecipeModel>(SELECTED_RECIPE))
    private var requestJob: Job? = null
    private var latestRequestId = 0L

    init {
        getRecipesFlow()
    }

    fun searchRecipes(query: String) {
        if (query == searchQuery.value) return
        searchQuery.value = query
        savedStateHandle[SEARCH_QUERY] = query
        getRecipesFlow(RecipesRequest(query))
    }

    fun getRecipesFlow(recipesRequest: RecipesRequest = RecipesRequest(searchQuery.value)) {
        requestJob?.cancel()
        val requestId = ++latestRequestId
        isLoading.value = true
        errorMessage.value = ""
        requestJob = viewModelScope.launch {
            try {
                getRecipesUseCase(recipesRequest).collect { result ->
                    if (requestId != latestRequestId) return@collect
                    when (result) {
                        is SurfaceResult.Success -> {
                            recipeList.value = result.data
                            errorMessage.value = ""
                            isLoading.value = false
                        }
                        is SurfaceResult.Error -> {
                            errorMessage.value = result.exception.message ?: "Unable to load recipes"
                            isLoading.value = false
                        }
                        SurfaceResult.Loading -> isLoading.value = true
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                if (requestId == latestRequestId) {
                    errorMessage.value = exception.message ?: "Unable to load recipes"
                }
            } finally {
                if (requestId == latestRequestId) isLoading.value = false
            }
        }
    }

    fun setRecipe(recipe: RecipeModel) {
        savedStateHandle[SELECTED_RECIPE] = recipe
        currentRecipe.value = recipe
    }

    fun getRecipe() = currentRecipe.value

    private companion object {
        const val SELECTED_RECIPE = "selected_recipe"
        const val SEARCH_QUERY = "search_query"
    }
}
