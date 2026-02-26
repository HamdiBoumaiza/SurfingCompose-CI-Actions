package com.hb.surfingcompose.presentation.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hb.surfingcompose.data.onError
import com.hb.surfingcompose.data.onLoading
import com.hb.surfingcompose.data.onSuccess
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.domain.request.RecipesRequest
import com.hb.surfingcompose.domain.usecase.GetRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val getRecipesUseCase: GetRecipesUseCase,
) : ViewModel() {

    val recipeList = mutableStateOf<List<RecipeModel>>(emptyList())
    val errorMessage = mutableStateOf("")
    val isLoading = mutableStateOf(true)

    private val currentRecipe = mutableStateOf<RecipeModel?>(null)

    fun getRecipesFlow(recipesRequest: RecipesRequest = RecipesRequest()) {
        viewModelScope.launch(Dispatchers.Default) {
            getRecipesUseCase.invoke(recipesRequest).collect { result ->
                result.onSuccess {
                    isLoading.value = false
                    recipeList.value = it
                }
                result.onError {
                    isLoading.value = false
                    errorMessage.value = it.message.toString()
                }
                result.onLoading { isLoading.value = true }

            }


        }
    }

    fun setRecipe(recipe: RecipeModel) {
        currentRecipe.value = recipe
    }

    fun getRecipe() = currentRecipe.value

}