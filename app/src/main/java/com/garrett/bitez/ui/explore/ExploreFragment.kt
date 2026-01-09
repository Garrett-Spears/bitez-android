package com.garrett.bitez.ui.explore

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.garrett.bitez.R
import com.garrett.bitez.data.model.FoodLocation
import com.garrett.bitez.ui.explore.foodlocations.FoodLocationsAdapter
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
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.min

// Create defaults for start position on map
const val DEFAULT_START_LATITUDE: Double = 39.8283
const val DEFAULT_START_LONGITUDE: Double = -98.5795
const val DEFAULT_ZOOM_LEVEL: Float = 3f

// Bottom sheet half-expanded height ration
const val BOTTOM_SHEET_HALF_EXPANDED_RATIO: Float = 0.5f

@AndroidEntryPoint
class ExploreFragment : Fragment() {
    private val tag: String = this::class.java.simpleName

    // Ref to viewModel
    private val exploreViewModel: ExploreViewModel by viewModels()

    // UI components
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var searchButton: MaterialButton

    // Locations service client
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // Bottom sheet fixed UI metrics
    private lateinit var bottomSheetMetrics: BottomSheetMetrics

    private lateinit var foodLocationsAdapter: FoodLocationsAdapter

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

        // Get ref to manual search button
        this.searchButton = view.findViewById<MaterialButton>(R.id.search_locations_button)

        // Add logic to hide button and manually search for food locations as an option
        this.searchButton.setOnClickListener {
            this.searchButton.visibility = View.INVISIBLE
            this.exploreViewModel.fetchNextPageFoodLocations()
        }

        // Check if map already has saved position to restore
        val mapHasSavedPosition: Boolean = this.exploreViewModel.cameraPositionAvailable()

        val bottomSheet: LinearLayout = view.findViewById<LinearLayout>(R.id.bottom_sheet)

        this.initializeBottomSheet(bottomSheet)
        this.initializeFoodLocationsList(bottomSheet)

