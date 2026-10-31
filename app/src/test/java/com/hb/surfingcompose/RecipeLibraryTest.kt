package com.hb.surfingcompose

import com.hb.surfingcompose.domain.library.MemoryRecipeLibrary
import com.hb.surfingcompose.domain.models.NutritionModel
import com.hb.surfingcompose.domain.models.RecipeModel
import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.test.runTest

class RecipeLibraryTest {
    private val recipe = RecipeModel(7, "Soup", nutrition = NutritionModel(), description = "", originalVideoUrl = "", thumbnailUrl = "")

    @Test fun addingToACollectionSavesRecipeAndRemovingMembershipKeepsFavorite() = runTest {
        val library = MemoryRecipeLibrary()
        val id = library.createCollection("Weeknight dinners")!!
        library.setMembership(recipe, id, true)
        library.setMembership(recipe, id, true)
        assertEquals(listOf(recipe), library.state.value.recipes)
        assertEquals(setOf(recipe.id), library.state.value.collections.single().recipeIds)
        library.setMembership(recipe, id, false)
        assertTrue(library.state.value.collections.single().recipeIds.isEmpty())
        assertEquals(listOf(recipe), library.state.value.recipes)
    }

    @Test fun removingFavoriteRemovesAllMembershipsAndDeletingCollectionKeepsRecipes() = runTest {
        val library = MemoryRecipeLibrary()
        val first = library.createCollection("Dinners")!!
        val second = library.createCollection("Soups")!!
        library.setMembership(recipe, first, true)
        library.setMembership(recipe, second, true)
        library.deleteCollection(first)
        assertEquals(listOf(recipe), library.state.value.recipes)
        library.toggleFavorite(recipe)
        assertTrue(library.state.value.recipes.isEmpty())
        assertTrue(library.state.value.collections.single().recipeIds.isEmpty())
        library.toggleFavorite(recipe)
        assertEquals(listOf(recipe), library.state.value.recipes)
    }

    @Test fun blankAndDuplicateCollectionsAreHandled() = runTest {
        val library = MemoryRecipeLibrary()
        assertNull(library.createCollection("  "))
        val id = library.createCollection("  Dinners  ")
        assertEquals(id, library.createCollection("dinners"))
        assertEquals("Dinners", library.state.value.collections.single().name)
        library.setMembership(recipe, "missing", true)
        assertTrue(library.state.value.recipes.isEmpty())
    }
    @Test fun renameRejectsDuplicatesAndUndoDoesNotResurrectDeletedCollections() = runTest {
        val library = MemoryRecipeLibrary()
        val first = library.createCollection("Dinners")!!
        val second = library.createCollection("Lunch")!!
        assertFalse(library.renameCollection(first, "lunch"))
        assertTrue(library.renameCollection(first, "Weeknight dinners"))
        library.setMembership(recipe, first, true)
        library.setMembership(recipe, second, true)
        val removal = library.toggleFavorite(recipe)!!
        library.deleteCollection(second)
        library.restoreFavorite(removal)
        assertEquals("Weeknight dinners", library.state.value.collections.single().name)
        assertEquals(setOf(recipe.id), library.state.value.collections.single().recipeIds)
    }

}
