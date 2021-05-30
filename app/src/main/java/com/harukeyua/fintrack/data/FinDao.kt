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

package com.harukeyua.fintrack.data

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.harukeyua.fintrack.data.model.*
import com.harukeyua.fintrack.data.model.Transaction
import java.time.OffsetDateTime

@Dao
interface FinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionType(transactionType: TransactionType)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccounts(account: List<Account>)

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun updateAccount(account: Account)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateAccounts(accounts: List<Account>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: List<Transaction>)

    @Query("SELECT * FROM transactions ORDER BY datetime(dateTime) DESC")
    fun getTransactions(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY datetime(dateTime) DESC")
    fun getTransactionsPaging(): PagingSource<Int, Transaction>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY datetime(dateTime) DESC")
    fun getTransactionsPagingByAccount(accountId: Int): PagingSource<Int, Transaction>

    @Query("SELECT * FROM transactionTypes ORDER BY name ASC")
    fun getTransactionTypes(): LiveData<List<TransactionType>>

    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAccounts(): LiveData<List<Account>>

    @Query("SELECT * FROM accounts ORDER BY name ASC")
    suspend fun getAccountsList(): List<Account>

    @Query("SELECT * FROM transactions WHERE dateTime(dateTime) BETWEEN dateTime(:startDate) AND dateTime(:endDate) ORDER By dateTime(dateTime)")
    fun getTransactionsInDateRange(
        startDate: OffsetDateTime,
        endDate: OffsetDateTime
    ): LiveData<List<Transaction>>

    @Query("SELECT * FROM TransactionInfo WHERE dateTime(dateTime) BETWEEN dateTime(:startDate) AND dateTime(:endDate) ORDER By dateTime(dateTime)")
    fun getTransactionsInfoInDateRange(
        startDate: OffsetDateTime,
        endDate: OffsetDateTime
    ): LiveData<List<TransactionInfo>>

    @Query("SELECT * FROM transactions WHERE dateTime(dateTime) BETWEEN dateTime(:startDate) AND dateTime(:endDate) ORDER By dateTime(dateTime)")
    suspend fun getTransactionsListInDateRange(
        startDate: OffsetDateTime,
        endDate: OffsetDateTime
    ): List<Transaction>

    @Query("DELETE FROM accounts WHERE type = \"MONO\"")
    suspend fun deleteMonoAccounts()

    @Query("SELECT * FROM accounts WHERE type = \"MONO\" ORDER BY name ASC")
    suspend fun getMonoAccountsList(): List<Account>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncInfo(item: SyncInfo)

    @Query("SELECT * FROM SyncInfo ORDER BY id DESC")
    fun getLatestSyncInfo(): LiveData<List<SyncInfo>>

    @Query("SELECT * FROM SyncInfo ORDER BY id DESC")
    suspend fun getLatestSyncInfoList(): List<SyncInfo>

    @Query("SELECT * FROM transactionTypes WHERE NOT mccCode = -1")
    suspend fun getMccTransactionTypesList(): List<TransactionType>

    @Query("DELETE FROM SyncInfo")
    suspend fun deleteSyncInfo()
}