package com.garrett.bitez.data.model.googleplaces

data class PlacePhoto(
    val name: String,
    val widthPx: Int,
    val heightPx: Int,
    val authorAttributions: List<AuthorAttribution>,
    val flagContentUri: String? = null,
    val googleMapsUri: String? = null
)