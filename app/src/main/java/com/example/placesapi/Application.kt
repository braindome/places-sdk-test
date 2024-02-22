package com.example.placesapi

import android.app.Application
import com.google.android.libraries.places.api.Places

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        Places.initializeWithNewPlacesApiEnabled(this, BuildConfig.PLACES_API_KEY)
    }
}