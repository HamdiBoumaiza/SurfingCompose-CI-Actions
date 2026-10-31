package com.hb.surfingcompose.domain.library

import com.hb.surfingcompose.domain.models.RecipeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

data class RecipeCollection(val id: String, val name: String, val recipeIds: Set<Int> = emptySet())
data class LibraryState(
    val recipes: List<RecipeModel> = emptyList(),
    val collections: List<RecipeCollection> = emptyList(),
    val isLoaded: Boolean = true,
    val error: String? = null
)
data class FavoriteRemoval(val recipe: RecipeModel, val collectionIds: Set<String>)

interface RecipeLibrary {
    val state: StateFlow<LibraryState>
    suspend fun toggleFavorite(recipe: RecipeModel): FavoriteRemoval?
    suspend fun restoreFavorite(removal: FavoriteRemoval)
    suspend fun createCollection(name: String): String?
    suspend fun renameCollection(collectionId: String, name: String): Boolean
    suspend fun setMembership(recipe: RecipeModel, collectionId: String, included: Boolean)
    suspend fun deleteCollection(collectionId: String)
}

/** In-memory implementation for previews and isolated tests. Production uses Room. */
open class MemoryRecipeLibrary(initial: LibraryState = LibraryState()) : RecipeLibrary {
    private val mutableState = MutableStateFlow(initial)
    override val state: StateFlow<LibraryState> = mutableState
    protected open suspend fun persist(state: LibraryState) = Unit
    private suspend fun update(state: LibraryState) {
        persist(state)
        mutableState.value = state
    }
    override suspend fun toggleFavorite(recipe: RecipeModel): FavoriteRemoval? {
        val current = state.value
        val saved = current.recipes.firstOrNull { it.id == recipe.id }
        val removed = saved?.let { FavoriteRemoval(it, current.collections.filter { recipe.id in it.recipeIds }.map { it.id }.toSet()) }
        update(current.copy(
            recipes = if (saved != null) current.recipes.filterNot { it.id == recipe.id } else current.recipes + recipe,
            collections = if (saved != null) current.collections.map { it.copy(recipeIds = it.recipeIds - recipe.id) } else current.collections
        ))
        return removed
    }
    override suspend fun restoreFavorite(removal: FavoriteRemoval) {
        val current = state.value
        update(current.copy(
            recipes = current.recipes.filterNot { it.id == removal.recipe.id } + removal.recipe,
            collections = current.collections.map { if (it.id in removal.collectionIds) it.copy(recipeIds = it.recipeIds + removal.recipe.id) else it }
        ))
    }
    override suspend fun createCollection(name: String): String? {
        val trimmed = name.trim().take(60)
        if (trimmed.isBlank()) return null
        state.value.collections.firstOrNull { it.name.equals(trimmed, true) }?.let { return it.id }
        val collection = RecipeCollection(UUID.randomUUID().toString(), trimmed)
        update(state.value.copy(collections = state.value.collections + collection))
        return collection.id
    }
    override suspend fun renameCollection(collectionId: String, name: String): Boolean {
        val trimmed = name.trim().take(60)
        if (trimmed.isBlank() || state.value.collections.none { it.id == collectionId } ||
            state.value.collections.any { it.id != collectionId && it.name.equals(trimmed, true) }) return false
        update(state.value.copy(collections = state.value.collections.map { if (it.id == collectionId) it.copy(name = trimmed) else it }))
        return true
    }
    override suspend fun setMembership(recipe: RecipeModel, collectionId: String, included: Boolean) {
        val current = state.value
        if (current.collections.none { it.id == collectionId }) return
        update(current.copy(
            recipes = if (included && current.recipes.none { it.id == recipe.id }) current.recipes + recipe else current.recipes,
            collections = current.collections.map {
                if (it.id == collectionId) it.copy(recipeIds = if (included) it.recipeIds + recipe.id else it.recipeIds - recipe.id) else it
            }
        ))
    }
    override suspend fun deleteCollection(collectionId: String) {
        update(state.value.copy(collections = state.value.collections.filterNot { it.id == collectionId }))
    }
}
