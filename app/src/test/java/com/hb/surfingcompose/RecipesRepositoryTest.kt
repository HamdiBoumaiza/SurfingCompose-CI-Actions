package com.hb.surfingcompose

import com.hb.surfingcompose.data.InternetConnection
import com.hb.surfingcompose.data.RecipesRepositoryImpl
import com.hb.surfingcompose.data.SurfaceResult
import com.hb.surfingcompose.data.datasource.local.LocalDataSource
import com.hb.surfingcompose.data.datasource.remote.RemoteDataSource
import com.hb.surfingcompose.data.mapper.toEntity
import com.hb.surfingcompose.data.mapper.toResponse
import com.hb.surfingcompose.data.responses.InstructionsResponse
import com.hb.surfingcompose.data.responses.RecipeResponse
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.domain.request.RecipesRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class RecipesRepositoryTest {
    private class MemoryCache(var recipes: List<RecipeResponse> = emptyList()) : LocalDataSource {
        override suspend fun saveRecipes(list: List<RecipeResponse>) { recipes = list.toEntity().toResponse() }
        override suspend fun getRecipes(): List<RecipeResponse> = recipes
    }

    private class RemoteRecipes : RemoteDataSource {
        var calls = 0
        var result: SurfaceResult<List<RecipeResponse>> = SurfaceResult.Success(emptyList())
        override suspend fun getRecipes(recipesRequest: RecipesRequest): SurfaceResult<List<RecipeResponse>> {
            calls++
            return result
        }
    }

    @Test
    fun offlineSearchTrimsQueryAndMatchesNamesIgnoringCase() = runTest {
        val remote = RemoteRecipes()
        val cache = MemoryCache(listOf(recipe("Tomato Soup"), recipe("Pasta"), recipe("SOUP with beans")))
        val results = RecipesRepositoryImpl(cache, remote, InternetConnection { false })
            .getRecipes(RecipesRequest("  soup  ")).toList()
        assertEquals(SurfaceResult.Loading, results.first())
        assertEquals(listOf("Tomato Soup", "SOUP with beans"), success(results.last()).map { it.name })
        assertEquals(0, remote.calls)
    }

    @Test
    fun offlinePaginationIsAppliedAfterSearch() = runTest {
        val cache = MemoryCache(listOf(recipe("Soup one"), recipe("Pasta"), recipe("Soup two"), recipe("Soup three")))
        val results = RecipesRepositoryImpl(cache, RemoteRecipes(), InternetConnection { false })
            .getRecipes(RecipesRequest(1, 1, "soup")).toList()
        assertEquals(listOf("Soup two"), success(results.last()).map { it.name })
    }

    @Test
    fun offlineTagFilterUsesApiNameAndFiltersBeforePagination() = runTest {
        val tag = com.hb.surfingcompose.domain.models.TagsModel("Quick meals", "", "under_30_minutes")
        val cache = MemoryCache(listOf(
            recipe("Soup one").copy(tags = listOf(com.hb.surfingcompose.data.responses.TagsResponse("Different label", "", "under_30_minutes"))),
            recipe("Soup two"),
            recipe("Soup three").copy(tags = listOf(com.hb.surfingcompose.data.responses.TagsResponse("Quick meals", "")))
        ))
        val results = RecipesRepositoryImpl(cache, RemoteRecipes(), InternetConnection { false })
            .getRecipes(RecipesRequest(1, 1, "soup", tag)).toList()
        assertEquals(listOf("Soup three"), success(results.last()).map { it.name })
    }

    @Test fun offlineCombinedFiltersMatchAllCategoriesBeforePagination() = runTest {
        val vegan = com.hb.surfingcompose.domain.models.TagsModel("Vegan", "diet", "vegan")
        val quick = com.hb.surfingcompose.domain.models.TagsModel("Quick", "time", "under_30_minutes")
        val cache = MemoryCache(listOf(
            recipe("Soup one").copy(tags = listOf(com.hb.surfingcompose.data.responses.TagsResponse("Vegan", "diet", "vegan"))),
            recipe("Soup two").copy(tags = listOf(com.hb.surfingcompose.data.responses.TagsResponse("Vegan", "diet", "vegan"), com.hb.surfingcompose.data.responses.TagsResponse("Quick", "time", "under_30_minutes")))
        ))
        val results = RecipesRepositoryImpl(cache, RemoteRecipes(), InternetConnection { false })
            .getRecipes(RecipesRequest(0, 30, "soup", filterTags = listOf(vegan, quick))).toList()
        assertEquals(listOf("Soup two"), success(results.last()).map { it.name })
    }

    @Test
    fun unmatchedOfflineQueryReturnsEmptyList() = runTest {
        val results = RecipesRepositoryImpl(MemoryCache(listOf(recipe("Pasta"))), RemoteRecipes(), InternetConnection { false })
            .getRecipes(RecipesRequest("soup")).toList()
        assertTrue(success(results.last()).isEmpty())
    }

    @Test
    fun onlineRecipesKeepInstructionPositionsWhenReadOffline() = runTest {
        var online = true
        val cache = MemoryCache()
        val remote = RemoteRecipes().apply {
            result = SurfaceResult.Success(listOf(recipe("Soup").copy(instructions = listOf(
                InstructionsResponse("Chop", 1), InstructionsResponse("Cook", 2)
            ))))
        }
        val repository = RecipesRepositoryImpl(cache, remote, InternetConnection { online })
        val onlineResults = success(repository.getRecipes(RecipesRequest()).toList().last())
        online = false
        val offlineResults = success(repository.getRecipes(RecipesRequest()).toList().last())
        assertEquals(onlineResults, offlineResults)
        assertEquals(listOf(1, 2), offlineResults.single().instructions.map { it.position })
    }

    @Test
    fun cacheFailureIsEmittedAsError() = runTest {
        val cache = object : LocalDataSource {
            override suspend fun saveRecipes(list: List<RecipeResponse>) = Unit
            override suspend fun getRecipes(): List<RecipeResponse> = throw IllegalStateException("Cache failed")
        }
        val results = RecipesRepositoryImpl(cache, RemoteRecipes(), InternetConnection { false })
            .getRecipes(RecipesRequest()).toList()
        assertEquals(SurfaceResult.Loading, results.first())
        assertEquals("Cache failed", (results.last() as SurfaceResult.Error).exception.message)
    }

    @Test(expected = CancellationException::class)
    fun cancellationIsNotConvertedIntoAnError() = runTest {
        val cache = object : LocalDataSource {
            override suspend fun saveRecipes(list: List<RecipeResponse>) = Unit
            override suspend fun getRecipes(): List<RecipeResponse> = throw CancellationException("Cancelled")
        }
        RecipesRepositoryImpl(cache, RemoteRecipes(), InternetConnection { false })
            .getRecipes(RecipesRequest()).toList()
        Unit
    }

    private fun recipe(name: String) = RecipeResponse(id = name.hashCode(), name = name)
    private fun success(result: SurfaceResult<List<RecipeModel>>) = (result as SurfaceResult.Success).data
}
