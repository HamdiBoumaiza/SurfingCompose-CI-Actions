package com.hb.surfingcompose.data.services

import com.hb.surfingcompose.data.responses.RecipesListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RecipesAPI {

    @GET("list")
    suspend fun getRecipes(
        @Query("from") from: Int,
        @Query("size") size: Int,
        @Query("q") name: String?
    ): Response<RecipesListResponse>
}