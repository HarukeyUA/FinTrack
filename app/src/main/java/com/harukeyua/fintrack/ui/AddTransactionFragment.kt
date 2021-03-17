package com.harukeyua.fintrack.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.harukeyua.fintrack.R
import com.harukeyua.fintrack.adapters.AccountListChooserAdapter
import com.harukeyua.fintrack.currencyInputFilter
import com.harukeyua.fintrack.databinding.AddTransactionFragmentBinding
import com.harukeyua.fintrack.utils.HorizontalMarginItemDecoration
import com.harukeyua.fintrack.viewmodels.AddTransactionViewModel
import com.harukeyua.fintrack.viewmodels.AmountErrorTypes
import com.harukeyua.fintrack.viewmodels.Operation
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@AndroidEntryPoint
class AddTransactionFragment : Fragment(), OnMapReadyCallback {

    private val viewModel: AddTransactionViewModel by viewModels()

    private var _binding: AddTransactionFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var accountListChooserAdapter: AccountListChooserAdapter

    private lateinit var placesAutocompleteAdapter: ArrayAdapter<String>

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var activityResult: ActivityResultLauncher<String>

    private lateinit var placesClient: PlacesClient

    private var map: GoogleMap? = null

    private var locationPermissionGranted = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddTransactionFragmentBinding.inflate(inflater, container, false)

        val appBarConfiguration = AppBarConfiguration(findNavController().graph)
        binding.topAppBar.setupWithNavController(findNavController(), appBarConfiguration)

        accountListChooserAdapter = AccountListChooserAdapter()
        binding.accountsList.adapter = accountListChooserAdapter
        binding.accountsList.addItemDecoration(
            HorizontalMarginItemDecoration(
                resources.getDimensionPixelSize(
                    R.dimen.margin_medium
                )
            )
        )

        binding.setDateButton.text = getString(R.string.date_standard, viewModel.selectedDate)
        binding.setTimeButton.text = getString(R.string.time_standard, viewModel.selectedTime)

        binding.transactionAmount.editText?.filters = listOf(currencyInputFilter).toTypedArray()

        placesAutocompleteAdapter =
            ArrayAdapter<String>(requireContext(), R.layout.list_select_item)
        (binding.placeName.editText as AutoCompleteTextView).setAdapter(placesAutocompleteAdapter)

        observe()
        setupListeners()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        placesClient = Places.createClient(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        return binding.root
    }

