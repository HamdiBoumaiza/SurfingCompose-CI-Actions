package com.hb.surfingcompose.domain.repository

import com.hb.surfingcompose.data.SurfaceResult
import com.hb.surfingcompose.data.responses.RecipeResponse
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.domain.request.RecipesRequest
import kotlinx.coroutines.flow.Flow

interface RecipesRepository {

    suspend fun saveRecipes(list: List<RecipeResponse>)

    suspend fun getRecipes(recipesRequest: RecipesRequest): Flow<SurfaceResult<List<RecipeModel>>>

    suspend fun getRecipe(id: Int): Flow<RecipeModel>
}