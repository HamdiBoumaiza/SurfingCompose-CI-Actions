package com.hb.surfingcompose.domain.request

import com.hb.surfingcompose.domain.request.Constants.DEFAULT_GET_RECIPES_FROM
import com.hb.surfingcompose.domain.request.Constants.DEFAULT_GET_RECIPES_SIZE

class RecipesRequest(val from: Int, val size: Int, val nameOfFood: String? = null) {
    constructor() : this(DEFAULT_GET_RECIPES_FROM, DEFAULT_GET_RECIPES_SIZE, "")
    constructor(nameOfFood: String?) : this(DEFAULT_GET_RECIPES_FROM, DEFAULT_GET_RECIPES_SIZE, nameOfFood)
}