        // Get ref to map from layout
        this.mapView = view.findViewById<MapView>(R.id.explore_map)
        this.mapView.onCreate(savedInstanceState)

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
    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { mapView.onPause(); super.onPause() }
    override fun onStop() { mapView.onStop(); super.onStop() }
    override fun onDestroyView() {
        this@ExploreFragment.exploreViewModel.removeMarkedFoodLocations()
        mapView.onDestroy();
        super.onDestroyView() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    // Function to shrink/grow map as bottom sheet is slid up and down
    private fun updateMapSize(currBottomSheetMetrics: BottomSheetMetrics, slideOffset: Float) {
        // Height of bottom sheet currently, get current percentage of its max possible height that it can move up
        val currentHeightBottomSheet: Float = slideOffset * currBottomSheetMetrics.heightOfFullyExpandedBottomSheet

        // Total height of bottom sheet when half-expanded, including fixed peek height
        val totalHeightOfHalfExpandedBottomSheet: Int = currBottomSheetMetrics.heightOfHalfExpandedBottomSheet + currBottomSheetMetrics.peekHeight

        // Total current height of bottom sheet, including fixed peek height
        val totalCurrentHeightBottomSheet: Int = currentHeightBottomSheet.toInt() + currBottomSheetMetrics.peekHeight

        // If bottom sheet height is <= half expanded height, set mapView to be fixed above bottom sheet
        // Otherwise, bottom sheet is above halfway point so set mapView to be fixed above half-expanded height of bottom sheet
        val bottomMapPadding: Int = min(totalCurrentHeightBottomSheet, totalHeightOfHalfExpandedBottomSheet)

        // Set bottom padding of map
        mapView.setPadding(0, 0, 0, bottomMapPadding)
    }

    // Helper function to lazy-calc bottom sheet data or return cached data if avail
    private fun getCurrentBottomSheetMetrics(bottomSheet: View): BottomSheetMetrics {
        // If bottom sheet sizes have not been calculated yet, calculate them and cache
        // them for future reuse
        if (!::bottomSheetMetrics.isInitialized) {
            this@ExploreFragment.bottomSheetMetrics = BottomSheetMetrics.calcBottomSheetMetrics(bottomSheet)
        }

        return this@ExploreFragment.bottomSheetMetrics
    }

    // Initializes UI for bottom sheet
    private fun initializeBottomSheet(bottomSheet: LinearLayout) {
        // Get behavior of bottom sheet to define
        val bottomSheetBehavior: BottomSheetBehavior<LinearLayout> = BottomSheetBehavior.from(bottomSheet)

        // Set percentage where bottom sheet should stop to half expand
        bottomSheetBehavior.halfExpandedRatio = BOTTOM_SHEET_HALF_EXPANDED_RATIO


        // Set initial state of bottom sheet
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED

        // Add callback to shrink/expand map as bottom sheet is slid up and down
        bottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Do nothing for bottom sheet state changes for now
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Update size of map to shrink/grow accordingly
                updateMapSize(getCurrentBottomSheetMetrics(bottomSheet), slideOffset)
            }
        })

        // Callback to run once on layout of bottom sheet to update map initially
        bottomSheet.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Remove listener to avoid multiple calls
                bottomSheet.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val currBottomSheetMetrics: BottomSheetMetrics = getCurrentBottomSheetMetrics(bottomSheet)

                // Figure out where bottom sheet state is currently to get start slide offset
                val startSlideOffset: Float = when (BottomSheetBehavior.from(bottomSheet).state) {
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> currBottomSheetMetrics.halfSlideOffset
                    BottomSheetBehavior.STATE_EXPANDED -> 1f
                    else -> 0f
                }

                // Update size of map to adjust to initial state of bottom view
                updateMapSize(currBottomSheetMetrics, startSlideOffset)
            }
        })
    }

    // Initializes recyclerview and data for food locations
    private fun initializeFoodLocationsList(bottomSheet: LinearLayout) {
        val foodLocationsRecyclerView: RecyclerView =
            bottomSheet.findViewById<RecyclerView>(R.id.food_locations_recycler_view)

        // Create adapter for recyclerView
        this.foodLocationsAdapter = FoodLocationsAdapter()

        // Set adapter and layout manager for RV
        foodLocationsRecyclerView.adapter = this.foodLocationsAdapter
        foodLocationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Start coroutine to listen for new data from flow in viewModel
        viewLifecycleOwner.lifecycleScope.launch {
            // Only run when fragment is in STARTED state and not STOPPED
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                this@ExploreFragment.exploreViewModel.foodLocations.collectLatest {
                        newFoodLocations: List<FoodLocation> ->
                    foodLocationsAdapter.submitList(newFoodLocations)

                    // Pass null ref if map is not ready yet
                    val googleMapRef: GoogleMap? = if (::googleMap.isInitialized) googleMap else null

                    // If there are no food locations currently fetched, then clear any stale
                    // markers before adding any new ones
                    if (this@ExploreFragment.exploreViewModel.foodLocations.value.isEmpty()) {
                        googleMapRef?.clear()

                        // Show manual search button when no places showing up
                        this@ExploreFragment.searchButton.visibility = View.VISIBLE
                    }
                    else {
                        // Hide button when locations are showing up
                        this@ExploreFragment.searchButton.visibility = View.INVISIBLE
                    }

                    this@ExploreFragment.exploreViewModel
                        .markNewFoodLocations(newFoodLocations, googleMapRef)
                }
            }
        }

        // Add listener to fetch next page of data when close to end of list
        foodLocationsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val layoutManager: LinearLayoutManager = rv.layoutManager as LinearLayoutManager
                val totalItemCount: Int = layoutManager.itemCount
                val lastVisible: Int = layoutManager.findLastVisibleItemPosition()

                // If less than 2 items from end of list, fetch next page
                if (lastVisible >= totalItemCount - 2) {
                    this@ExploreFragment.exploreViewModel.fetchNextPageFoodLocations()
                }
            }
        })
    }

    private fun initializeMap(restoringSavedPosition: Boolean) {
        var cameraPosition: CameraPosition? = exploreViewModel.cameraPosition.value

        // If could not find location, just build a default camera position
        if (cameraPosition == null) {
            cameraPosition = this.buildDefaultCameraPosition()
        }

        // Load nearby location data for current location on initialization
        this.exploreViewModel.fetchNextPageFoodLocations()

        // Fetch map and initialize its state
        mapView.getMapAsync(object : OnMapReadyCallback {
            override fun onMapReady(googleMap: GoogleMap) {
                // Configure map on creation here
                googleMap.uiSettings.isZoomControlsEnabled = true

                // Save ref to googleMap to use
                this@ExploreFragment.googleMap = googleMap

                // Initially move camera upon render
                moveCameraToPosition(cameraPosition, restoringSavedPosition)

                // Mark any food locations on the map that arrived before map was ready
                this@ExploreFragment.exploreViewModel.markNewFoodLocations(emptyList(), googleMap)

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
            googleMap.moveCamera(cameraUpdate)
        }
        // Otherwise, move camera with an animation
        else {
            googleMap.animateCamera(cameraUpdate)
        }
    }
}
