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
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.PieEntry
import com.harukeyua.fintrack.repos.FinInfoRepo
import com.harukeyua.fintrack.utils.getFloatBalance
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.OffsetDateTime
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class StatsViewModel @Inject constructor(repo: FinInfoRepo) : ViewModel() {

    private val _selectedDateRange =
        MutableLiveData(Pair(OffsetDateTime.now().minusMonths(1), OffsetDateTime.now()))
    val selectedDateRange: LiveData<Pair<OffsetDateTime, OffsetDateTime>>
        get() = _selectedDateRange

    val transactionsInDateRange = Transformations.switchMap(selectedDateRange) { pair ->
        repo.getTransactionsInDateRange(pair)
    }

    val totalTransactions = Transformations.map(transactionsInDateRange) { list ->
        list.sumOf { it.amount }
    }

    val totalGainsTransactions = Transformations.map(transactionsInDateRange) { list ->
        list.filter { it.amount > 0 }.sumOf { it.amount }
    }

    val totalExpensesTransactions = Transformations.map(transactionsInDateRange) { list ->
        list.filter { it.amount < 0 }.sumOf { it.amount }
    }

    val transactionsInfoInDateRange = Transformations.switchMap(selectedDateRange) { pair ->
        repo.getTransactionsInfoInDateRange(pair)
    }

    val transactionsExpensesTypeChartData =
        Transformations.map(transactionsInfoInDateRange) { list ->
            list.filter { it.amount < 0 }.groupBy { it.transactionTypeName }.map {
                PieEntry(
                    getFloatBalance(it.value.sumOf { transaction -> abs(transaction.amount) })
                        ?: 0f,
                    it.key
                )
            }.sortedBy { it.value }
        }

    val transactionsIncomeTypeChartData = Transformations.map(transactionsInfoInDateRange) { list ->
        list.filter { it.amount > 0 }.groupBy { it.transactionTypeName }.map {
            PieEntry(
                getFloatBalance(it.value.sumOf { transaction -> abs(transaction.amount) }) ?: 0f,
                it.key
            )
        }.sortedBy { it.value }
    }

    val transactionsPlaceChartData = Transformations.map(transactionsInDateRange) { list ->
        list.filter { it.location != null && it.amount < 0 }.groupBy { it.location?.name }.map {
            val label = if (it.key!!.length > 20) "${it.key!!.take(20)}..." else it.key
            PieEntry(
                getFloatBalance(it.value.sumOf { transaction -> abs(transaction.amount) }) ?: 0f,
                label
            )
        }.sortedBy { it.value }
    }

    fun setDate(start: OffsetDateTime, end: OffsetDateTime) {
        _selectedDateRange.value = Pair(start, end)
    }
}