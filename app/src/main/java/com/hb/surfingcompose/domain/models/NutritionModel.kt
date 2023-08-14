package com.hb.surfingcompose.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class NutritionModel(
    val fat: Int,
    val calories: Int,
    val sugar: Int,
    val carbohydrates: Int,
    val fiber: Int,
    val protein: Int
) : Parcelable {
    constructor() : this(0, 0, 0, 0, 0, 0)
}