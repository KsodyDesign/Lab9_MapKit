package com.example.lab9_mapkit

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.Log
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.TextStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import java.lang.ref.WeakReference

class PlacesMapController(
    private val mapView: MapView,
    private val onPlaceClick: (Place) -> Unit,
    private val onLongTap: (Double, Double) -> Unit,
    private val onCameraChanged: (MapCamera) -> Unit
) {
    private val map = mapView.mapWindow.map
    private val markers = map.mapObjects.addCollection()
    private var visiblePlaces: List<Place> = emptyList()
    private val presetIcon = pin(Color.rgb(36, 99, 90))
    private val customIcon = pin(Color.rgb(208, 111, 28))

    // MapKit keeps weak references, so listeners live as long as the controller.
    private val tapListener = MapObjectTapListener { obj, _ ->
        val place = obj.userData as? Place
        if (place != null) {
            Log.d("PlacesMap", "Placemark tapped: ${place.id}")
            onPlaceClick(place)
            true
        } else false
    }
    private val inputListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) = Unit
        override fun onMapLongTap(map: Map, point: Point) {
            Log.d("PlacesMap", "Long tap: ${point.latitude}, ${point.longitude}")
            onLongTap(point.latitude, point.longitude)
        }
    }
    private val cameraListener = CameraListener { _, position, _, _ ->
        onCameraChanged(position.asModel())
    }
    private val inputReference = WeakReference<InputListener>(inputListener)
    private val cameraReference = WeakReference(cameraListener)
    private val tapReference = WeakReference(tapListener)

    init {
        map.addInputListener(inputReference)
        map.addCameraListener(cameraReference)
    }

    fun render(places: List<Place>) {
        if (places == visiblePlaces) return
        markers.clear()
        places.forEach { place ->
            markers.addPlacemark().apply {
                geometry = Point(place.latitude, place.longitude)
                userData = place
                setIcon(if (place.custom) customIcon else presetIcon)
                setIconStyle(IconStyle().apply { anchor = PointF(0.5f, 1f); scale = 0.7f })
                setText(place.title, TextStyle().apply { size = 11f; offset = 4f })
                addTapListener(tapReference)
            }
        }
        visiblePlaces = places.toList()
    }

    fun moveTo(camera: MapCamera) {
        map.move(CameraPosition(Point(camera.latitude, camera.longitude), camera.zoom, camera.azimuth, camera.tilt))
    }

    fun camera(): MapCamera = map.cameraPosition.asModel()
    fun zoom(delta: Float) { moveTo(camera().let { it.copy(zoom = (it.zoom + delta).coerceIn(2f, 20f)) }) }

    fun dispose() {
        map.removeInputListener(inputReference)
        map.removeCameraListener(cameraReference)
        markers.clear()
    }

    private fun CameraPosition.asModel() = MapCamera(target.latitude, target.longitude, zoom, azimuth, tilt)

    private fun pin(color: Int): ImageProvider {
        val bitmap = Bitmap.createBitmap(64, 80, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        val point = Path().apply { moveTo(9f, 39f); lineTo(32f, 77f); lineTo(55f, 39f); close() }
        canvas.drawPath(point, paint)
        canvas.drawCircle(32f, 29f, 27f, paint)
        paint.color = Color.WHITE
        canvas.drawCircle(32f, 29f, 10f, paint)
        return ImageProvider.fromBitmap(bitmap)
    }
}
