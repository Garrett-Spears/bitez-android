package com.garrett.bitez.data.network.responses

import com.garrett.bitez.data.model.googleplaces.Place

data class TextSearchResponse (
    val nextPageToken: String?,
    val places: List<Place>?
)
