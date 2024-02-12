package com.example.placesapi.localdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NearbyPlace(
    val name: String,
    val latLng: String,
    @PrimaryKey(autoGenerate = false)
    val id: String = ""
)
