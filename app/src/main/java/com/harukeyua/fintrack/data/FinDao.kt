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

    @Query("SELECT * FROM transactions WHERE transactionTypeId = :typeId ORDER BY datetime(dateTime) DESC")
    fun getTransactionsPagingByType(typeId: Int): PagingSource<Int, Transaction>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY datetime(dateTime) DESC")
    fun getTransactionsPagingByAccount(accountId: Int): PagingSource<Int, Transaction>

    @Query("SELECT * FROM transactionTypes ORDER BY name ASC")
    fun getTransactionTypes(): LiveData<List<TransactionType>>

    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAccounts(): LiveData<List<Account>>

    @androidx.room.Transaction
    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAccountWithTransactions(): LiveData<List<AccountWithTransactions>>

    @androidx.room.Transaction
    @Query("SELECT * FROM transactionTypes ORDER BY name ASC")
    fun getTransactionTypeWithTransactions(): LiveData<List<TransactionTypeWithTransactions>>

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
}