package com.hb.surfingcompose.domain.request

import com.hb.surfingcompose.domain.models.TagsModel
import java.util.Locale
import com.hb.surfingcompose.domain.request.Constants.DEFAULT_GET_RECIPES_FROM
import com.hb.surfingcompose.domain.request.Constants.DEFAULT_GET_RECIPES_SIZE

class RecipesRequest(
    val from: Int,
    val size: Int,
    val nameOfFood: String? = null,
    val tag: TagsModel? = null,
    val filterTags: List<TagsModel> = emptyList()
) {
    constructor() : this(DEFAULT_GET_RECIPES_FROM, DEFAULT_GET_RECIPES_SIZE)
    constructor(nameOfFood: String?) : this(DEFAULT_GET_RECIPES_FROM, DEFAULT_GET_RECIPES_SIZE, nameOfFood)
}

internal fun TagsModel.apiName(): String =
    name?.takeIf { it.isNotBlank() } ?: displayName.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_").trim('_')
