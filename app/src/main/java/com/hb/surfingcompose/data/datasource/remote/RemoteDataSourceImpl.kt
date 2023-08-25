package com.hb.surfingcompose.data.datasource.remote

import com.hb.surfingcompose.data.SurfaceResult
import com.hb.surfingcompose.data.responses.RecipeResponse
import com.hb.surfingcompose.data.services.RecipesAPI
import com.hb.surfingcompose.domain.request.RecipesRequest
import javax.inject.Inject


class RemoteDataSourceImpl @Inject constructor(private val recipesApi: RecipesAPI) : RemoteDataSource {

    override suspend fun getRecipes(recipesRequest: RecipesRequest): SurfaceResult<List<RecipeResponse>> {
        return try {
            recipesApi.getRecipes(recipesRequest.from, recipesRequest.size, recipesRequest.nameOfFood).run {
                if (isSuccessful && body() != null) {
                    SurfaceResult.Success(body()?.results ?: emptyList())
                } else {
                    SurfaceResult.Error(Exception("Something went wrong , code HTTP : ${code()}"))
                }
            }
        } catch (exception: Exception) {
            SurfaceResult.Error(exception)
        }
    }

}