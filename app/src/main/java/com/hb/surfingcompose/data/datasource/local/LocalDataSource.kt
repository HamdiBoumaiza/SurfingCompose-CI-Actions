package com.hb.surfingcompose.data.datasource.local

import com.hb.surfingcompose.data.responses.RecipeResponse


interface LocalDataSource {

    suspend fun saveRecipes(list: List<RecipeResponse>)

    suspend fun getRecipes(): List<RecipeResponse>

}