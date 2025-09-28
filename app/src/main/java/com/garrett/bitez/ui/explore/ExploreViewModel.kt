package com.garrett.bitez.ui.explore

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.garrett.bitez.data.model.FoodLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

const val CITY_ZOOM_LEVEL: Float = 12f

class ExploreViewModel : ViewModel() {
    private val tag: String = this::class.java.simpleName

    // Keep track of whether map is ready to render
    private val _isMapReady: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val isMapReady: LiveData<Boolean> = _isMapReady

    // Keep track of position in map where camera should be
    private val _cameraPosition: MutableLiveData<CameraPosition?> = MutableLiveData<CameraPosition?>(null)
    val cameraPosition: LiveData<CameraPosition?> = _cameraPosition

    // List of locations on map to display in list
    private val _foodLocations: MutableStateFlow<List<FoodLocation>> =
        MutableStateFlow<List<FoodLocation>>(emptyList())
    val foodLocations: StateFlow<List<FoodLocation>> = _foodLocations

    fun setIsMapReady(isMapReady: Boolean) {
        this._isMapReady.value = isMapReady
    }

    fun setCameraPosition(cameraPosition: CameraPosition?) {
        this._cameraPosition.value = cameraPosition
    }

    // Overloaded setCamera position given necessary components needed to build one
    fun setCameraPosition(location: Location, zoomLevel: Float) {
        val targetLatLng: LatLng = LatLng(location.latitude, location.longitude)
        val cameraPosition: CameraPosition =  CameraPosition.Builder()
            .target(targetLatLng)
            .zoom(zoomLevel)
            .build()
        this.setCameraPosition(cameraPosition)
    }

    fun cameraPositionAvailable(): Boolean {
        return this.cameraPosition.value != null
    }

    // Function to fetch the last known location of device
     fun getLastLocation(context: Context, fusedLocationProviderClient: FusedLocationProviderClient?) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(tag, "Cannot call func to get last location without location permission.")
        }

        // Create async task to get location of device
        val getLocationTask: Task<Location?>? = fusedLocationProviderClient?.lastLocation

        // If task was launched successfully, add on complete listener to process result
        if (getLocationTask != null) {
            val onCompleteListener: OnCompleteListener<Location?> = object : OnCompleteListener<Location?> {
                override fun onComplete(task: Task<Location?>) {
                    // Found valid non-null location, so use it
                    if (task.isSuccessful) {
                        val location: Location? = task.result
                        if (location != null) {
                            this@ExploreViewModel.setCameraPosition(location, CITY_ZOOM_LEVEL)
                            Log.d(
                                tag,
                                "Fetched location: ${location.latitude}, ${location.longitude}"
                            )
                        }
                    }

                    // Map ready to render
                    this@ExploreViewModel.setIsMapReady(true)
                }
            }

            getLocationTask.addOnCompleteListener(onCompleteListener)
        }
    }
}