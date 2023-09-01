package com.hb.surfingcompose.data.datasource.local

import com.hb.surfingcompose.data.mapper.toEntity
import com.hb.surfingcompose.data.mapper.toResponse
import com.hb.surfingcompose.data.responses.RecipeResponse
import javax.inject.Inject


class LocalDataSourceImpl @Inject constructor(private val recipesDao: RecipesDao) : LocalDataSource {

    override suspend fun saveRecipes(list: List<RecipeResponse>) = recipesDao.insertRecipes(list.toEntity())

    override suspend fun getRecipes(): List<RecipeResponse> = recipesDao.queryAllRecipes().map { it.toResponse() }
}