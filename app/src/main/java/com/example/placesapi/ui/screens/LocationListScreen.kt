package com.example.placesapi.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.placesapi.localdb.NearbyPlaceState


const val TAG = "LocationListScreen"
@Composable
fun LocationListScreen(state: NearbyPlaceState) {

    Spacer(modifier = Modifier.height(16.dp))

    // Display the locations in a LazyColumn
    LazyColumn {
        Log.d(TAG, "View model places: ${state.nearbyPlaces}")
        items(state.nearbyPlaces) { nearbyPlace ->
            LocationRow(nearbyPlace = nearbyPlace)
        }
    }
}