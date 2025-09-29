package com.garrett.bitez.data.network.requests

import com.garrett.bitez.data.model.googleplaces.LocationRestriction

data class TextSearchRequest (
    val textQuery: String,
    val pageSize: Int,
    val includedType: String,
    var pageToken: String? = null, // Optional
    val locationRestriction: LocationRestriction
)


