package com.hb.surfingcompose.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecipesListModel(val results: List<RecipeModel>) : Parcelable