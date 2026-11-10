package com.example.lab9_mapkit

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Bundle
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.lifecycle.ViewModelProvider
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

import android.os.SystemClock
import android.view.MotionEvent
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.mapview.MapView

@RunWith(AndroidJUnit4::class)
class MapScreenTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var activity: MainActivity
    private fun main(action: () -> Unit) = instrumentation.runOnMainSync(action)
    private fun launch() {
        activity = instrumentation.startActivitySync(Intent(instrumentation.targetContext, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) as MainActivity
        instrumentation.waitForIdleSync()
    }
    private fun current() {
        main { activity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).first() as MainActivity }
    }
    private fun waitFor(timeout: Long = 35_000, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeout
        while (System.currentTimeMillis() < deadline) {
            var success = false
            main { success = condition() }
            if (success) return
            Thread.sleep(200)
        }
        fail("Timed out waiting for app state")
    }
    private fun screenshot(name: String) {
        instrumentation.waitForIdleSync()
        Thread.sleep(800)
        val file = File(instrumentation.targetContext.getExternalFilesDir(null), "checks/$name.png")
        file.parentFile!!.mkdirs()
        instrumentation.uiAutomation.takeScreenshot().let { bitmap ->
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }
    private fun rotate() {
        val old = activity
        main { activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        Thread.sleep(2000)
        current()
        assertNotSame(old, activity)
    }
    private fun shell(command: String) {
        android.os.ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command)).use { it.readBytes() }
    }

    private fun model() = ViewModelProvider(activity)[PlacesViewModel::class.java]
    private fun controller(): PlacesMapController {
        val field = MainActivity::class.java.getDeclaredField("controller").apply { isAccessible = true }
        return field.get(activity) as PlacesMapController
    }
    private fun mapView(): MapView {
        val field = MainActivity::class.java.getDeclaredField("mapView").apply { isAccessible = true }
        return field.get(activity) as MapView
    }
    private fun touch(x: Float, y: Float, duration: Long = 80) {
        val downTime = SystemClock.uptimeMillis()
        val down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0)
        instrumentation.uiAutomation.injectInputEvent(down, true)
        down.recycle()
        Thread.sleep(duration)
        val up = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0)
        instrumentation.uiAutomation.injectInputEvent(up, true)
        up.recycle()
        Thread.sleep(700)
    }
    @Test fun loadsMapTiles() {
        assertTrue("MapKit key is required", BuildConfig.MAPKIT_API_KEY.isNotBlank())
        launch()
        waitFor(50_000) {
            val field = MainActivity::class.java.getDeclaredField("mapLoaded").apply { isAccessible = true }
            field.getBoolean(activity)
        }
        screenshot("map-tiles")
        main { activity.finish() }
    }

    @Test fun markersDialogsPersistenceAndRotation() {
        assertTrue("MapKit key is required", BuildConfig.MAPKIT_API_KEY.isNotBlank())
        launch()
        waitFor { model().state.value.loaded }
        assertTrue(model().state.value.places.size >= 5)
        var initialCamera = MapCamera()
        main { initialCamera = controller().camera() }
        assertEquals(55.0084, initialCamera.latitude, 0.001)
        assertEquals(82.9357, initialCamera.longitude, 0.001)
        assertEquals(12f, initialCamera.zoom, 0.01f)
        screenshot("preset-places")
        val station = Place.presets.first()
        main { controller().moveTo(MapCamera(station.latitude, station.longitude, 15f)) }
        Thread.sleep(1200)
        System.gc()
        Thread.sleep(500)
        var x = 0f; var y = 0f
        main {
            val view = mapView()
            val location = IntArray(2).also { view.getLocationOnScreen(it) }
            val point = view.mapWindow.worldToScreen(Point(station.latitude, station.longitude))!!
            x = location[0] + point.x
            y = location[1] + point.y - 15f
        }
        touch(x, y)
        waitFor { activity.supportFragmentManager.findFragmentByTag("place_card") != null }
        screenshot("place-card")
        main { (activity.supportFragmentManager.findFragmentByTag("place_card") as PlaceCardDialog).dismiss() }
        instrumentation.waitForIdleSync()
        main {
            val view = mapView()
            val location = IntArray(2).also { view.getLocationOnScreen(it) }
            x = location[0] + view.width * 0.65f
            y = location[1] + view.height * 0.65f
        }
        touch(x, y, 1400)
        waitFor { activity.supportFragmentManager.findFragmentByTag("add_place") != null }
        main {
            val dialog = (activity.supportFragmentManager.findFragmentByTag("add_place") as AddPlaceDialog).dialog as androidx.appcompat.app.AlertDialog
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick()
            assertNotNull(dialog.findViewById<android.widget.EditText>(R.id.placeNameInput)!!.error)
            dialog.findViewById<android.widget.EditText>(R.id.placeNameInput)!!.setText("Встреча у вокзала")
        }
        screenshot("new-place-dialog")
        main {
            val dialog = (activity.supportFragmentManager.findFragmentByTag("add_place") as AddPlaceDialog).dialog as androidx.appcompat.app.AlertDialog
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick()
        }
        waitFor { model().state.value.places.any { it.title == "Встреча у вокзала" } && !model().state.value.saving }
        val saved = PlaceStorage(instrumentation.targetContext).load()
        assertTrue(saved.any { it.title == "Встреча у вокзала" && it.custom })
        var camera = MapCamera()
        main { camera = controller().camera() }
        val count = model().state.value.places.size
        rotate()
        waitFor { model().state.value.places.size == count }
        main {
            val restored = controller().camera()
            assertEquals(camera.latitude, restored.latitude, 0.0001)
            assertEquals(camera.longitude, restored.longitude, 0.0001)
            assertEquals(camera.zoom, restored.zoom, 0.01f)
        }
        screenshot("rotated")
        main { activity.finish() }
        instrumentation.waitForIdleSync()
        launch()
        waitFor { model().state.value.loaded }
        assertTrue(model().state.value.places.any { it.title == "Встреча у вокзала" })
        screenshot("restored-place")
        main { activity.finish() }
    }
}
