package com.hb.surfingcompose.data.datasource.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "instruction")
data class InstructionsEntity(@PrimaryKey val position: Int, val displayText: String)
