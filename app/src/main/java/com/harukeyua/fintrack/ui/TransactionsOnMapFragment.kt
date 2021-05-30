package com.harukeyua.fintrack.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.ktx.awaitMap
import com.harukeyua.fintrack.R
import com.harukeyua.fintrack.databinding.TransactionsOnMapFragmentBinding
import com.harukeyua.fintrack.utils.getConvertedBalance
import com.harukeyua.fintrack.viewmodels.TransactionsOnMapViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TransactionsOnMapFragment : Fragment() {

    private val viewModel: TransactionsOnMapViewModel by viewModels()

    private var _binding: TransactionsOnMapFragmentBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var activityResult: ActivityResultLauncher<String>

    private var map: GoogleMap? = null

    private var locationPermissionGranted = false

    private var clusterManager: ClusterManager<TransactionClusterItem>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TransactionsOnMapFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        lifecycle.coroutineScope.launchWhenCreated {
            val googleMap = mapFragment?.awaitMap()
            setupMap(googleMap)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setUpClusterer() {
        clusterManager = ClusterManager(requireContext(), map)
        clusterManager?.renderer = DynamicIconClusterRenderer(requireContext(), map, clusterManager)

        map?.setOnCameraIdleListener(clusterManager)
        map?.setOnMarkerClickListener(clusterManager)
        observeTransactions()
    }

    private fun observeTransactions() {
        viewModel.transactionsGrouped.observe(viewLifecycleOwner) { list ->
            clusterManager?.clearItems()
            clusterManager?.addItems(list.map {
                TransactionClusterItem(
                    it.location!!.lat,
                    it.location.lon,
                    it.location.name,
                    getString(
                        R.string.map_transaction_marker,
                        it.description,
                        getConvertedBalance(it.amount)
                    ),
                    it.amount >= 0
                )
            })

            clusterManager?.cluster()

        }
    }

    private fun setupLocation() {
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
            requestLocationPermission()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityResult = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                locationPermissionGranted = true
                updateLocationUI()
                getDeviceLocation()
            } else {
                Log.e("AddTransactionFragment", "Location permission denied")
            }
        }
    }

    private fun requestLocationPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                locationPermissionGranted = true
                updateLocationUI()
                getDeviceLocation()
            }
            else -> {
                updateLocationUI()
                activityResult.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    private fun setupMap(p0: GoogleMap?) {
        map = p0
        setupLocation()
        setUpClusterer()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        map?.let {
            with(it) {
                try {
                    if (locationPermissionGranted) {
                        isMyLocationEnabled = true
                        uiSettings.isMyLocationButtonEnabled = true
                    } else {
                        isMyLocationEnabled = false
                        uiSettings.isMyLocationButtonEnabled = false
                        moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(DEFAULT_LOCATION_LAT, DEFAULT_LOCATION_LON),
                                DEFAULT_COUNTRY_ZOOM_LEVEL
                            )
                        )
                    }
                    with(uiSettings) {
                        isCompassEnabled = false
                    }
                } catch (e: Exception) {
                    Log.e("Exception: %s", e.message, e)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
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
                    } else {
                        map?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(DEFAULT_LOCATION_LAT, DEFAULT_LOCATION_LON),
                                DEFAULT_COUNTRY_ZOOM_LEVEL
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    companion object {
        const val DEFAULT_ZOOM_LEVEL = 15.0f
        const val DEFAULT_COUNTRY_ZOOM_LEVEL = 5.0f
        const val DEFAULT_LOCATION_LAT = 49.0256141
        const val DEFAULT_LOCATION_LON = 30.2700183
    }

}

class TransactionClusterItem(
    lat: Double,
    lng: Double,
    private val title: String,
    private val snippet: String,
    val isGain: Boolean
) : ClusterItem {

    private val position: LatLng = LatLng(lat, lng)

    override fun getPosition(): LatLng {
        return position
    }

    override fun getTitle(): String {
        return title
    }

    override fun getSnippet(): String {
        return snippet
    }

}