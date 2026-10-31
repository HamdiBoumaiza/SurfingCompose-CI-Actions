package com.hb.surfingcompose.data.datasource.local

import androidx.room.Dao
import androidx.room.Query
import com.hb.surfingcompose.data.datasource.local.entities.RecipeEntity


@Dao
interface RecipesDao {

    @androidx.room.Upsert
    suspend fun insertRecipes(list: List<RecipeEntity>)

    @Query("SELECT * FROM recipe WHERE id = :id")
    suspend fun recipeById(id: Int): RecipeEntity?

    @Query("SELECT * FROM recipe ORDER BY id")
    fun queryAllRecipes(): List<RecipeEntity>
}