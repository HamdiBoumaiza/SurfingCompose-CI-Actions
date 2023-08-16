package com.hb.surfingcompose.viewmodel

import androidx.lifecycle.ViewModel
import com.hb.surfingcompose.data.SurfaceResult
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.domain.request.RecipesRequest
import com.hb.surfingcompose.domain.usecase.GetRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val getRecipesUseCase: GetRecipesUseCase,
) : ViewModel() {

    suspend fun getRecipesFlow(recipesRequest: RecipesRequest): Flow<SurfaceResult<List<RecipeModel>>> = getRecipesUseCase.invoke(recipesRequest)

}