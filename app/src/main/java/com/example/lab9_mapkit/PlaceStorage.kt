package com.example.lab9_mapkit

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class PlaceStorage(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("places", Context.MODE_PRIVATE)

    fun load(): List<Place> {
        val array = JSONArray(preferences.getString("user_places", "[]"))
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            Place(item.getString("id"), item.getString("title"), item.getDouble("latitude"),
                item.getDouble("longitude"), item.optString("description", "Пользовательская точка"), true)
        }
    }

    fun save(places: List<Place>) {
        val array = JSONArray()
        places.filter { it.custom }.forEach { place ->
            array.put(JSONObject().apply {
                put("id", place.id)
                put("title", place.title)
                put("latitude", place.latitude)
                put("longitude", place.longitude)
                put("description", place.description)
            })
        }
        if (!preferences.edit().putString("user_places", array.toString()).commit()) {
            throw IOException("Не удалось сохранить точки")
        }
    }
}
