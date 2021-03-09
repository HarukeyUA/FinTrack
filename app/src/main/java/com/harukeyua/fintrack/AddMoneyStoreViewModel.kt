package com.harukeyua.fintrack

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harukeyua.fintrack.data.model.MoneyStore
import com.harukeyua.fintrack.data.model.StoreType
import com.harukeyua.fintrack.repos.FinEditRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddMoneyStoreViewModel @Inject constructor(private val finEditRepo: FinEditRepo) :
    ViewModel() {

    private val _accountNameError = MutableLiveData(false)
    val accountNameError: LiveData<Boolean>
        get() = _accountNameError

    private val _accountBalanceError = MutableLiveData(false)
    val accountBalanceError: LiveData<Boolean>
        get() = _accountBalanceError

    private val _navigateToOverview = MutableLiveData<Event<Unit>>()
    val navigateToOverview: LiveData<Event<Unit>>
        get() = _navigateToOverview

    fun addAccount(accountName: String, accountType: StoreType, accountBalance: String) {
        if (accountName.isEmpty()) {
            _accountNameError.value = true
            return
        }
        try {
            val balance = convertToPenny(accountBalance)
            Log.d("add", "balance: $balance")
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    finEditRepo.insertMoneyStore(
                        MoneyStore(
                            name = accountName,
                            type = accountType,
                            balance = balance
                        )
                    )
                }
                _navigateToOverview.value = Event(Unit)
            }
        } catch (e: NumberFormatException) {
            _accountBalanceError.value = true
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun convertToPenny(item: String): Long {
        val dotPosition = item.indexOfFirst { it == ',' || it == '.' }
        return if (item.contains("[.,]".toRegex()) && item.length - dotPosition == 3)
            (item.replace("[.,]".toRegex(), "")).toLong()
        else if (item.contains("[.,]".toRegex()) && item.length - dotPosition == 2)
            (item.replace("[.,]".toRegex(), "0")).toLong()
        else if (item.isNotEmpty())
            item.plus("00").toLong()
        else
            0L
    }
}