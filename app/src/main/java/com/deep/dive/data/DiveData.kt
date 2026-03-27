package com.deep.dive.data

import com.deep.dive.domain.model.DiveSpot
import com.deep.dive.domain.model.SpotType
import com.mapbox.geojson.Point

object DiveData {
    val points: List<DiveSpot> = listOf(
        DiveSpot(
            id = "mizpah",
            name = "Mizpah/PC1170",
            type = SpotType.WRECK,
            location = "West Palm Beach",
            coordinates = Point.fromLngLat(-80.00968, 26.47178),
            depth = "90Ft"
        ),
        DiveSpot(
            id = "rodeo25",
            name = "Rodeo 25",
            type = SpotType.WRECK,
            location = "Pompano Beach",
            coordinates = Point.fromLngLat(-80.03813, 26.13878),
            depth = "90-122Ft"
        ),
        DiveSpot(
            id = "tortuga",
            name = "Tortuga",
            type = SpotType.WRECK,
            location = "Miami",
            coordinates = Point.fromLngLat(-80.04616, 25.53373),
            depth = "90-110Ft"
        ),
        DiveSpot(
            id = "zoo",
            name = "The Zoo",
            type = SpotType.REEF,
            location = "West Palm Beach",
            coordinates = Point.fromLngLat(-79.59320, 26.48190),
            depth = "86Ft"
        ),
        DiveSpot(
            id = "black_rock",
            name = "Black Rock",
            type = SpotType.REEF,
            location = "West Palm Beach",
            coordinates = Point.fromLngLat(-80.00157, 26.48826),
            depth = "85-100Ft"
        )
    )
}