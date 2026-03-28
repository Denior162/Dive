package com.deep.dive.domain.model

import com.mapbox.geojson.Point

data class DiveSpot(
    val id: String,
    val name: String,
    val type: SpotType,
    val location: String,
    val coordinates: Point,
    val depth: IntRange
)
