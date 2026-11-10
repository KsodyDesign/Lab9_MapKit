package com.example.lab9_mapkit

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlacesState(val places: List<Place> = Place.presets, val saving: Boolean = false,
    val loaded: Boolean = false, val error: String? = null)

class PlacesViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = PlaceStorage(application)
    private val mutableState = MutableStateFlow(PlacesState())
    val state = mutableState.asStateFlow()
    var camera = MapCamera()

    init {
        viewModelScope.launch {
            try {
                val places = withContext(Dispatchers.IO) { storage.load() }
                mutableState.value = PlacesState(Place.presets + places, loaded = true)
                Log.d("Places", "Restored ${places.size} user places")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.value = PlacesState(loaded = true,
                    error = "Не удалось прочитать сохранённые точки.")
            }
        }
    }

    fun addPlace(place: Place) {
        val current = mutableState.value
        if (!current.loaded || current.saving) return
        mutableState.value = current.copy(saving = true, error = null)
        viewModelScope.launch {
            try {
                val places = current.places + place
                withContext(Dispatchers.IO) { storage.save(places) }
                mutableState.value = PlacesState(places, loaded = true)
                Log.d("Places", "Saved user place")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.value = current.copy(error = "Не удалось сохранить точку. Попробуйте ещё раз.")
            }
        }
    }

    fun consumeError() { mutableState.value = mutableState.value.copy(error = null) }
}
