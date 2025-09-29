package com.garrett.bitez.data.model.googleplaces

data class Rectangle(
    val low: LatitudeLongitude,  // Southwest corner
    val high: LatitudeLongitude  // Northeast corner
)
