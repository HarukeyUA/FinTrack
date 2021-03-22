package com.harukeyua.fintrack.repos

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.insertSeparators
import androidx.paging.map
import com.harukeyua.fintrack.data.FinDao
import com.harukeyua.fintrack.data.model.Transaction
import com.harukeyua.fintrack.data.model.TransactionInfo
import com.harukeyua.fintrack.data.model.TransactionListItem
import kotlinx.coroutines.flow.map
import java.time.OffsetDateTime
import javax.inject.Inject

class FinInfoRepo @Inject constructor(private val dao: FinDao) {

    val accountsList = dao.getAccounts()

    val typesList = dao.getTransactionTypes()

    fun transactionsListByStore(storeId: Int?) = Pager(
        config = PagingConfig(
            pageSize = 60,
            enablePlaceholders = true,
            maxSize = 200
        )
    ) {
        if (storeId == null) {
            dao.getTransactionsPaging()
        } else {
            dao.getTransactionsPagingByAccount(storeId)
        }
    }.flow.map { pagingData ->
        pagingData.map { item -> TransactionListItem.Item(item) }
            .insertSeparators { before: TransactionListItem.Item?, after: TransactionListItem.Item? ->
                if (before == null && after == null) {
                    // List is empty after fully loaded; return null to skip adding separator.
                    null
                } else if (after == null) {
                    // Footer; return null here to skip adding a footer.
                    null
                } else if (before == null) {
                    TransactionListItem.Separator(after.transaction.dateTime)
                } else if (after.transaction.dateTime.dayOfMonth != before.transaction.dateTime.dayOfMonth) {
                    TransactionListItem.Separator(after.transaction.dateTime)
                } else {
                    null
                }
            }
    }

    fun transactionsListByType(typeId: Int) = Pager(
        config = PagingConfig(
            pageSize = 60,
            enablePlaceholders = true,
            maxSize = 200
        )
    ) {
        dao.getTransactionsPagingByType(typeId)
    }.flow

    fun transactionsList() = Pager(
        config = PagingConfig(
            pageSize = 60,
            enablePlaceholders = true,
            maxSize = 200
        )
    ) {
        dao.getTransactionsPaging()
    }.flow

    fun getTransactionsInDateRange(range: Pair<OffsetDateTime, OffsetDateTime>): LiveData<List<Transaction>> {
        return dao.getTransactionsInDateRange(
            range.first,
            range.second.plusDays(1).withHour(0).withMinute(0)
        )
    }

    fun getTransactionsInfoInDateRange(range: Pair<OffsetDateTime, OffsetDateTime>): LiveData<List<TransactionInfo>> {
        return dao.getTransactionsInfoInDateRange(
            range.first,
            range.second.plusDays(1).withHour(0).withMinute(0)
        )
    }
}