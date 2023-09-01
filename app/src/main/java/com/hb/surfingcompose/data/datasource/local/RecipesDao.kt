package com.hb.surfingcompose.data.datasource.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hb.surfingcompose.data.datasource.local.entities.RecipeEntity


@Dao
interface RecipesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipes(list: List<RecipeEntity>)

    @Query("SELECT * FROM recipe")
    fun queryAllRecipes(): List<RecipeEntity>
}