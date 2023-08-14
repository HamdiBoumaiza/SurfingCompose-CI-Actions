package com.hb.surfingcompose.domain.mapper

import com.hb.surfingcompose.data.responses.InstructionsResponse
import com.hb.surfingcompose.data.responses.NutritionResponse
import com.hb.surfingcompose.data.responses.RecipeResponse
import com.hb.surfingcompose.data.responses.RecipesListResponse
import com.hb.surfingcompose.data.responses.TagsResponse
import com.hb.surfingcompose.domain.models.InstructionsModel
import com.hb.surfingcompose.domain.models.NutritionModel
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.domain.models.RecipesListModel
import com.hb.surfingcompose.domain.models.TagsModel


internal fun NutritionResponse.toPresentation() = NutritionModel(fat ?: 0, calories ?: 0, sugar ?: 0, carbohydrates ?: 0, fiber ?: 0, protein ?: 0)

internal fun InstructionsResponse.toPresentation() = InstructionsModel(displayText ?: "")

internal fun TagsResponse.toPresentation() = TagsModel(displayName ?: "", type ?: "")

internal fun RecipeResponse.toPresentation() = RecipeModel(
    id ?: 0,
    name ?: "",
    instructions.map { it.toPresentation() },
    tags.map { it.toPresentation() },
    nutrition?.toPresentation() ?: NutritionModel(),
    description ?: "",
    originalVideoUrl ?: "",
    thumbnailUrl ?: ""
)

internal fun List<RecipeResponse>.toPresentation(): List<RecipeModel> = map { it.toPresentation() }