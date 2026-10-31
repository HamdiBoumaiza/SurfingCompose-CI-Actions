package com.hb.surfingcompose.data.datasource.local

import com.hb.surfingcompose.data.library.*
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hb.surfingcompose.data.datasource.local.entities.InstructionsEntity
import com.hb.surfingcompose.data.datasource.local.entities.NutritionEntity
import com.hb.surfingcompose.data.datasource.local.entities.RecipeEntity
import com.hb.surfingcompose.data.datasource.local.entities.TagsEntity

@Database(entities = [RecipeEntity::class, InstructionsEntity::class, TagsEntity::class, NutritionEntity::class, FavoriteRecipeEntity::class, CollectionEntity::class, MembershipEntity::class, LibraryMetadata::class], version = 5)
@TypeConverters(DbConverters::class)
abstract class RecipesDatabase : RoomDatabase() {
    abstract fun recipesDao(): RecipesDao
    abstract fun libraryDao(): LibraryDao

    companion object {
        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tag ADD COLUMN name TEXT")
            }
        }
        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipe ADD COLUMN prepTimeMinutes INTEGER")
                db.execSQL("ALTER TABLE recipe ADD COLUMN totalTimeMinutes INTEGER")
                db.execSQL("ALTER TABLE recipe ADD COLUMN servings INTEGER")
                db.execSQL("CREATE TABLE IF NOT EXISTS favorite_recipe (id INTEGER NOT NULL, name TEXT NOT NULL, instructions TEXT NOT NULL, tags TEXT NOT NULL, nutrition TEXT NOT NULL, description TEXT NOT NULL, originalVideoUrl TEXT NOT NULL, thumbnailUrl TEXT NOT NULL, prepTimeMinutes INTEGER, totalTimeMinutes INTEGER, servings INTEGER, PRIMARY KEY(id))")
                db.execSQL("CREATE TABLE IF NOT EXISTS recipe_collection (id TEXT NOT NULL, name TEXT NOT NULL, PRIMARY KEY(id))")
                db.execSQL("CREATE TABLE IF NOT EXISTS collection_recipe (collectionId TEXT NOT NULL, recipeId INTEGER NOT NULL, PRIMARY KEY(collectionId, recipeId), FOREIGN KEY(collectionId) REFERENCES recipe_collection(id) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(recipeId) REFERENCES favorite_recipe(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_collection_recipe_recipeId ON collection_recipe(recipeId)")
                db.execSQL("CREATE TABLE IF NOT EXISTS library_metadata (id INTEGER NOT NULL, imported INTEGER NOT NULL, PRIMARY KEY(id))")
            }
        }

    }
}