    private fun observe() {
        viewModel.accountsList.observe(viewLifecycleOwner) { list ->
            accountListChooserAdapter.submitList(list)
        }

        viewModel.transactionTypes.observe(viewLifecycleOwner) { types ->
            binding.categoryChips.removeAllViews()
            types.forEachIndexed { index, type ->
                val chip =
                    layoutInflater.inflate(
                        R.layout.chip_choice,
                        binding.categoryChips,
                        false
                    ) as Chip
                chip.text = type.name
                chip.id = type.id
                binding.categoryChips.addView(chip)
                if (index == 0) {
                    binding.categoryChips.clearCheck()
                    binding.categoryChips.check(type.id)
                }
            }
        }

        viewModel.amountErrorEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                when (it) {
                    AmountErrorTypes.EMPTY -> binding.transactionAmount.error =
                        getString(R.string.required_field_label)
                    AmountErrorTypes.ZERO -> binding.transactionAmount.error =
                        getString(R.string.amount_zero_error)
                    AmountErrorTypes.FORMAT -> binding.transactionAmount.error =
                        getString(R.string.format_error)
                }
            }
        }

        viewModel.descriptionErrorEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                binding.transactionDescription.error = getString(R.string.required_field_label)
            }
        }

        viewModel.insufficientAmountError.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.insufficient_error_title))
                    .setMessage(getString(R.string.insufficient_error_desc))
                    .setPositiveButton(R.string.OK) { _, _ -> }
                    .show()
            }
        }

        viewModel.dbError.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.placesLikelihood.observe(viewLifecycleOwner) { list ->
            placesAutocompleteAdapter.clear()
            placesAutocompleteAdapter.addAll(list.map { it.place.name })
        }

        viewModel.selectedLocation.observe(viewLifecycleOwner) { location ->
            map?.let {
                map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        location, DEFAULT_ZOOM_LEVEL
                    )
                )
                map?.clear()
                map?.addMarker(MarkerOptions().position(location))
            }
        }

        viewModel.locationNameError.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                binding.placeName.error = getString(R.string.required_field_label)
            }
        }

        viewModel.locationCoorsError.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.select_location_error),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun setupListeners() {
        binding.setDateButton.setOnClickListener {
            showDatePicker()
        }

        binding.addCategoryButton.setOnClickListener {
            showTypeAddDialog()
        }

        binding.setTimeButton.setOnClickListener {
            showTimePicker()
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.add_transaction) {
                val operation =
                    if (binding.toggleTransactionType.checkedButtonId == binding.buttonAdd.id) Operation.ADD else Operation.REMOVE
                viewModel.insertTransaction(
                    binding.transactionDescription.editText!!.text.toString(),
                    operation,
                    binding.transactionAmount.editText!!.text.toString(),
                    binding.categoryChips.checkedChipId,
                    accountListChooserAdapter.getSelectedAccount(),
                    binding.includeLocationCheckbox.isChecked,
                    binding.placeName.editText?.text.toString()
                )
                true
            } else
                false

        }

        (binding.placeName.editText as AutoCompleteTextView).setOnItemClickListener { _, _, position, _ ->
            viewModel.setPointLocation(position)
        }

        binding.includeLocationCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.placeName.isEnabled = isChecked
        }
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(viewModel.selectedDate.toInstant().toEpochMilli())
            .build()

        picker.addOnPositiveButtonClickListener {
            val selection = picker.selection
            selection?.let {
                val selection1 = Instant.ofEpochMilli(selection)
                val zone = ZoneOffset.systemDefault()
                viewModel.setDate(OffsetDateTime.ofInstant(selection1, zone))
                updateButtonText()
            }
        }

        picker.show(parentFragmentManager, "DatePickerDialog")
    }

    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(viewModel.selectedTime.hour).setMinute(viewModel.selectedTime.minute).build()

        picker.addOnPositiveButtonClickListener {
            viewModel.setTime(picker.hour, picker.minute)
            updateButtonText()
        }

        picker.show(parentFragmentManager, "TimePickerDialog")
    }

    private fun showTypeAddDialog() {
        val dialog = AddTypeDialog { name ->
            viewModel.insertType(name)
        }
        dialog.show(parentFragmentManager, "AddTypeDialog")
    }

    private fun updateButtonText() {
        binding.setDateButton.text = getString(R.string.date_standard, viewModel.selectedDate)
        binding.setTimeButton.text = getString(R.string.time_standard, viewModel.selectedTime)
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

    @SuppressLint("MissingPermission")
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

    override fun onMapReady(p0: GoogleMap?) {
        map = p0
        map?.setOnMapClickListener {
            LocationPickerFragment.display(childFragmentManager) {
                viewModel.setPointLocation(it)
            }
        }
        map?.setOnMyLocationButtonClickListener {
            populateCurrentPLaceAutoCompletion()
            false
        }
        setupLocation()
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
                        isScrollGesturesEnabled = false
                        isZoomGesturesEnabled = false
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
                            populateCurrentPLaceAutoCompletion()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun populateCurrentPLaceAutoCompletion() {
        if (map == null)
            return
        if (locationPermissionGranted) {
            val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
            val request = FindCurrentPlaceRequest.newInstance(placeFields)

            val placeResult = placesClient.findCurrentPlace(request)
            placeResult.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val likelyPlaces = task.result
                    viewModel.setCurrentPlaces(likelyPlaces.placeLikelihoods)
                }

            }
        }
    }

    companion object {
        const val DEFAULT_ZOOM_LEVEL = 15.0f
        const val DEFAULT_COUNTRY_ZOOM_LEVEL = 5.0f
        const val DEFAULT_LOCATION_LAT = 49.0256141
        const val DEFAULT_LOCATION_LON = 30.2700183
    }

}