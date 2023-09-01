package com.hb.surfingcompose.data.mapper

import com.hb.surfingcompose.data.datasource.local.entities.InstructionsEntity
import com.hb.surfingcompose.data.datasource.local.entities.NutritionEntity
import com.hb.surfingcompose.data.datasource.local.entities.RecipeEntity
import com.hb.surfingcompose.data.datasource.local.entities.TagsEntity
import com.hb.surfingcompose.data.responses.InstructionsResponse
import com.hb.surfingcompose.data.responses.NutritionResponse
import com.hb.surfingcompose.data.responses.RecipeResponse
import com.hb.surfingcompose.data.responses.TagsResponse
import kotlin.random.Random


private fun NutritionResponse.toEntity() =
    NutritionEntity(id = Random.nextInt(), fat ?: 0, calories ?: 0, sugar ?: 0, carbohydrates ?: 0, fiber ?: 0, protein ?: 0)

private fun InstructionsResponse.toEntity() = InstructionsEntity(position ?: 0, displayText ?: "")

private fun TagsResponse.toEntity() = TagsEntity(displayName ?: "", type ?: "")

fun List<RecipeResponse>.toEntity(): List<RecipeEntity> {
    return map {
        RecipeEntity(
            it.id ?: 0,
            it.name ?: "",
            it.instructions.map { instruction -> instruction.toEntity() },
            it.tags.map { tag -> tag.toEntity() },
            it.nutrition?.toEntity() ?: NutritionEntity(),
            it.description ?: "",
            it.originalVideoUrl ?: "",
            it.thumbnailUrl ?: ""
        )
    }

}
