package com.garrett.bitez.ui.explore

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer

import com.garrett.bitez.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior

// Create defaults for start position
const val DEFAULT_START_LATITUDE: Double = 39.8283
const val DEFAULT_START_LONGITUDE: Double = -98.5795
const val DEFAULT_ZOOM_LEVEL: Float = 3f

class ExploreFragment : Fragment() {
    private val tag: String = this::class.java.simpleName

    // Ref to viewModel
    private val exploreViewModel: ExploreViewModel by viewModels()

    // UI components
    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null

    // Locations service client
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    // Permission launcher to handle permission requests and results
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission(),
            object: ActivityResultCallback<Boolean> {
                override fun onActivityResult(result: Boolean) {
                    // User accepted permission request, so fetch last known location
                    if (result) {
                        exploreViewModel.getLastLocation(requireContext(), fusedLocationProviderClient)
                    }
                    // User denied permission request, so go ahead and render map with default state
                    else {
                        exploreViewModel.setIsMapReady(true)
                    }
                }
        })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.exploreViewModel.setIsMapReady(false)

        // Check if map already has saved position to restore
        val mapHasSavedPosition: Boolean = this.exploreViewModel.cameraPositionAvailable()

        val bottomSheet: LinearLayout = view.findViewById<LinearLayout>(R.id.bottom_sheet)
        val bottomSheetBehavior: BottomSheetBehavior<LinearLayout> = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.halfExpandedRatio = 0.5f
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED

        // Get ref to map from layout
        this.mapView = view.findViewById<MapView>(R.id.explore_map)
        this.mapView?.onCreate(savedInstanceState)

        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Handle map initialization once map is ready to render
        this.exploreViewModel.isMapReady.observe(viewLifecycleOwner, object: Observer<Boolean> {
            override fun onChanged(value: Boolean) {
                // Only init map when "isMapReady" set to true
                if (value) this@ExploreFragment.initializeMap(mapHasSavedPosition)
            }
        })

        // If already have saved camera position, set map as ready to render
        if (mapHasSavedPosition) {
            this.exploreViewModel.setIsMapReady(true)
        }
        // Otherwise, start process of checking location permission and fetching location potentially
        else {
            tryGetDeviceLocationThenInitializeMap()
        }
    }

    // Forward fragment lifecycle to MapView
    override fun onStart() { super.onStart(); mapView?.onStart() }
    override fun onResume() { super.onResume(); mapView?.onResume() }
    override fun onPause() { mapView?.onPause(); super.onPause() }
    override fun onStop() { mapView?.onStop(); super.onStop() }
    override fun onDestroyView() { mapView?.onDestroy(); super.onDestroyView() }
    override fun onLowMemory() { super.onLowMemory(); mapView?.onLowMemory() }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    private fun initializeMap(restoringSavedPosition: Boolean) {
        var cameraPosition: CameraPosition? = exploreViewModel.cameraPosition.value

        // If could not find location, just build a default camera position
        if (cameraPosition == null) {
            cameraPosition = this.buildDefaultCameraPosition()
        }

        // Fetch map and initialize its state
        mapView?.getMapAsync(object : OnMapReadyCallback {
            override fun onMapReady(googleMap: GoogleMap) {
                // Configure map on creation here
                googleMap.uiSettings.isZoomControlsEnabled = true

                // Save ref to googleMap to use
                this@ExploreFragment.googleMap = googleMap

                // Initially move camera upon render
                moveCameraToPosition(cameraPosition, restoringSavedPosition)

                // Set listener to save camera position upon each position change
                googleMap.setOnCameraIdleListener(object : GoogleMap.OnCameraIdleListener {
                    override fun onCameraIdle() {
                        val newCameraPosition: CameraPosition = googleMap.cameraPosition
                        this@ExploreFragment.exploreViewModel.setCameraPosition(newCameraPosition)
                    }
                })
            }
        })
    }

    // Builds default camera position when no location is available
    private fun buildDefaultCameraPosition(): CameraPosition {
        val defaultLatLng: LatLng = LatLng(DEFAULT_START_LATITUDE, DEFAULT_START_LONGITUDE)
        return CameraPosition.Builder()
            .target(defaultLatLng)
            .zoom(DEFAULT_ZOOM_LEVEL)
            .build()
    }

    private fun tryGetDeviceLocationThenInitializeMap() {
        // User has already given permission
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            this.exploreViewModel.getLastLocation(requireContext(), fusedLocationProviderClient)
        }
        // Request location permission if not granted
        else {
            requestLocationPermission()
        }
    }

    // Function to request location permission
    private fun requestLocationPermission() {
        val permissionStr: String = Manifest.permission.ACCESS_FINE_LOCATION

        // Need to ask for location permission
        requestPermissionLauncher.launch(permissionStr)
    }

    private fun moveCameraToPosition(cameraPosition: CameraPosition, restoringSavedPosition: Boolean) {
        val cameraUpdate: CameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)

        // If restoring saved position, do not animate camera (want to immediately restore state)
        if (restoringSavedPosition) {
            googleMap?.moveCamera(cameraUpdate)
        }
        // Otherwise, move camera with an animation
        else {
            googleMap?.animateCamera(cameraUpdate)
        }
    }
}