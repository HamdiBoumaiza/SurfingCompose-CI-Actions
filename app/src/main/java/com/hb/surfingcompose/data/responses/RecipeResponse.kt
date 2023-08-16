package com.hb.surfingcompose.data.responses

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity(tableName = "recipe")
data class RecipeResponse(
    @SerializedName("id") @PrimaryKey val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("instructions") val instructions: List<InstructionsResponse> = emptyList(),
    @SerializedName("instructions") val tags: List<TagsResponse> = emptyList(),
    @SerializedName("instructions") val nutrition: NutritionResponse? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("original_video_url") val originalVideoUrl: String? = null,
    @SerializedName("thumbnail_url") val thumbnailUrl: String? = null
)