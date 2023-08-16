package com.hb.surfingcompose.data.datasource.remote

import com.hb.surfingcompose.data.SurfaceResult
import com.hb.surfingcompose.data.responses.RecipeResponse
import com.hb.surfingcompose.data.responses.RecipesListResponse
import com.hb.surfingcompose.domain.request.RecipesRequest
import kotlinx.coroutines.flow.Flow


interface RemoteDataSource {

    suspend fun getRecipes(recipesRequest: RecipesRequest): SurfaceResult<List<RecipeResponse>>

}