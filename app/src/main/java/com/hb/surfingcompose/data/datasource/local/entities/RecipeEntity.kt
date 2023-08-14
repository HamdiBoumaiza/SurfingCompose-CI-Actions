package com.hb.surfingcompose.data.datasource.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "recipe")
data class RecipeEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val instructions: List<InstructionsEntity>,
    val tags: List<TagsEntity>,
    val nutrition: NutritionEntity,
    val description: String,
    val originalVideoUrl: String,
    val thumbnailUrl: String
)