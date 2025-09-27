package com.garrett.bitez.ui.explore

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.garrett.bitez.FragmentManagerMainActivity

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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task

class ExploreFragment : Fragment() {
    private val tag: String = this::class.java.simpleName

    // Create defaults for start position
    companion object {
        const val DEFAULT_START_LATITUDE: Double = 39.8283
        const val DEFAULT_START_LONGITUDE: Double = -98.5795
        const val DEFAULT_ZOOM_LEVEL: Float = 3f
        const val CITY_ZOOM_LEVEL: Float = 12f
    }

    // State variables to upload fragment upon opening
    private val _isLoading = MutableLiveData<Boolean>(true)
    private var isLoading: LiveData<Boolean> = _isLoading
    private var startLocation: Location? = null

    // Permission launcher to handle permission requests and results
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission(),
            object: ActivityResultCallback<Boolean> {
                override fun onActivityResult(result: Boolean) {
                    // User accepted permission request, so fetch last known location
                    if (result) {
                        getLastLocation()
                    }
                    // User denied permission request, so stop loading state without fetching location
                    else {
                        _isLoading.value = false
                    }
                }
        })

    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get ref to map from layout
        this.mapView = view.findViewById<MapView>(R.id.explore_map)
        this.mapView?.onCreate(savedInstanceState)

        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Handle map state once loading is done
        isLoading.observe(viewLifecycleOwner, Observer<Boolean> {
            this@ExploreFragment.initializeMap()
        })

        // Start process of checking location permission and fetching location potentially
        tryGetDeviceLocationThenInitializeMap()
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

    private fun initializeMap() {
        // Assign start latitude and longitude, assign to default position if start location
        // not available
        val startLatitude: Double = startLocation?.latitude ?: DEFAULT_START_LATITUDE
        val startLongitude: Double = startLocation?.longitude ?: DEFAULT_START_LONGITUDE
        val zoomLevel: Float = if (startLocation == null)  DEFAULT_ZOOM_LEVEL else CITY_ZOOM_LEVEL

        // Fetch map and initialize its state
        mapView?.getMapAsync(object : OnMapReadyCallback {
            override fun onMapReady(googleMap: GoogleMap) {
                // Configure map on creation here
                googleMap.uiSettings.isZoomControlsEnabled = true

                // Save ref to googleMap to use
                this@ExploreFragment.googleMap = googleMap

                moveCameraToPosition(startLatitude, startLongitude, zoomLevel)
            }
        })
    }

    // Function to fetch the last known location of device
    private fun getLastLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(tag, "Cannot call func to get last location without location permission.")
        }

        // Create async task to get location of device
        val getLocationTask: Task<Location?>? = fusedLocationProviderClient?.lastLocation

        // If task was launched successfully, add on complete listener to process result
        if (getLocationTask != null) {
            val onCompleteListener: OnCompleteListener<Location?> = object : OnCompleteListener<Location?> {
                override fun onComplete(task: Task<Location?>) {
                    // Found valid non-null location, so use it
                    if (task.isSuccessful && task.result != null) {
                        startLocation = task.result
                        Log.d(tag, "Location: ${task.result?.latitude}, ${task.result?.longitude}")
                    }

                    // Stop loading since task finished
                    _isLoading.value = false
                }
            }

            getLocationTask.addOnCompleteListener(onCompleteListener)
        }
    }

    private fun tryGetDeviceLocationThenInitializeMap() {
        // User has already given permission
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(tag, "Already have permission")
            getLastLocation()
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

    private fun moveCameraToPosition(latitude: Double, longitude: Double, zoomLevel: Float) {
        // Figure out position on map where camera should go
        val latitudeLongitudeDest: LatLng = LatLng(latitude, longitude)
        val cameraPosition: CameraPosition =  CameraPosition.Builder()
            .target(latitudeLongitudeDest)
            .zoom(zoomLevel)
            .build()
        val cameraUpdate: CameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)

        // Make camera update
        googleMap?.animateCamera(cameraUpdate)
    }
}