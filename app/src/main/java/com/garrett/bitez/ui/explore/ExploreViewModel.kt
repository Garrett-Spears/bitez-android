package com.garrett.bitez.ui.explore

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garrett.bitez.data.model.FoodLocation
import com.garrett.bitez.data.repository.FoodLocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.String

const val CITY_ZOOM_LEVEL: Float = 12f

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val foodLocationRepository: FoodLocationRepository
): ViewModel() {
    private val tag: String = this::class.java.simpleName

    // Keep track of whether map is ready to render
    private val _isMapReady: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val isMapReady: LiveData<Boolean> = _isMapReady

    // Keep track of position in map where camera should be
    private val _cameraPosition: MutableLiveData<CameraPosition?> = MutableLiveData<CameraPosition?>(null)
    val cameraPosition: LiveData<CameraPosition?> = _cameraPosition

    // Keep track of which locations on the map that are currently marked, or need to be marked
    private val pendingMarkedFoodLocations: MutableSet<FoodLocation> = mutableSetOf()
    private val markedFoodLocations: MutableSet<String> = mutableSetOf()

    // Keep track of page metadata for food locations
    private var currentFoodLocationSearchParams: FoodLocationSearchParams? = null
    private var isLoadingFoodLocations: Boolean = false
    private var isLastPage: Boolean = false

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

    fun markNewFoodLocations(foodLocations: List<FoodLocation>, map: GoogleMap?) {
        // Update pending list with all potential locations that need to be marked
        this.pendingMarkedFoodLocations.addAll(foodLocations)

        // If map is not ready yet then come around to mark locations when ready
        if (map == null) return

        // Check each food location, and if it hasn't been marked yet then mark it on the map
        for (foodLocation: FoodLocation in pendingMarkedFoodLocations) {
            if (!this.markedFoodLocations.contains(foodLocation.id)) {
                // Add the ID to the set so we don't mark it again
                this.markedFoodLocations.add(foodLocation.id)

                val foodLocationMarker: MarkerOptions = MarkerOptions()
                    .position(foodLocation.location)
                    .title(foodLocation.name)

                // Add new marker to the map
                map.addMarker(foodLocationMarker)
            }
        }
    }

    fun removeMarkedFoodLocations() {
        this.pendingMarkedFoodLocations.clear()
        this.markedFoodLocations.clear()
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

    fun fetchNextPageFoodLocations() {
        // Return to prevent duplicate API requests or if already fetched last page
        if (isLoadingFoodLocations || isLastPage) return

        // Get params needed for API call
        var foodLocationSearchParams: FoodLocationSearchParams? = this.currentFoodLocationSearchParams

        // If starting new food location search, get location to search from and keep track of state
        // to allow consistent pagination
        if (foodLocationSearchParams == null) {
            val currCameraPosition = this.cameraPosition.value

            // No location to fetch food places for
            if (currCameraPosition == null) return

            // Starting new search for this location, so keep track of search params
            foodLocationSearchParams = FoodLocationSearchParams(
                currLatLng = currCameraPosition.target,
                nextPageToken = null
            )
            this@ExploreViewModel.currentFoodLocationSearchParams = foodLocationSearchParams
        }

        // Launch coroutine to fetch data
        this.viewModelScope.launch {
            this@ExploreViewModel.isLoadingFoodLocations = true

            // Fetch next page of food locations for current location
            val nextPageFoodLocations: Pair<List<FoodLocation>, String?>? =
                this@ExploreViewModel.foodLocationRepository
                    .getNearbyFoodLocations(
                        foodLocationSearchParams.currLatLng,
                        foodLocationSearchParams.nextPageToken)

            // Error occurred
            if (nextPageFoodLocations == null) {
                Log.e(tag, "Error fetching page of food locations.")
                return@launch
            }
            // Append newly fetched data to mutable state flow
            else if (nextPageFoodLocations.first.isNotEmpty()) {
                this@ExploreViewModel._foodLocations.value =
                    this@ExploreViewModel._foodLocations.value + nextPageFoodLocations.first
            }

            // If next page token is null, then this is the last page
            if (nextPageFoodLocations.second == null) {
                this@ExploreViewModel.isLastPage = true
            }
            // If not-null, save state of next page token
            else {
                this@ExploreViewModel.currentFoodLocationSearchParams?.nextPageToken =
                    nextPageFoodLocations.second
            }

            this@ExploreViewModel.isLoadingFoodLocations = false
        }
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

data class FoodLocationSearchParams(
    val currLatLng: LatLng,
    var nextPageToken: String?
)
