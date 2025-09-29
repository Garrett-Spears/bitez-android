package com.garrett.bitez.data.model.googleplaces

import com.garrett.bitez.data.model.googleplaces.PlaceDisplayName
import com.garrett.bitez.data.model.googleplaces.PlaceLocation
import com.garrett.bitez.data.model.googleplaces.PlacePhoto

data class Place (
    val id: String,
    val displayName: PlaceDisplayName,
    val location: PlaceLocation,
    val photos: List<PlacePhoto>?
)