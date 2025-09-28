package com.garrett.bitez.data.model

import com.google.android.gms.maps.model.LatLng

data class FoodLocation (
    val id: String,
    val name: String,
    val address: String,
    val latLng: LatLng,
    val googleMapsRating: Double,
    val photoURL: String
)
