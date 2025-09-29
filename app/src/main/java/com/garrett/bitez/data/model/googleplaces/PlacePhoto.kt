package com.garrett.bitez.data.model.googleplaces

import com.garrett.bitez.BuildConfig

data class PlacePhoto(
    val name: String,
    val widthPx: Int,
    val heightPx: Int,
    val authorAttributions: List<AuthorAttribution>,
    val flagContentUri: String? = null,
    val googleMapsUri: String? = null
) {
    fun getPhotoUrl(): String {
        return "https://places.googleapis.com/v1/$name/media?key=${BuildConfig.GOOGLE_REST_API_KEY}&maxHeightPx=${heightPx}&maxWidthPx=${widthPx}"
    }
}