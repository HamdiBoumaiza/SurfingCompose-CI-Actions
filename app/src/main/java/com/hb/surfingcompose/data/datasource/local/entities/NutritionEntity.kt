package com.hb.surfingcompose.data.datasource.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "nutrition")
data class NutritionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fat: Int,
    val calories: Int,
    val sugar: Int,
    val carbohydrates: Int,
    val fiber: Int,
    val protein: Int
) {
    constructor() : this(0, 0, 0, 0, 0, 0, 0)
}