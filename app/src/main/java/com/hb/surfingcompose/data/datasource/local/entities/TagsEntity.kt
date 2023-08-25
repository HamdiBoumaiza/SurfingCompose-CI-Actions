package com.hb.surfingcompose.data.datasource.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tag")
data class TagsEntity(@PrimaryKey val displayName: String, val type: String)