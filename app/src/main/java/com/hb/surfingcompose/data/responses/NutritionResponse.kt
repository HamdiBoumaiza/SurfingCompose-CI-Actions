package com.hb.surfingcompose.data.responses

import com.google.gson.annotations.SerializedName


data class NutritionResponse(
    @SerializedName("fat") val fat: Int? = null,
    @SerializedName("calories") val calories: Int? = null,
    @SerializedName("sugar") val sugar: Int? = null,
    @SerializedName("carbohydrates") val carbohydrates: Int? = null,
    @SerializedName("fiber") val fiber: Int? = null,
    @SerializedName("protein") val protein: Int? = null
)