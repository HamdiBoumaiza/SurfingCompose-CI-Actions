package com.hb.surfingcompose.data.responses

import com.google.gson.annotations.SerializedName


data class TagsResponse(
    @SerializedName("display_name") val displayName: String? = null,
    @SerializedName("type") val type: String? = null
)