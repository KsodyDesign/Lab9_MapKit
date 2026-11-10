package com.example.lab9_mapkit

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.lab9_mapkit.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.map.MapLoadedListener
import com.yandex.mapkit.mapview.MapView
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: PlacesViewModel by viewModels()
    private var mapView: MapView? = null
    private var controller: PlacesMapController? = null
    private val handler = Handler(Looper.getMainLooper())
    private var mapLoaded = false
    private val slowLoading = Runnable {
        if (!mapLoaded) binding.statusText.setText(R.string.loading_slow)
    }
    private val loadedListener = MapLoadedListener {
        mapLoaded = true
        handler.removeCallbacks(slowLoading)
        binding.statusText.text = getString(R.string.places_count, viewModel.state.value.places.size)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val configured = BuildConfig.MAPKIT_API_KEY.isNotBlank()
        if (configured) MapKitFactory.initialize(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets(binding.root)
        if (!configured) {
            binding.setupText.visibility = View.VISIBLE
            binding.statusText.setText(R.string.key_missing)
            listOf(binding.homeButton, binding.zoomIn, binding.zoomOut).forEach { it.isEnabled = false }
            return
        }
        val view = MapView(this)
        mapView = view
        binding.mapContainer.addView(view, 0, FrameLayout.LayoutParams(-1, -1))
        controller = PlacesMapController(view,
            onPlaceClick = { place ->
                if (supportFragmentManager.findFragmentByTag("place_card") == null) {
                    PlaceCardDialog.forPlace(place).show(supportFragmentManager, "place_card")
                }
            },
            onLongTap = { latitude, longitude ->
                val state = viewModel.state.value
                if (state.loaded && !state.saving && supportFragmentManager.findFragmentByTag("add_place") == null) {
                    AddPlaceDialog.at(latitude, longitude).show(supportFragmentManager, "add_place")
                }
            },
            onCameraChanged = { viewModel.camera = it })
        if (savedInstanceState?.containsKey("camera_lat") == true) {
            viewModel.camera = MapCamera(savedInstanceState.getDouble("camera_lat"),
                savedInstanceState.getDouble("camera_lon"), savedInstanceState.getFloat("camera_zoom"),
                savedInstanceState.getFloat("camera_azimuth"), savedInstanceState.getFloat("camera_tilt"))
        }
        controller?.moveTo(viewModel.camera)
        view.mapWindow.map.setMapLoadedListener(WeakReference(loadedListener))
        handler.postDelayed(slowLoading, 20_000)
        supportFragmentManager.setFragmentResultListener(AddPlaceDialog.RESULT, this) { _, result ->
            viewModel.addPlace(Place.userPlace(result.getString("title").orEmpty(),
                result.getDouble("latitude"), result.getDouble("longitude")))
        }
        binding.homeButton.setOnClickListener { controller?.moveTo(MapCamera()) }
        binding.zoomIn.setOnClickListener { controller?.zoom(1f) }
        binding.zoomOut.setOnClickListener { controller?.zoom(-1f) }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    controller?.render(state.places)
                    if (state.saving) binding.statusText.setText(R.string.saving)
                    else if (mapLoaded) binding.statusText.text = getString(R.string.places_count, state.places.size)
                    state.error?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        viewModel.consumeError()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (mapView != null) {
            MapKitFactory.getInstance().onStart()
            mapView?.onStart()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        controller?.camera()?.let { camera ->
            outState.putDouble("camera_lat", camera.latitude)
            outState.putDouble("camera_lon", camera.longitude)
            outState.putFloat("camera_zoom", camera.zoom)
            outState.putFloat("camera_azimuth", camera.azimuth)
            outState.putFloat("camera_tilt", camera.tilt)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        controller?.camera()?.let { viewModel.camera = it }
        mapView?.onStop()
        if (mapView != null) MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroy() {
        handler.removeCallbacks(slowLoading)
        controller?.dispose()
        super.onDestroy()
    }
}
