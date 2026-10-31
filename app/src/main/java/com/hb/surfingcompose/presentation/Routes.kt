package com.hb.surfingcompose.presentation

const val ROUTE_LIST_RECIPES = "recipes_list_screen"
const val ROUTE_DETAILS_RECIPES = "recipe_detail_screen/{recipeId}"
const val ROUTE_COOKING = "cooking_screen/{recipeId}"
fun detailsRoute(id: Int) = "recipe_detail_screen/$id"
fun cookingRoute(id: Int) = "cooking_screen/$id"
