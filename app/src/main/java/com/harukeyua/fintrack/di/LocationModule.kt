package com.harukeyua.fintrack.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ActivityContext

@InstallIn(FragmentComponent::class)
@Module
object LocationModule {

    @Provides
    fun provideFusedLocationClient(@ActivityContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    fun providePlacesClient(@ActivityContext context: Context): PlacesClient {
        return Places.createClient(context)
    }
}