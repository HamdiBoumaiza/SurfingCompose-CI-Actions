package com.hb.surfingcompose.data.datasource.local

import com.google.gson.Gson
import java.lang.reflect.Type

class GsonParser {
    private val gson: Gson = Gson()
    fun <T> fromJson(json: String, type: Type): T? {
        return gson.fromJson(json, type)
    }

    fun <T> toJson(obj: T, type: Type): String? {
        return gson.toJson(obj, type)
    }
}