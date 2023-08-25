package com.hb.surfingcompose.di

import com.hb.surfingcompose.BuildConfig
import com.hb.surfingcompose.data.RecipesRepositoryImpl
import com.hb.surfingcompose.data.datasource.local.LocalDataSource
import com.hb.surfingcompose.data.datasource.remote.RemoteDataSource
import com.hb.surfingcompose.data.datasource.remote.RemoteDataSourceImpl
import com.hb.surfingcompose.data.services.RecipesAPI
import com.hb.surfingcompose.domain.repository.RecipesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://tasty.p.rapidapi.com/recipes/"
    private const val TIMEOUT = 30L
    private const val HEADER_KEY = "X-RapidAPI-Key"
    private const val HEADER_RAPID_API_HOST_KEY = "X-RapidAPI-Host"
    private const val HEADER_RAPID_API_HOST_VALUE = "tasty.p.rapidapi.com"

    @Singleton
    @Provides
    fun provideRecipesRepository(localDataSource: LocalDataSource, remoteDataSource: RemoteDataSource): RecipesRepository =
        RecipesRepositoryImpl(localDataSource, remoteDataSource)

    @Singleton
    @Provides
    fun provideRemoteDataSource(api: RecipesAPI): RemoteDataSource =
        RemoteDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideRecipesApi(okHttpClient: OkHttpClient): RecipesAPI {
        return Retrofit
            .Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL).client(okHttpClient)
            .build()
            .create(RecipesAPI::class.java)
    }

    @Singleton
    @Provides
    fun providesOkHttpClient(): OkHttpClient {
        val client = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
        client.addInterceptor { chain ->
            val request = chain.request().newBuilder()
            request.addHeader(HEADER_KEY, BuildConfig.RAPID_API_KEY)
            request.addHeader(HEADER_RAPID_API_HOST_KEY, HEADER_RAPID_API_HOST_VALUE)
            chain.proceed(request.build())
        }
        return client.build()
    }
}