package com.hb.surfingcompose.data.library

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    // Invalidation of any library table triggers a consistent transactional snapshot.
    @Query("SELECT (SELECT COUNT(*) FROM favorite_recipe) + (SELECT COUNT(*) FROM recipe_collection) + (SELECT COUNT(*) FROM collection_recipe)")
    fun observeChanges(): Flow<Int>
    @Query("SELECT * FROM favorite_recipe ORDER BY rowid") suspend fun favorites(): List<FavoriteRecipeEntity>
    @Query("SELECT * FROM recipe_collection ORDER BY rowid") suspend fun collections(): List<CollectionEntity>
    @Query("SELECT * FROM collection_recipe") suspend fun memberships(): List<MembershipEntity>
    @Query("SELECT * FROM library_metadata WHERE id = 1") suspend fun metadata(): LibraryMetadata?
    @Upsert suspend fun saveFavorite(recipe: FavoriteRecipeEntity)
    @Upsert suspend fun saveCollection(collection: CollectionEntity)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun addMembership(membership: MembershipEntity)
    @Upsert suspend fun saveMetadata(metadata: LibraryMetadata)
    @Query("DELETE FROM favorite_recipe WHERE id = :id") suspend fun deleteFavorite(id: Int)
    @Query("DELETE FROM recipe_collection WHERE id = :id") suspend fun deleteCollection(id: String)
    @Query("DELETE FROM collection_recipe WHERE collectionId = :collectionId AND recipeId = :recipeId")
    suspend fun removeMembership(collectionId: String, recipeId: Int)
}
