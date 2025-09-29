package com.garrett.bitez.data.model

import com.garrett.bitez.data.model.googleplaces.PlacePhoto
import com.google.android.gms.maps.model.LatLng

data class FoodLocation (
    val id: String,
    val name: String,
    val location: LatLng,
    val photo: PlacePhoto?
)
