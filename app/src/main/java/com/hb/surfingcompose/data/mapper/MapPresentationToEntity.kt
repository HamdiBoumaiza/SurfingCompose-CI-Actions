package com.hb.surfingcompose.data.mapper

import com.hb.surfingcompose.data.datasource.local.entities.InstructionsEntity
import com.hb.surfingcompose.data.datasource.local.entities.NutritionEntity
import com.hb.surfingcompose.data.datasource.local.entities.RecipeEntity
import com.hb.surfingcompose.data.datasource.local.entities.TagsEntity
import com.hb.surfingcompose.data.responses.InstructionsResponse
import com.hb.surfingcompose.data.responses.NutritionResponse
import com.hb.surfingcompose.data.responses.RecipeResponse
import com.hb.surfingcompose.data.responses.TagsResponse


private fun List<TagsEntity>.toTagsResponse(): List<TagsResponse> {
    return map { TagsResponse(displayName = it.displayName, type = it.type) }
}

private fun List<InstructionsEntity>.toInstructionResponse(): List<InstructionsResponse> {
    return map { InstructionsResponse(displayText = it.displayText) }
}

private fun NutritionEntity.toNutritionResponse(): NutritionResponse {
    return NutritionResponse(fat = fat, calories = calories, sugar = sugar, carbohydrates = carbohydrates, fiber = fiber, protein = protein)
}

fun RecipeEntity.toResponse(): RecipeResponse {
    return RecipeResponse(
        id = id,
        name = name,
        instructions = instructions.toInstructionResponse(),
        tags = tags.toTagsResponse(),
        nutrition = nutrition.toNutritionResponse(),
        description = description,
        originalVideoUrl = originalVideoUrl,
        thumbnailUrl = thumbnailUrl
    )
}

fun List<RecipeEntity>.toResponse(): List<RecipeResponse> = map { it.toResponse() }
