package com.harukeyua.fintrack.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.chip.Chip
import com.harukeyua.fintrack.convertToPenny
import com.harukeyua.fintrack.data.model.Account
import com.harukeyua.fintrack.data.model.Transaction
import com.harukeyua.fintrack.data.model.TransactionType
import com.harukeyua.fintrack.repos.FinEditRepo
import com.harukeyua.fintrack.repos.FinInfoRepo
import com.harukeyua.fintrack.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import javax.inject.Inject

enum class Operation { ADD, REMOVE }

enum class AmountErrorTypes { EMPTY, ZERO, FORMAT }

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val finInfoRepo: FinInfoRepo,
    private val finEditRepo: FinEditRepo
) :
    ViewModel() {

    val accountsList = finInfoRepo.accountsList

    val transactionTypes = finInfoRepo.typesList

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
        account: Account?
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

        val transaction = Transaction(
            transactionTypeId = categoryId,
            accountId = account.id,
            amount = amountLong,
            dateTime = dateTime,
            description = description
        )

        viewModelScope.launch {
            try {
                finEditRepo.insertTransaction(transaction)
                finEditRepo.updateAccount(account.copy(balance = account.balance + amountLong))
            } catch (e: Exception) {
                e.printStackTrace()
                _dbError.value = Event(e.localizedMessage ?: e.toString())
            }
        }

    }
}