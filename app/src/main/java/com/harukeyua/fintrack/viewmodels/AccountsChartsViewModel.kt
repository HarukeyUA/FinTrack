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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.harukeyua.fintrack.data.model.Account
import com.harukeyua.fintrack.repos.FinInfoRepo
import com.harukeyua.fintrack.utils.getFloatBalance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AccountsChartsViewModel @Inject constructor(private val repo: FinInfoRepo) : ViewModel() {

    private val _chartsData = MutableLiveData<List<Pair<Account, List<Entry>>>>()
    val chartsData: LiveData<List<Pair<Account, List<Entry>>>>
    get() = _chartsData

    fun getList(from: Long, to: Long) {
        val fromDateTime =
            OffsetDateTime.ofInstant(Instant.ofEpochSecond(from), ZoneId.systemDefault())
        val toDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(to), ZoneId.systemDefault())

        viewModelScope.launch {
            val accounts = repo.getAccountsList()
            val transactions = repo.getTransactionsListInDateRange(Pair(fromDateTime, toDateTime))
            val groupedByAccounts = transactions.groupBy { it.accountId }
            val test = groupedByAccounts.map { group ->
                Pair(accounts.find { it.id == group.key }!!,
                    group.value.groupBy { it.dateTime.toLocalDate() }.map {
                        Entry(
                            it.value.last().dateTime.dayOfYear.toFloat(),
                            getFloatBalance(it.value.last().balance) ?: 0f
                        )
                    })
            }
            _chartsData.value = test
        }
    }
}