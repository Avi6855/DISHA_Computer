package com.avinashpatil.app.dishacomputer.data.converters

import androidx.room.TypeConverter
import com.avinashpatil.app.dishacomputer.api.models.BatchDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        if (value == null) return null
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        if (list == null) return null
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromBatchDtoList(value: String?): List<BatchDto>? {
        if (value == null) return null
        val listType = object : TypeToken<List<BatchDto>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toBatchDtoList(list: List<BatchDto>?): String? {
        if (list == null) return null
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromQuizQuestionDTOList(value: String?): List<com.avinashpatil.app.dishacomputer.api.models.QuizQuestionDTO>? {
        if (value == null) return null
        val listType = object : TypeToken<List<com.avinashpatil.app.dishacomputer.api.models.QuizQuestionDTO>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toQuizQuestionDTOList(list: List<com.avinashpatil.app.dishacomputer.api.models.QuizQuestionDTO>?): String? {
        if (list == null) return null
        return gson.toJson(list)
    }
}
