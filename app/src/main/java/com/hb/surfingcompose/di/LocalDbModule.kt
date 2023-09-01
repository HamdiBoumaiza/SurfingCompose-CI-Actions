package com.hb.surfingcompose.di

import android.app.Application
import androidx.room.Room
import com.hb.surfingcompose.data.datasource.local.DbConverters
import com.hb.surfingcompose.data.datasource.local.LocalDataSource
import com.hb.surfingcompose.data.datasource.local.LocalDataSourceImpl
import com.hb.surfingcompose.data.datasource.local.RecipesDao
import com.hb.surfingcompose.data.datasource.local.RecipesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDbModule {

    @Singleton
    @Provides
    fun provideStarDatabase(application: Application): RecipesDatabase {
        return Room
            .databaseBuilder(application, RecipesDatabase::class.java, "recipes_database")
            .addTypeConverter(DbConverters())
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideStarDao(starDatabase: RecipesDatabase): RecipesDao {
        return starDatabase.recipesDao()
    }

    @Singleton
    @Provides
    fun provideLocalDataSource(recipesDao: RecipesDao): LocalDataSource =
        LocalDataSourceImpl(recipesDao)
}