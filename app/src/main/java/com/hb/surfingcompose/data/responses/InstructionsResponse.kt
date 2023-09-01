package com.hb.surfingcompose.data.responses

import com.google.gson.annotations.SerializedName


data class InstructionsResponse(
    @SerializedName("display_text") val displayText: String? = null,
    @SerializedName("position") val position : Int? = null
)