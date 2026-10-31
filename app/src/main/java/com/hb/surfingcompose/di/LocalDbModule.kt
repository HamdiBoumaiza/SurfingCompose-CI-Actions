package com.hb.surfingcompose.di

import android.app.Application
import android.content.Context
import com.hb.surfingcompose.domain.library.RecipeLibrary
import com.hb.surfingcompose.data.library.RoomRecipeLibrary
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

    @Provides
    @Singleton
    fun provideRecipeLibrary(application: Application, database: RecipesDatabase): RecipeLibrary =
        RoomRecipeLibrary(database, application.getSharedPreferences("recipe_library", Context.MODE_PRIVATE))

    @Singleton
    @Provides
    fun provideStarDatabase(application: Application): RecipesDatabase {
        return Room
            .databaseBuilder(application, RecipesDatabase::class.java, "recipes_database")
            .addTypeConverter(DbConverters())
            .addMigrations(RecipesDatabase.MIGRATION_3_4, RecipesDatabase.MIGRATION_4_5)
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