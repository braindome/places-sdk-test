package com.example.placesapi.localdb

import com.google.android.gms.maps.model.LatLng

data class NearbyPlaceState(
    val nearbyPlaces: List<NearbyPlace> = emptyList(),
    val name: String = "",
    val latLng: String = "",
    val sortType: SortType = SortType.NAME
)