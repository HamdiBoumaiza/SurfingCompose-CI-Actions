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
        override suspend fun getRecipes(from: Int?, size: Int?, name: String?): Response<RecipesListResponse> {
            throw exception
        }
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
