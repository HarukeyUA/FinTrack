package com.harukeyua.fintrack.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.coroutineScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.maps.android.ktx.awaitMap
import com.harukeyua.fintrack.R
import com.harukeyua.fintrack.databinding.LocationPickerFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationPickerFragment(val onLocationPick: (location: LatLng) -> Unit) : DialogFragment() {

    private lateinit var toolbar: Toolbar

    private var map: GoogleMap? = null

    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = LocationPickerFragmentBinding.inflate(inflater, container, false)
        toolbar = binding.toolbar
        toolbar.setNavigationOnClickListener { dismiss() }
        toolbar.inflateMenu(R.menu.pick_location_menu)
        toolbar.setOnMenuItemClickListener {
            map?.let {
                onLocationPick(it.cameraPosition.target)
            }
            dismiss()
            true
        }
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        lifecycle.coroutineScope.launchWhenCreated {
            val googleMap = mapFragment?.awaitMap()
            setupMap(googleMap)
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            it.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
    }

    private fun setupMap(p0: GoogleMap?) {
        map = p0

        val locationRequest = LocationRequest.create().apply {
            interval = 1000 * 30
            fastestInterval = 1000 * 5
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(requireContext())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnCompleteListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (task.result != null) {
                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(task.result.latitude, task.result.longitude),
                                    DEFAULT_ZOOM_LEVEL
                                )
                            )
                        }
                    }
                }

            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                map?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(DEFAULT_LOCATION_LAT, DEFAULT_LOCATION_LON),
                        DEFAULT_COUNTRY_ZOOM_LEVEL
                    )
                )
            }

        }

    }

    companion object {
        const val DEFAULT_ZOOM_LEVEL = 15.0f
        const val DEFAULT_COUNTRY_ZOOM_LEVEL = 5.0f
        const val DEFAULT_LOCATION_LAT = 49.0256141
        const val DEFAULT_LOCATION_LON = 30.2700183

        fun display(
            fragmentManager: FragmentManager,
            onLocationPick: (location: LatLng) -> Unit
        ): LocationPickerFragment {
            val exampleDialog = LocationPickerFragment(onLocationPick)
            exampleDialog.show(fragmentManager, "LocationPicker")
            return exampleDialog
        }
    }
}