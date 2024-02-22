package com.example.placesapi

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.placesapi.localdb.NearbyPlaceDatabase
import com.example.placesapi.localdb.NearbyPlaceViewModel
import com.example.placesapi.ui.theme.PlacesAPITheme
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPhotoResponse
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse

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

    private var bitmap by mutableStateOf<ImageBitmap?>(null)
    private var loadedBitmapResourceId by mutableStateOf<Int?>(null)



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
                        Button(onClick = { getLocationPhotoById("ChIJWV3PC2PzT0YR-_gAuGdqTNQ") }) {
                            Text(text = "Get Place Image")
                        }
                        //LocationListScreen(state = state)

                        bitmap?.let {
                            Image(
                                bitmap = it,
                                contentDescription = "Location image",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .size(300.dp)
                            )
                        }


                        
                    }


                }
            }
        }
    }

    fun getPlaceById(placeId: String) {
        // Specify the fields to return.
        // Places.initialize(application, apiKey)
        // Places.initializeWithNewPlacesApiEnabled(application, apiKey)

        val placesClient = Places.createClient(application)
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.TYPES)

        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                // Log.d(TAG, "Inside vm")
                Log.i(TAG, "Called from MainActivity. Place found: ${place.name}, ID: ${place.id}, Type: ${place.placeTypes}")
            }.addOnFailureListener { exception: Exception ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.message}")
                    val statusCode = exception.statusCode
                    TODO("Handle error with given status code")
                }
            }
    }

    fun getLocationPhotoById(placeId: String) {

        Places.initialize(application, apiKey)

        val placesClient = Places.createClient(application)

        // Specify fields. Requests for photos must always have the PHOTO_METADATAS field.
        val fields = listOf(Place.Field.PHOTO_METADATAS)

        // Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
        val placeRequest = FetchPlaceRequest.newInstance(placeId, fields)

        placesClient.fetchPlace(placeRequest)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place

                // Get the photo metadata.
                val metada = place.photoMetadatas
                if (metada == null || metada.isEmpty()) {
                    Log.w(TAG, "No photo metadata.")
                    return@addOnSuccessListener
                }
                val photoMetadata = metada.first()

                // Get the attribution text.
                val attributions = photoMetadata?.attributions

                // Create a FetchPhotoRequest.
                val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(500) // Optional.
                    .setMaxHeight(300) // Optional.
                    .build()
                placesClient.fetchPhoto(photoRequest)
                    .addOnSuccessListener { fetchPhotoResponse: FetchPhotoResponse ->
                        val loadedBitmap = fetchPhotoResponse.bitmap
                        //imageView.setImageBitmap(bitmap)
                        bitmap = loadedBitmap.asImageBitmap()


                    }.addOnFailureListener { exception: Exception ->
                        if (exception is ApiException) {
                            Log.e(TAG, "Place not found: " + exception.message)
                            val statusCode = exception.statusCode
                            TODO("Handle error with given status code.")
                        }
                    }
            }


    }


}

