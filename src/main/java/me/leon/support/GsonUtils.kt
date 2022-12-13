package me.leon.support

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken

object GsonUtils {
    private val gson: Gson = Gson()

    fun toJson(obj: Any): String = gson.toJson(obj)

    fun <D> fromJson(json: String?, clazz: Class<D>): D = gson.fromJson(json, clazz)

    fun <D> jsonToList(json: String, clazz: Class<Array<D>>): List<D> =
        gson.fromJson(json, clazz).toList()

    fun <D> jsonToArrayList(json: String, clazz: Class<D>): List<D> {
        return gson
            .fromJson<List<JsonObject>>(json, object : TypeToken<List<JsonObject>>() {}.type)
            .map { gson.fromJson(it, clazz) }
            .toList()
    }
}

inline fun <reified D> String.fromJson() = GsonUtils.fromJson(this, D::class.java)

inline fun <reified D> String.fromJsonArray() = GsonUtils.jsonToList(this, Array<D>::class.java)

inline fun <reified D> String.fromJsonList() = GsonUtils.jsonToArrayList(this, D::class.java)

fun Any.toJson() = GsonUtils.toJson(this)
