package com.example.placesapi.localdb

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NearbyPlace::class],
    version = 1
)
abstract class NearbyPlaceDatabase : RoomDatabase() {

    abstract val dao: NearbyPlaceDao

}