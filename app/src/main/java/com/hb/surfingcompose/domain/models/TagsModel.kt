package com.hb.surfingcompose.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TagsModel(
    val displayName: String,
    val type: String
) : Parcelable