package com.hb.surfingcompose.data.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hb.surfingcompose.data.datasource.local.entities.InstructionsEntity
import com.hb.surfingcompose.data.datasource.local.entities.NutritionEntity
import com.hb.surfingcompose.data.datasource.local.entities.RecipeEntity
import com.hb.surfingcompose.data.datasource.local.entities.TagsEntity

@Database(entities = [RecipeEntity::class, InstructionsEntity::class, TagsEntity::class, NutritionEntity::class], version = 1)
@TypeConverters(DbConverters::class)
abstract class RecipesDatabase : RoomDatabase() {
    abstract fun recipesDao(): RecipesDao
}