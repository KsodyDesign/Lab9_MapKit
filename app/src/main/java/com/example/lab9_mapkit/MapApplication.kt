package com.example.lab9_mapkit

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class MapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.MAPKIT_API_KEY.isNotBlank()) {
            MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        }
    }
}
