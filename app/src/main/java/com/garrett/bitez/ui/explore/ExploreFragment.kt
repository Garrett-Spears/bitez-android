package com.garrett.bitez.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.garrett.bitez.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

class ExploreFragment : Fragment() {
    private var mapView: MapView? = null

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
        mapView = view.findViewById<MapView>(R.id.explore_map)

        // Make start position of map
        val startPosition: CameraPosition = CameraPosition.Builder()
            .target(LatLng(28.5384, -81.3789)) // latitude & longitude
            .zoom(12f)
            .build()

        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(object : OnMapReadyCallback {
            override fun onMapReady(googleMap: GoogleMap) {
                // Configure map on creation here
                googleMap.uiSettings.isZoomControlsEnabled = true
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(startPosition))
            }
        })
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
}