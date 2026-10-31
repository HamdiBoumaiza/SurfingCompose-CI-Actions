package com.hb.surfingcompose.data

import com.hb.surfingcompose.data.datasource.local.LocalDataSource
import com.hb.surfingcompose.data.datasource.remote.RemoteDataSource
import com.hb.surfingcompose.data.responses.RecipeResponse
import com.hb.surfingcompose.domain.mapper.toPresentation
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.domain.repository.RecipesRepository
import com.hb.surfingcompose.domain.request.RecipesRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class RecipesRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
    private val internetConnection: InternetConnection
) : RecipesRepository {

    override suspend fun getRecipe(id: Int): RecipeModel? = kotlinx.coroutines.withContext(Dispatchers.IO) {
        localDataSource.getRecipe(id)?.toPresentation()
    }

    override suspend fun saveRecipes(list: List<RecipeResponse>) {
        localDataSource.saveRecipes(list)
    }

    override suspend fun getRecipes(recipesRequest: RecipesRequest): Flow<SurfaceResult<List<RecipeModel>>> {
        return flow<SurfaceResult<List<RecipeModel>>> {
            emit(SurfaceResult.Loading)
            if (internetConnection.isAvailable()) {
                when (val result = remoteDataSource.getRecipes(recipesRequest)) {
                    is SurfaceResult.Success -> {
                        saveRecipes(result.data)
                        emit(SurfaceResult.Success(result.data.toPresentation()))
                    }
                    is SurfaceResult.Error -> emit(result)
                    SurfaceResult.Loading -> emit(SurfaceResult.Loading)
                }
            } else {
                val query = recipesRequest.nameOfFood.orEmpty().trim()
                val recipes = localDataSource.getRecipes()
                    .filter { query.isEmpty() || it.name.orEmpty().contains(query, ignoreCase = true) }
                    .filter { recipe -> (recipesRequest.filterTags + listOfNotNull(recipesRequest.tag)).all { selected ->
                        recipe.tags.any { tag ->
                            if (!tag.name.isNullOrBlank() && !selected.name.isNullOrBlank()) tag.name == selected.name
                            else tag.displayName.orEmpty().equals(selected.displayName, ignoreCase = true)
                        }
                    } }
                    .drop(recipesRequest.from.coerceAtLeast(0))
                    .take(recipesRequest.size.coerceAtLeast(0))
                emit(SurfaceResult.Success(recipes.toPresentation()))
            }
        }.catch { exception ->
            if (exception is CancellationException) throw exception
            emit(SurfaceResult.Error(exception as? Exception ?: Exception(exception)))
        }.flowOn(Dispatchers.IO)
    }
}
