package com.example.placesapi.localdb

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placesapi.Application
import com.example.placesapi.TAG
import com.example.placesapi.apiKey
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NearbyPlaceViewModel(
    private val dao: NearbyPlaceDao,
    private val application: Application
): ViewModel() {
    private val _sortType = MutableStateFlow(SortType.NAME)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _nearbyPlaces = _sortType
        .flatMapLatest { sortType ->
            when (sortType) {
                SortType.NAME -> dao.getNearbyPlacesOrderedByName()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _state = MutableStateFlow(NearbyPlaceState())

    val state = combine(_state, _sortType, _nearbyPlaces) { state, sortType, nearbyPlaces ->
        state.copy(
            nearbyPlaces = nearbyPlaces,
            sortType = sortType
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NearbyPlaceState())


    fun saveNearbyPlaces(nearbyPlaceList: List<NearbyPlace>) {
        viewModelScope.launch {
            for (nearbyPlace in nearbyPlaceList) {
                dao.upsertNearbyPlace(nearbyPlace)
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
                Log.i(TAG, "Called from View Model. Place found: ${place.name}, ID: ${place.id}, Type: ${place.placeTypes}")
            }.addOnFailureListener { exception: Exception ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.message}")
                    val statusCode = exception.statusCode
                    TODO("Handle error with given status code")
                }
            }
    }

    fun getNearbyPlaces() {
        Places.initialize(application, apiKey)

        val placesClient = Places.createClient(application)
        val placeFields = listOf(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ID)

        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)

        if (ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val placeResponse = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    handlePlacesApiResponse(task.result)
                } else {
                    val exception = task.exception
                    if (exception is ApiException) {
                        Log.e(TAG, "Place not found: ${exception.statusCode}")
                    }
                }
            }
        } else {
            // Handle the case where location permission is not granted
            // You may want to show a dialog or request permission here
        }
    }

    private fun handlePlacesApiResponse(response: FindCurrentPlaceResponse?) {
        val nearbyPlacesList = mutableListOf<NearbyPlace>()
        for (placeLikelihood: PlaceLikelihood in response?.placeLikelihoods ?: emptyList()) {
            Log.i(
                TAG, "Place '${placeLikelihood.place.name} at coordinates ${placeLikelihood.place.latLng}, ID: ${placeLikelihood.place.id} has likelihood: ${placeLikelihood.likelihood}"
            )
        }

        for (placeLikelihood: PlaceLikelihood in response?.placeLikelihoods ?: emptyList()) {
            val name = placeLikelihood.place.name
            val latLng = placeLikelihood.place.latLng?.toString() ?: ""
            val id = placeLikelihood.place.id

            if (name != null && id != null) {
                val nearbyPlace = NearbyPlace(
                    name = name,
                    latLng = latLng,
                    id = id
                )
                nearbyPlacesList.add(nearbyPlace)
            }
        }

        saveNearbyPlaces(nearbyPlacesList)
    }

    /*
    fun getNearbyPlaces() {
        Places.initialize(application, apiKey)

        val placesClient = Places.createClient(this)
        val placeFields = listOf(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ID)

        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val placeResponse = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val response = task.result
                    val nearbyPlacesList = mutableListOf<NearbyPlace>()
                    for (placeLikelihood: PlaceLikelihood in response?.placeLikelihoods ?: emptyList()) {
                        Log.i(
                            TAG, "Place '${placeLikelihood.place.name} at coordinates ${placeLikelihood.place.latLng}, ID: ${placeLikelihood.place.id} has likelihood: ${placeLikelihood.likelihood}"
                        )
                    }

                    for (placeLikelihood: PlaceLikelihood in response?.placeLikelihoods ?: emptyList()) {
                        val name = placeLikelihood.place.name
                        val latLng = placeLikelihood.place.latLng?.toString() ?: ""
                        val id = placeLikelihood.place.id

                        if (name != null && id != null) {
                            val nearbyPlace = NearbyPlace(
                                name = name,
                                latLng = latLng,
                                id = id
                            )
                            nearbyPlacesList.add(nearbyPlace)
                        }
                    }

                    saveNearbyPlaces(nearbyPlacesList)

                } else {
                    val exception = task.exception
                    if (exception is ApiException) {
                        Log.e(TAG, "Place not found: ${exception.statusCode}")
                    }
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

    }

     */

    /*
fun saveNearbyPlaces() {
    val name = state.value.name
    val latLng = state.value.latLng

    if(name.isBlank() || latLng.isBlank() ) {
        return
    }

    val nearbyPlace = NearbyPlace(
        name = name,
        latLng = latLng
    )

    viewModelScope.launch {
        dao.upsertNearbyPlace(nearbyPlace)
    }
}

 */


}