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

package com.harukeyua.fintrack.repos

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.insertSeparators
import androidx.paging.map
import com.harukeyua.fintrack.data.FinDao
import com.harukeyua.fintrack.data.model.Account
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

    suspend fun getAccountsList(): List<Account> {
        return dao.getAccountsList()
    }

    suspend fun getTransactionsListInDateRange(range: Pair<OffsetDateTime, OffsetDateTime>): List<Transaction> {
        return dao.getTransactionsListInDateRange(range.first, range.second)
    }
}