package com.example.lab9_mapkit

import org.junit.Assert.*
import org.junit.Test

class PlaceTest {
    @Test fun hasFiveDistinctPresetPlaces() {
        assertTrue(Place.presets.size >= 5)
        assertEquals(Place.presets.size, Place.presets.map { it.id }.distinct().size)
        assertTrue(Place.presets.all { it.description.isNotBlank() && !it.custom })
    }
    @Test fun userPlaceTrimsNameAndHasOwnId() {
        val first = Place.userPlace("  Встреча  ", 55.01, 82.93)
        val second = Place.userPlace("Встреча", 55.01, 82.93)
        assertEquals("Встреча", first.title)
        assertTrue(first.custom)
        assertNotEquals(first.id, second.id)
    }
    @Test fun initialCameraMatchesAssignment() {
        assertEquals(55.0084, MapCamera().latitude, 0.0)
        assertEquals(82.9357, MapCamera().longitude, 0.0)
        assertEquals(12f, MapCamera().zoom, 0f)
    }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsBlankName() { Place.userPlace("  ", 55.0, 82.0) }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidLatitude() { Place.userPlace("Точка", 91.0, 82.0) }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsNaNLongitude() { Place.userPlace("Точка", 55.0, Double.NaN) }
}
