package com.hb.surfingcompose.data.responses

import com.google.gson.annotations.SerializedName


data class RecipesListResponse(
    @SerializedName("results") val results: List<RecipeResponse> = emptyList()
)