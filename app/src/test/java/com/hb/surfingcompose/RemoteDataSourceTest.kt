package com.hb.surfingcompose

import com.hb.surfingcompose.data.SurfaceResult
import com.hb.surfingcompose.data.datasource.remote.RemoteDataSourceImpl
import com.hb.surfingcompose.data.responses.RecipesListResponse
import com.hb.surfingcompose.data.services.RecipesAPI
import com.hb.surfingcompose.domain.request.RecipesRequest
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class RemoteDataSourceTest {
    private fun failingApi(exception: Exception) = object : RecipesAPI {
        override suspend fun getRecipes(from: Int?, size: Int?, name: String?, tags: String?): Response<RecipesListResponse> {
            throw exception
        }
    }

    @Test
    fun tagMachineNameIsSentWithSearchQuery() = runTest {
        var captured: List<Any?>? = null
        val api = object : RecipesAPI {
            override suspend fun getRecipes(from: Int?, size: Int?, name: String?, tags: String?): Response<RecipesListResponse> {
                captured = listOf(from, size, name, tags)
                return Response.success(RecipesListResponse())
            }
        }
        RemoteDataSourceImpl(api).getRecipes(RecipesRequest(0, 20, "soup", com.hb.surfingcompose.domain.models.TagsModel("Quick meals", "", "under_30_minutes")))
        assertEquals(listOf(0, 20, "soup", "under_30_minutes"), captured)
    }

    @Test fun paginationAndCombinedFiltersAreSentWithoutDuplicateTags() = runTest {
        var captured: List<Any?>? = null
        val api = object : RecipesAPI {
            override suspend fun getRecipes(from: Int?, size: Int?, name: String?, tags: String?): Response<RecipesListResponse> {
                captured = listOf(from, size, name, tags)
                return Response.success(RecipesListResponse())
            }
        }
        val vegan = com.hb.surfingcompose.domain.models.TagsModel("Vegan", "diet", "vegan")
        val quick = com.hb.surfingcompose.domain.models.TagsModel("Quick", "time", "under_30_minutes")
        RemoteDataSourceImpl(api).getRecipes(RecipesRequest(30, 30, "soup", vegan, listOf(vegan, quick)))
        assertEquals(listOf(30, 30, "soup", "vegan,under_30_minutes"), captured)
    }

    @Test(expected = CancellationException::class)
    fun cancelledNetworkRequestPropagatesCancellation() = runTest {
        RemoteDataSourceImpl(failingApi(CancellationException("Cancelled"))).getRecipes(RecipesRequest())
        Unit
    }

    @Test
    fun networkFailureIsReportedAsAnError() = runTest {
        val result = RemoteDataSourceImpl(failingApi(IOException("Offline"))).getRecipes(RecipesRequest())
        assertEquals("Offline", (result as SurfaceResult.Error).exception.message)
    }
}
