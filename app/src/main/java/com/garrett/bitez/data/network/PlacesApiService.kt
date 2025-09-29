package com.garrett.bitez.data.network

import com.garrett.bitez.data.model.FoodLocation
import com.garrett.bitez.data.network.requests.TextSearchRequest
import com.garrett.bitez.data.network.responses.TextSearchResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

const val PLACES_TEXT_SEARCH_FIELD_MASK: String =
    "nextPageToken,places.id,places.displayName,places.location,places.photos"

interface PlacesApiService {
    @POST("v1/places:searchText")
    suspend fun textSearchPlaces(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Header("X-Goog-FieldMask") fieldMask: String = PLACES_TEXT_SEARCH_FIELD_MASK,
        @Body textSearchRequest: TextSearchRequest
    ): TextSearchResponse
}
