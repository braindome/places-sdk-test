package com.example.placesapi

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.placesapi.localdb.NearbyPlaceDatabase
import com.example.placesapi.localdb.NearbyPlaceViewModel
import com.example.placesapi.ui.screens.LocationListScreen
import com.example.placesapi.ui.theme.PlacesAPITheme

const val TAG = "MainActivity"
const val apiKey =  BuildConfig.PLACES_API_KEY


/**
 * PHOTOS - TODO
 * 1. Store photo metadata in Room
 * 2. Store photo in local storage
 * 3. Store local file path in Room
 * 4. Access and display photos in the UI
 */

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            NearbyPlaceDatabase::class.java,
            "contacts.db"
        ).build()
    }

    private val viewModel by viewModels<NearbyPlaceViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NearbyPlaceViewModel(db.dao, application as Application) as T
                }
            }
        }
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            Log.e("Places test", "No api key")
            finish()
            return
        }

        setContent {
            PlacesAPITheme {
                val state by viewModel.state.collectAsState()
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.padding(20.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Button(onClick = { viewModel.getNearbyPlaces() }) {
                            Text(text = "Get Places")
                        }
                        Button(onClick = { viewModel.getPlaceById("ChIJWV3PC2PzT0YR-_gAuGdqTNQ") }) {
                            Text(text = "Get Place Info")
                        }
                        //LocationListScreen(state = state)
                    }


                }
            }
        }
    }


}

