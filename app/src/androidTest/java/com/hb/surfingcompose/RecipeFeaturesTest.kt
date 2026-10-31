package com.hb.surfingcompose

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hb.surfingcompose.data.SurfaceResult
import com.hb.surfingcompose.data.datasource.local.DbConverters
import com.hb.surfingcompose.data.datasource.local.RecipesDatabase
import com.hb.surfingcompose.data.library.RoomRecipeLibrary
import com.hb.surfingcompose.domain.library.LibraryState
import com.hb.surfingcompose.domain.library.RecipeCollection
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import com.hb.surfingcompose.data.mapper.toEntity
import com.hb.surfingcompose.data.responses.RecipeResponse
import com.hb.surfingcompose.domain.library.MemoryRecipeLibrary
import com.hb.surfingcompose.domain.models.InstructionsModel
import com.hb.surfingcompose.domain.models.NutritionModel
import com.hb.surfingcompose.domain.models.RecipeModel
import com.hb.surfingcompose.domain.models.TagsModel
import com.hb.surfingcompose.domain.repository.RecipesRepository
import com.hb.surfingcompose.domain.request.RecipesRequest
import com.hb.surfingcompose.domain.usecase.GetRecipesUseCase
import com.hb.surfingcompose.presentation.activities.RecipesNavigation
import com.hb.surfingcompose.presentation.screens.CookingScreen
import com.hb.surfingcompose.presentation.theme.SurfingComposeTheme
import com.hb.surfingcompose.presentation.viewmodel.RecipesViewModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecipeFeaturesTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()
    private val recipe = RecipeModel(
        77, "Tomato soup", nutrition = NutritionModel(), description = "A warming soup.",
        thumbnailUrl = "", originalVideoUrl = "", tags = listOf(TagsModel("Vegetarian", "diet", "vegetarian")),
        instructions = listOf(InstructionsModel("Chop the tomatoes.", 1), InstructionsModel("Simmer and serve.", 2))
    )

    @Test fun favoriteCollectionAndTagJourney() {
        val library = MemoryRecipeLibrary()
        val requests = mutableListOf<RecipesRequest>()
        val repository = object : RecipesRepository {
            override suspend fun saveRecipes(list: List<RecipeResponse>) = Unit
            override suspend fun getRecipes(recipesRequest: RecipesRequest) = flowOf(SurfaceResult.Success(listOf(recipe))).also { requests += recipesRequest }
        }
        composeRule.setContent {
            val viewModel = remember { RecipesViewModel(GetRecipesUseCase(repository), SavedStateHandle(), library) }
            SurfingComposeTheme { RecipesNavigation(rememberNavController(), viewModel) }
        }
        composeRule.onNodeWithContentDescription("Save to favorites").performScrollTo().performClick()
        composeRule.onNodeWithText("My recipes").performClick()
        composeRule.onNodeWithText(recipe.name).performScrollTo().performClick()
        composeRule.onNodeWithText("Add to collection").performScrollTo().performClick()
        composeRule.onNodeWithText("Collection name").performTextInput("Weeknight dinners")
        composeRule.onNodeWithText("Create and add recipe").performClick()
        composeRule.onNodeWithText("Weeknight dinners").assertIsOn()
        composeRule.onNodeWithText("Done").performClick()
        composeRule.onNodeWithText("Vegetarian").performScrollTo().performClick()
        composeRule.onNodeWithText("Clear filter").assertExists()
        composeRule.runOnIdle {
            assertEquals("vegetarian", requests.last().tag?.name)
            assertEquals(setOf(recipe.id), library.state.value.collections.single().recipeIds)
        }
        composeRule.onNodeWithText("Clear filter").performClick()
        composeRule.runOnIdle { assertNull(requests.last().tag) }
    }

    @Test fun legacyLibraryMigratesToRoomAndUndoSurvivesReload() = runBlocking {
        val context = composeRule.activity
        val name = "feature-room-library"
        val prefs = context.getSharedPreferences("feature-test-library", Context.MODE_PRIVATE)
        context.deleteDatabase(name)
        prefs.edit().putString("library_v1", Gson().toJson(LibraryState(listOf(recipe), listOf(RecipeCollection("dinners", "Dinners", setOf(recipe.id)))))).commit()
        fun database() = Room.databaseBuilder(context, RecipesDatabase::class.java, name).addTypeConverter(DbConverters()).build()
        var db = database()
        var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            var library = RoomRecipeLibrary(db, prefs, scope)
            withTimeout(5000) { library.state.first { it.isLoaded } }
            assertEquals(listOf(recipe), library.state.value.recipes)
            assertEquals(setOf(recipe.id), library.state.value.collections.single().recipeIds)
            assertTrue(library.renameCollection("dinners", "Weeknight dinners"))
            val removal = library.toggleFavorite(recipe)!!
            assertTrue(library.state.value.collections.single().recipeIds.isEmpty())
            library.restoreFavorite(removal)
            assertEquals(setOf(recipe.id), library.state.value.collections.single().recipeIds)
            library.toggleFavorite(recipe)
            scope.cancel(); db.close()
            db = database()
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            library = RoomRecipeLibrary(db, prefs, scope)
            withTimeout(5000) { library.state.first { it.isLoaded } }
            assertTrue(library.state.value.recipes.isEmpty()) // Legacy data must not be imported a second time.
            assertEquals("Weeknight dinners", library.state.value.collections.single().name)
        } finally { scope.cancel(); db.close(); context.deleteDatabase(name); prefs.edit().clear().commit() }
    }

    @Test fun cookingProgressAndTimerSurviveRestorationAndExitRestoresWakeFlag() {
        val restoration = StateRestorationTester(composeRule)
        val visible = androidx.compose.runtime.mutableStateOf(true)
        restoration.setContent { SurfingComposeTheme { if (visible.value) CookingScreen(recipe) { visible.value = false } } }
        composeRule.runOnIdle { assertTrue(hasKeepScreenOn(composeRule.activity.window.decorView)) }
        composeRule.onNodeWithText("Complete and next").performClick()
        composeRule.onNodeWithText("Step 2 of 2").performScrollTo().assertExists()
        composeRule.onNodeWithText("Timer settings").performClick()
        composeRule.onNodeWithText("Start timer").performClick()
        restoration.emulateSavedInstanceStateRestore()
        composeRule.onNodeWithText("Step 2 of 2").performScrollTo().assertExists()
        composeRule.onNodeWithText("Completed: 1 / 2").assertExists()
        composeRule.onNodeWithText("Pause").performClick()
        composeRule.onNodeWithText("Resume").assertExists()
        composeRule.onNodeWithText("Exit cooking").performScrollTo().performClick()
        composeRule.onNodeWithText("Stop your timer?").assertExists()
        composeRule.onNodeWithText("Keep cooking").performClick()
        composeRule.onNodeWithText("Reset").performClick()
        composeRule.onNodeWithText("Finish").performClick()
        composeRule.onNodeWithText("All done!").performScrollTo().assertExists()
        composeRule.onNodeWithText("Done").performClick()
        composeRule.onNodeWithText("Kitchen timer").assertDoesNotExist()
        composeRule.runOnIdle { assertFalse(hasKeepScreenOn(composeRule.activity.window.decorView)) }
    }

    private fun hasKeepScreenOn(view: View): Boolean = view.keepScreenOn ||
        (view is ViewGroup && (0 until view.childCount).any { hasKeepScreenOn(view.getChildAt(it)) })

    @Test fun migrationPreservesExistingCachedRecipes() = runBlocking {
        val context = composeRule.activity
        val name = "feature-test-migration"
        context.deleteDatabase(name)
        try {
            SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name).apply { parentFile!!.mkdirs() }, null).use { db ->
                db.execSQL("CREATE TABLE recipe (id INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL, instructions TEXT NOT NULL, tags TEXT NOT NULL, nutrition TEXT NOT NULL, description TEXT NOT NULL, originalVideoUrl TEXT NOT NULL, thumbnailUrl TEXT NOT NULL)")
                db.execSQL("CREATE TABLE instruction (position INTEGER NOT NULL PRIMARY KEY, displayText TEXT NOT NULL)")
                db.execSQL("CREATE TABLE nutrition (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, fat INTEGER NOT NULL, calories INTEGER NOT NULL, sugar INTEGER NOT NULL, carbohydrates INTEGER NOT NULL, fiber INTEGER NOT NULL, protein INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE tag (displayName TEXT NOT NULL PRIMARY KEY, type TEXT NOT NULL)")
                db.execSQL("INSERT INTO recipe VALUES (8, 'Cached soup', '[]', '[]', '{\"id\":0,\"fat\":0,\"calories\":0,\"sugar\":0,\"carbohydrates\":0,\"fiber\":0,\"protein\":0}', '', '', '')")
                db.version = 3
            }
            Room.databaseBuilder(context, RecipesDatabase::class.java, name)
                .addTypeConverter(DbConverters()).addMigrations(RecipesDatabase.MIGRATION_3_4, RecipesDatabase.MIGRATION_4_5).build().let { db ->
                    try {
                        assertEquals("Cached soup", db.recipesDao().queryAllRecipes().single().name)
                        assertEquals(5, db.openHelper.readableDatabase.version)
                        assertNull(db.recipesDao().recipeById(8)!!.prepTimeMinutes)
                        val updated = db.recipesDao().recipeById(8)!!.copy(name = "Fresh soup", prepTimeMinutes = 10, servings = 4)
                        db.recipesDao().insertRecipes(listOf(updated))
                        assertEquals(updated, db.recipesDao().recipeById(8))
                    } finally { db.close() }
                }
        } finally { context.deleteDatabase(name) }
    }
}
