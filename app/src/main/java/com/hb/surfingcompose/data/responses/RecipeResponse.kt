package com.hb.surfingcompose.data.responses

import com.google.gson.annotations.SerializedName


data class RecipeResponse(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("instructions") val instructions: List<InstructionsResponse> = emptyList(),
    @SerializedName("tags") val tags: List<TagsResponse> = emptyList(),
    @SerializedName("nutrition") val nutrition: NutritionResponse? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("original_video_url") val originalVideoUrl: String? = null,
    @SerializedName("thumbnail_url") val thumbnailUrl: String? = null
)