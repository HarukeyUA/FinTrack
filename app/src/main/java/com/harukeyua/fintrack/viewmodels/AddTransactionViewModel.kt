package com.harukeyua.fintrack.viewmodels

import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.material.chip.Chip
import com.harukeyua.fintrack.data.model.*
import com.harukeyua.fintrack.repos.FinEditRepo
import com.harukeyua.fintrack.repos.FinInfoRepo
import com.harukeyua.fintrack.utils.Event
import com.harukeyua.fintrack.utils.convertToPenny
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.util.*
import javax.inject.Inject

enum class Operation { ADD, REMOVE }

enum class AmountErrorTypes { EMPTY, ZERO, FORMAT }

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val finInfoRepo: FinInfoRepo,
    private val finEditRepo: FinEditRepo
) :
    ViewModel() {

    val accountsList =
        finInfoRepo.accountsList.map { accountsList -> accountsList.filter { it.type != AccountType.MONO } }

    val transactionTypes = finInfoRepo.typesList.map { types ->
        types.distinctBy {
            it.name.toLowerCase(
                Locale.getDefault()
            )
        }
    }

    private val _descriptionErrorEvent = MutableLiveData<Event<Unit>>()
    val descriptionErrorEvent: LiveData<Event<Unit>>
        get() = _descriptionErrorEvent

    private val _amountErrorEvent = MutableLiveData<Event<AmountErrorTypes>>()
    val amountErrorEvent: LiveData<Event<AmountErrorTypes>>
        get() = _amountErrorEvent

    private val _insufficientAmountError = MutableLiveData<Event<Unit>>()
    val insufficientAmountError: LiveData<Event<Unit>>
        get() = _insufficientAmountError

    private val _dbError = MutableLiveData<Event<String>>()
    val dbError: LiveData<Event<String>>
        get() = _dbError

    private val _locationNameError = MutableLiveData<Event<Unit>>()
    val locationNameError: LiveData<Event<Unit>>
        get() = _locationNameError

    private val _locationCoorsError = MutableLiveData<Event<Unit>>()
    val locationCoorsError: LiveData<Event<Unit>>
        get() = _locationCoorsError

    private val _placesLikelihood = MutableLiveData<List<PlaceLikelihood>>()
    val placesLikelihood: LiveData<List<PlaceLikelihood>>
        get() = _placesLikelihood

    private val _selectedLocation = MutableLiveData<LatLng>()
    val selectedLocation: LiveData<LatLng>
        get() = _selectedLocation

    var selectedDate: OffsetDateTime = OffsetDateTime.now()
        private set

    var selectedTime: OffsetDateTime = OffsetDateTime.now()
        private set

    fun setDate(offsetDateTime: OffsetDateTime) {
        selectedDate = offsetDateTime
    }

    fun setTime(hours: Int, minutes: Int) {
        selectedTime = selectedTime.withHour(hours).withMinute(minutes)
    }

    fun insertType(name: String) {
        if (name.isNotEmpty()) {
            viewModelScope.launch {
                finEditRepo.insertType(TransactionType(name = name))
            }
        }
    }

    private fun convertUserAmountInput(amount: String): Long? {
        val amountLong = if (amount.isEmpty()) {
            _amountErrorEvent.value = Event(AmountErrorTypes.EMPTY)
            null
        } else {
            try {
                convertToPenny(amount)
            } catch (e: Exception) {
                e.printStackTrace()
                _amountErrorEvent.value = Event(AmountErrorTypes.FORMAT)
                null
            }
        }

        return if (amountLong == 0L) {
            _amountErrorEvent.value = Event(AmountErrorTypes.ZERO)
            null
        } else amountLong
    }

    fun insertTransaction(
        description: String,
        operation: Operation,
        amount: String,
        categoryId: Int,
        account: Account?,
        includeLocation: Boolean,
        placeName: String
    ) {
        var isValid = true
        if (description.isEmpty()) {
            _descriptionErrorEvent.value = Event(Unit)
            isValid = false
        }
        if (categoryId == Chip.NO_ID) {
            isValid = false
        }

        var amountLong = convertUserAmountInput(amount)

        if (includeLocation) {
            if (placeName.isEmpty()) {
                _locationNameError.value = Event(Unit)
                isValid = false
            }

            if (selectedLocation.value == null) {
                _locationCoorsError.value = Event(Unit)
                isValid = false
            }
        }

        if (account == null || amountLong == null || !isValid) {
            return
        }

        if (operation == Operation.REMOVE && account.balance < amountLong) {
            _insufficientAmountError.value = Event(Unit)
            return
        } else if (operation == Operation.REMOVE)
            amountLong *= -1

        val dateTime = OffsetDateTime.of(
            selectedDate.year,
            selectedDate.monthValue,
            selectedDate.dayOfMonth,
            selectedTime.hour,
            selectedTime.minute,
            OffsetDateTime.now().second,
            0,
            selectedDate.offset
        )
        val resBalance = account.balance + amountLong
        val transaction = if (!includeLocation)
            Transaction(
                transactionTypeId = categoryId,
                accountId = account.id,
                amount = amountLong,
                dateTime = dateTime,
                description = description,
                balance = resBalance
            ) else
            Transaction(
                transactionTypeId = categoryId,
                accountId = account.id,
                amount = amountLong,
                dateTime = dateTime,
                description = description,
                location = LocationInfo(
                    selectedLocation.value!!.longitude,
                    selectedLocation.value!!.latitude,
                    placeName
                ),
                balance = resBalance
            )

        viewModelScope.launch {
            try {
                finEditRepo.insertTransaction(transaction)
                finEditRepo.updateAccount(account.copy(balance = resBalance))
            } catch (e: Exception) {
                e.printStackTrace()
                _dbError.value = Event(e.localizedMessage ?: e.toString())
            }
        }

    }

    fun setCurrentPlaces(list: List<PlaceLikelihood>) {
        _placesLikelihood.value = list
    }

    fun setPointLocation(likelyPLaceIndex: Int) {
        try {
            val place = _placesLikelihood.value?.get(likelyPLaceIndex)
            place?.let {
                _selectedLocation.value = it.place.latLng
            }
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }

    fun setPointLocation(latLng: LatLng) {
        _selectedLocation.value = latLng
        // Manual location selection - clear suggestions
        _placesLikelihood.value = listOf()
    }
}