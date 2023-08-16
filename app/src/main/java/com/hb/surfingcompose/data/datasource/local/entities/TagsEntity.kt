package com.hb.surfingcompose.data.datasource.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hb.surfingcompose.domain.models.TagsModel


@Entity(tableName = "tag")
data class TagsEntity(@PrimaryKey val displayName: String, val type: String)