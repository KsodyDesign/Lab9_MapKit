package com.example.lab9_mapkit

import java.util.UUID

data class Place(
    val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val custom: Boolean = false
) {
    init {
        require(id.isNotBlank() && title.isNotBlank())
        require(latitude.isFinite() && latitude in -90.0..90.0)
        require(longitude.isFinite() && longitude in -180.0..180.0)
    }

    companion object {
        fun userPlace(title: String, latitude: Double, longitude: Double) = Place(
            UUID.randomUUID().toString(), title.trim(), latitude, longitude,
            "Точка, добавленная на карту пользователем.", true)

        val presets = listOf(
            Place("station", "Главный вокзал", 55.0359, 82.8960, "Железнодорожный вокзал Новосибирск-Главный."),
            Place("square", "Площадь Ленина", 55.0302, 82.9204, "Центральная площадь Новосибирска."),
            Place("novat", "Театр оперы и балета", 55.0304, 82.9250, "Новосибирский государственный академический театр оперы и балета."),
            Place("museum", "Краеведческий музей", 55.0289, 82.9207, "Исторический отдел Новосибирского государственного краеведческого музея."),
            Place("river", "Михайловская набережная", 55.0076, 82.9371, "Прогулочная набережная на берегу Оби.")
        )
    }
}

data class MapCamera(val latitude: Double = 55.0084, val longitude: Double = 82.9357,
    val zoom: Float = 12f, val azimuth: Float = 0f, val tilt: Float = 0f)
