/*
 * Copyright  2021 Nazar Rusnak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.harukeyua.fintrack.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harukeyua.fintrack.data.model.Account
import com.harukeyua.fintrack.data.model.AccountType
import com.harukeyua.fintrack.repos.FinEditRepo
import com.harukeyua.fintrack.utils.Event
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

    fun addAccount(accountName: String, accountType: AccountType, accountBalance: String) {
        if (accountName.isEmpty()) {
            _accountNameError.value = true
            return
        }
        try {
            val balance = convertToPenny(accountBalance)
            Log.d("add", "balance: $balance")
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    finEditRepo.insertAccount(
                        Account(
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