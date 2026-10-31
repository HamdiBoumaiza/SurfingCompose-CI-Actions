package com.hb.surfingcompose.data.library

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hb.surfingcompose.data.datasource.local.entities.RecipeEntity

@Entity(tableName = "favorite_recipe", primaryKeys = ["id"])
data class FavoriteRecipeEntity(@Embedded val recipe: RecipeEntity)

@Entity(tableName = "recipe_collection")
data class CollectionEntity(@PrimaryKey val id: String, val name: String)

@Entity(
    tableName = "collection_recipe", primaryKeys = ["collectionId", "recipeId"],
    foreignKeys = [
        ForeignKey(entity = CollectionEntity::class, parentColumns = ["id"], childColumns = ["collectionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = FavoriteRecipeEntity::class, parentColumns = ["id"], childColumns = ["recipeId"], onDelete = ForeignKey.CASCADE)
    ], indices = [Index("recipeId")]
)
data class MembershipEntity(val collectionId: String, val recipeId: Int)

@Entity(tableName = "library_metadata")
data class LibraryMetadata(@PrimaryKey val id: Int = 1, val imported: Boolean = true)
