package com.hb.surfingcompose.data.library

import android.content.SharedPreferences
import androidx.room.withTransaction
import com.google.gson.Gson
import com.hb.surfingcompose.data.datasource.local.RecipesDatabase
import com.hb.surfingcompose.data.mapper.toEntity
import com.hb.surfingcompose.data.mapper.toResponse
import com.hb.surfingcompose.data.responses.*
import com.hb.surfingcompose.domain.library.*
import com.hb.surfingcompose.domain.mapper.toPresentation
import com.hb.surfingcompose.domain.models.RecipeModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** All JSON migration, queries and writes run off the UI thread. */
class RoomRecipeLibrary(
    private val database: RecipesDatabase,
    private val legacyPreferences: SharedPreferences,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : RecipeLibrary {
    private val dao = database.libraryDao()
    private val mutableState = MutableStateFlow(LibraryState(isLoaded = false))
    override val state: StateFlow<LibraryState> = mutableState.asStateFlow()
    private val mutex = Mutex()
    private val ready = scope.async { importLegacyLibrary() }

    init {
        scope.launch {
            try {
                ready.await()
                dao.observeChanges().collect { mutex.withLock { refresh() } }
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) {
                mutableState.update { it.copy(isLoaded = true, error = error.message ?: "Unable to open saved recipes") }
            }
        }
    }

    private suspend fun importLegacyLibrary() = withContext(Dispatchers.IO) {
        database.withTransaction {
            if (dao.metadata() != null) return@withTransaction
            // Keep the old file as a recovery copy. The DB marker prevents re-imports.
            val json = legacyPreferences.getString("library_v1", null)
            val legacy = if (json == null) LibraryState() else
                Gson().fromJson(json, LibraryState::class.java) ?: error("Unable to migrate saved recipes")
            legacy.recipes.forEach { dao.saveFavorite(it.favoriteEntity()) }
            legacy.collections.forEach { collection ->
                dao.saveCollection(CollectionEntity(collection.id, collection.name))
                collection.recipeIds.filter { id -> legacy.recipes.any { it.id == id } }.forEach {
                    dao.addMembership(MembershipEntity(collection.id, it))
                }
            }
            dao.saveMetadata(LibraryMetadata())
        }
    }

    private suspend fun refresh() {
        mutableState.value = database.withTransaction {
            val memberships = dao.memberships()
            LibraryState(
                recipes = dao.favorites().map { it.recipe.toResponse().toPresentation() },
                collections = dao.collections().map { collection -> RecipeCollection(
                    collection.id, collection.name,
                    memberships.filter { it.collectionId == collection.id }.map { it.recipeId }.toSet()
                ) }
            )
        }
    }

    private suspend fun <T> write(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        ready.await()
        mutex.withLock {
            val result = database.withTransaction { block() }
            refresh()
            result
        }
    }

    override suspend fun toggleFavorite(recipe: RecipeModel): FavoriteRemoval? = write {
        val saved = dao.favorites().firstOrNull { it.recipe.id == recipe.id }
        if (saved == null) { dao.saveFavorite(recipe.favoriteEntity()); null }
        else {
            val removal = FavoriteRemoval(saved.recipe.toResponse().toPresentation(), dao.memberships().filter { it.recipeId == recipe.id }.map { it.collectionId }.toSet())
            dao.deleteFavorite(recipe.id)
            removal
        }
    }
    override suspend fun restoreFavorite(removal: FavoriteRemoval) = write {
        dao.saveFavorite(removal.recipe.favoriteEntity())
        val ids = dao.collections().map { it.id }.toSet()
        removal.collectionIds.filter { it in ids }.forEach { dao.addMembership(MembershipEntity(it, removal.recipe.id)) }
    }
    override suspend fun createCollection(name: String): String? = write {
        val trimmed = name.trim().take(60)
        if (trimmed.isBlank()) return@write null
        dao.collections().firstOrNull { it.name.equals(trimmed, true) }?.id ?: UUID.randomUUID().toString().also {
            dao.saveCollection(CollectionEntity(it, trimmed))
        }
    }
    override suspend fun renameCollection(collectionId: String, name: String): Boolean = write {
        val trimmed = name.trim().take(60)
        val collections = dao.collections()
        if (trimmed.isBlank() || collections.none { it.id == collectionId } || collections.any { it.id != collectionId && it.name.equals(trimmed, true) }) false
        else { dao.saveCollection(CollectionEntity(collectionId, trimmed)); true }
    }
    override suspend fun setMembership(recipe: RecipeModel, collectionId: String, included: Boolean) = write {
        if (dao.collections().any { it.id == collectionId }) {
            if (included) {
                dao.saveFavorite(recipe.favoriteEntity())
                dao.addMembership(MembershipEntity(collectionId, recipe.id))
            } else dao.removeMembership(collectionId, recipe.id)
        }
    }
    override suspend fun deleteCollection(collectionId: String) = write { dao.deleteCollection(collectionId) }
}

private fun RecipeModel.favoriteEntity(): FavoriteRecipeEntity = FavoriteRecipeEntity(listOf(RecipeResponse(
    id, name,
    instructions.map { InstructionsResponse(it.displayText, it.position) },
    tags.map { TagsResponse(it.displayName, it.type, it.name) },
    NutritionResponse(nutrition.fat, nutrition.calories, nutrition.sugar, nutrition.carbohydrates, nutrition.fiber, nutrition.protein),
    description, originalVideoUrl, thumbnailUrl, prepTimeMinutes, totalTimeMinutes, servings
)).toEntity().single())
