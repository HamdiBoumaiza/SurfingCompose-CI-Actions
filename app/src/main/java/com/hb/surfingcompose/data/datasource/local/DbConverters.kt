package com.hb.surfingcompose.data.datasource.local

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.hb.surfingcompose.data.datasource.local.entities.InstructionsEntity
import com.hb.surfingcompose.data.datasource.local.entities.NutritionEntity
import com.hb.surfingcompose.data.datasource.local.entities.TagsEntity

@ProvidedTypeConverter
class DbConverters {

    private val gsonParser: GsonParser = GsonParser()

    @TypeConverter
    fun toInstructionList(instructions: String): List<InstructionsEntity> {
        return gsonParser.fromJson<List<InstructionsEntity>>(
            instructions,
            object : TypeToken<List<InstructionsEntity>>() {}.type
        ) ?: emptyList()
    }

    @TypeConverter
    fun toInstructionString(instructions: List<InstructionsEntity>?): String {
        return gsonParser.toJson(
            instructions,
            object : TypeToken<List<InstructionsEntity>>() {}.type
        ) ?: "[]"
    }


    @TypeConverter
    fun toTagsList(tags: String): List<TagsEntity> {
        return gsonParser.fromJson<List<TagsEntity>>(
            tags,
            object : TypeToken<List<TagsEntity>>() {}.type
        ) ?: emptyList()
    }

    @TypeConverter
    fun toTagsString(tags: List<TagsEntity>?): String {
        return gsonParser.toJson(
            tags,
            object : TypeToken<List<TagsEntity>>() {}.type
        ) ?: "[]"
    }

    @TypeConverter
    fun toNutrition(nutrition: String): NutritionEntity? {
        return gsonParser.fromJson<NutritionEntity>(
            nutrition,
            object : TypeToken<NutritionEntity>() {}.type
        )
    }

    @TypeConverter
    fun toNutritionString(nutrition: NutritionEntity?): String {
        return gsonParser.toJson(
            nutrition,
            object : TypeToken<NutritionEntity>() {}.type
        ) ?: ""
    }
}