package com.harukeyua.fintrack.viewmodels

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.harukeyua.fintrack.repos.FinInfoRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.OffsetDateTime
import javax.inject.Inject

@HiltViewModel
class TransactionsOnMapViewModel @Inject constructor(private val repo: FinInfoRepo) : ViewModel() {

    private val transactionsLastMonth = repo.getTransactionsInDateRange(
        Pair(
            OffsetDateTime.now().minusMonths(1),
            OffsetDateTime.now()
        )
    )

    val transactionsGrouped = Transformations.map(transactionsLastMonth) { list ->
        list.filter { it.location != null }
    }
}