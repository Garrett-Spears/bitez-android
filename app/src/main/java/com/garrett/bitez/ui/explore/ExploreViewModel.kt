package com.garrett.bitez.ui.explore

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task

class ExploreViewModel : ViewModel() {
    private val tag: String = this::class.java.simpleName

    // Keep track of initial map loading
    private val _isLoading = MutableLiveData<Boolean>(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _startLocation = MutableLiveData<Location?>(null)
    val startLocation: LiveData<Location?> = _startLocation

    fun setIsLoading(isLoading: Boolean) {
        this._isLoading.value = isLoading
    }

    fun setStartLocation(location: Location?) {
        this._startLocation.value = location
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
                    if (task.isSuccessful && task.result != null) {
                        this@ExploreViewModel.setStartLocation(task.result)
                        Log.d(tag, "Fetched last location: ${task.result?.latitude}, ${task.result?.longitude}")
                    }

                    // Stop loading since task finished
                    this@ExploreViewModel.setIsLoading(false);
                }
            }

            getLocationTask.addOnCompleteListener(onCompleteListener)
        }
    }
}