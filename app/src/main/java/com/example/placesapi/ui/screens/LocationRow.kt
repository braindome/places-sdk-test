package com.example.placesapi.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.placesapi.localdb.NearbyPlace

@Composable
fun LocationRow(nearbyPlace: NearbyPlace) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = "Name: ${nearbyPlace.name}", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "LatLng: ${nearbyPlace.latLng}")
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "ID: ${nearbyPlace.id}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LocationRowPreview() {
    LocationRow(nearbyPlace = NearbyPlace("Polestar Digital", "57.70797750000001,11.9745862", "123456"))
}
