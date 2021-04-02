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