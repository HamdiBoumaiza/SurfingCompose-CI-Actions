package com.hb.surfingcompose.data.datasource.local

import com.hb.surfingcompose.data.responses.RecipeResponse
import kotlinx.coroutines.flow.Flow


interface LocalDataSource {

    suspend fun saveRecipes(list: List<RecipeResponse>)

    suspend fun getRecipes(): Flow<List<RecipeResponse>>

    suspend fun getRecipe(id: Int): Flow<RecipeResponse>


}