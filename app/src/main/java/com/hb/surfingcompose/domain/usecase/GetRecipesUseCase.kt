package com.hb.surfingcompose.domain.usecase

import com.hb.surfingcompose.data.RecipesRepositoryImpl
import com.hb.surfingcompose.data.SurfaceResult
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.domain.request.RecipesRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecipesUseCase @Inject constructor(private val repositoryImpl: RecipesRepositoryImpl) {

    suspend operator fun invoke(recipesRequest: RecipesRequest): Flow<SurfaceResult<List<RecipeModel>>>  {
        return repositoryImpl.getRecipes(recipesRequest)
    }

}
