package com.harukeyua.fintrack.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
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

    val accountsBalanceChartData = Transformations.map(transactionsInDateRange) { list ->
        val grouped = list.groupBy { it.dateTime.dayOfYear }.map { it.value.last() }

        grouped.map { Entry(it.dateTime.dayOfYear.toFloat(), getFloatBalance(it.balance) ?: 0f) }
    }

    val transactionsInfoInDateRange = Transformations.switchMap(selectedDateRange) { pair ->
        repo.getTransactionsInfoInDateRange(pair)
    }

    val transactionsExpensesTypeChartData = Transformations.map(transactionsInfoInDateRange) { list ->
        list.filter { it.amount < 0 }.groupBy { it.transactionTypeName }.map {
            PieEntry(
                getFloatBalance(it.value.sumOf { transaction -> abs(transaction.amount) }) ?: 0f,
                it.key
            )
        }
    }

    val transactionsIncomeTypeChartData = Transformations.map(transactionsInfoInDateRange) { list ->
        list.filter { it.amount > 0 }.groupBy { it.transactionTypeName }.map {
            PieEntry(
                getFloatBalance(it.value.sumOf { transaction -> abs(transaction.amount) }) ?: 0f,
                it.key
            )
        }
    }

    fun setDate(start: OffsetDateTime, end: OffsetDateTime) {
        _selectedDateRange.value = Pair(start, end)
    }
}