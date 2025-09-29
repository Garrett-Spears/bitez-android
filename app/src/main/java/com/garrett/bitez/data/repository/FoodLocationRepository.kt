package com.garrett.bitez.data.repository

import android.util.Log
import com.garrett.bitez.BuildConfig
import com.garrett.bitez.data.model.FoodLocation
import com.garrett.bitez.data.model.googleplaces.LatitudeLongitude
import com.garrett.bitez.data.model.googleplaces.LocationRestriction
import com.garrett.bitez.data.model.googleplaces.Rectangle
import com.garrett.bitez.data.network.PlacesApiService
import com.garrett.bitez.data.network.RetrofitClient
import com.garrett.bitez.data.network.requests.TextSearchRequest
import com.garrett.bitez.data.network.responses.TextSearchResponse
import com.google.android.gms.maps.model.LatLng
import kotlin.math.cos

const val DEFAULT_NEARBY_LOCATIONS_PAGE_SIZE: Int = 5

// Offset in each direction to restrict search
const val LATITUDE_OFFSET_METERS: Double = 5000.0
const val LONGITUDE_OFFSET_METERS: Double = 5000.0

const val METERS_PER_DEGREE_LAT: Double = 111_000.0

class FoodLocationRepository {
    private val tag: String = this::class.java.simpleName

    private val api: PlacesApiService = RetrofitClient.placesApiService

    // Return list of nearby food locations if available
    suspend fun getNearbyFoodLocations(currLatLng: LatLng, nextPageToken: String?): Pair<List<FoodLocation>, String?>? {
        // Get center of where to search from
        val currLatitude: Double = currLatLng.latitude
        val currLongitude: Double = currLatLng.longitude

        // Calculate latitude and longitude offsets to search from
        val latOffset = LATITUDE_OFFSET_METERS / METERS_PER_DEGREE_LAT
        val lngOffset = LONGITUDE_OFFSET_METERS / (METERS_PER_DEGREE_LAT * cos(Math.toRadians(currLatitude)))

        // Get southwest corner of rectangle to search from
        val lowLat = currLatitude - latOffset
        val lowLng = currLongitude - lngOffset

        // Get northeast corner of rectangle to search from
        val highLat = currLatitude + latOffset
        val highLng = currLongitude + lngOffset

        val textSearchRequest: TextSearchRequest = TextSearchRequest(
            textQuery = "coffee",
            includedType = "cafe",
            pageSize = DEFAULT_NEARBY_LOCATIONS_PAGE_SIZE,
            locationRestriction = LocationRestriction(
                rectangle = Rectangle(
                    low = LatitudeLongitude(lowLat, lowLng),
                    high = LatitudeLongitude(highLat, highLng)
                )
            )
        )

        // If next page token passed in, then use it
        if (nextPageToken != null && nextPageToken.isNotEmpty()) {
            textSearchRequest.pageToken = nextPageToken
        }

        // Try fetching data from API
        val response: TextSearchResponse
        try {
            response = api.textSearchPlaces(
                apiKey = BuildConfig.GOOGLE_REST_API_KEY,
                textSearchRequest = textSearchRequest
            )
        }
        catch (e: Exception) {
            Log.e(tag, "Error fetching food locations: ${e.toString()}")
            return null
        }

        // Map each google place object to a local food location object
        val foodLocations: List<FoodLocation> = response.places.map { place ->
            FoodLocation(
                id = place.id,
                name = place.displayName.text,
                location = LatLng(place.location.latitude, place.location.longitude),
                photo = place.photos?.firstOrNull() // Only use first photo if available
            )
        }

        // Return list of results with potential next page token
        return Pair(foodLocations, response.nextPageToken)
    }
}