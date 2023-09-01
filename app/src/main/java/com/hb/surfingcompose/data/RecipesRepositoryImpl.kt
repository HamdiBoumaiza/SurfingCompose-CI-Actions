package com.hb.surfingcompose.data

import com.hb.surfingcompose.data.datasource.local.LocalDataSource
import com.hb.surfingcompose.data.datasource.remote.RemoteDataSource
import com.hb.surfingcompose.data.responses.RecipeResponse
import com.hb.surfingcompose.domain.mapper.toPresentation
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.domain.repository.RecipesRepository
import com.hb.surfingcompose.domain.request.RecipesRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecipesRepositoryImpl @Inject constructor(private val localDataSource: LocalDataSource, private val remoteDataSource: RemoteDataSource) :
    RecipesRepository {

    override suspend fun saveRecipes(list: List<RecipeResponse>) {
        localDataSource.saveRecipes(list)
    }

    override suspend fun getRecipes(recipesRequest: RecipesRequest): Flow<SurfaceResult<List<RecipeModel>>> {
        return if (isInternetAvailable()) {
            var result: SurfaceResult<List<RecipeModel>> = SurfaceResult.Loading
            remoteDataSource.getRecipes(recipesRequest)
                .onSuccess { response ->
                    saveRecipes(response)
                    result = SurfaceResult.Success(response.toPresentation())
                }.onError { result = SurfaceResult.Error(it) }
            flow { emit(result) }
        } else flow { SurfaceResult.Success(localDataSource.getRecipes().map { it.toPresentation() }) }
    }

}