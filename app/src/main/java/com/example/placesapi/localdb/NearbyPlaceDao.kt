package com.example.placesapi.localdb

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NearbyPlaceDao {
    @Upsert
    suspend fun upsertNearbyPlace(nearbyPlace: NearbyPlace)

    @Query("SELECT * FROM nearbyplace ORDER BY name ASC")
    fun getNearbyPlacesOrderedByName(): Flow<List<NearbyPlace>>
}