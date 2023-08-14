package com.hb.surfingcompose.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecipeModel(
    val id: Int,
    val name: String,
    val instructions: List<InstructionsModel> = emptyList(),
    val tags: List<TagsModel> = emptyList(),
    val nutrition: NutritionModel,
    val description: String,
    val originalVideoUrl: String,
    val thumbnailUrl: String
) : Parcelable