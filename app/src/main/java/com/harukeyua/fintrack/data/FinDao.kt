package com.harukeyua.fintrack.data

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.harukeyua.fintrack.data.model.*

@Dao
interface FinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionType(transactionType: TransactionType)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: List<Transaction>)

    @Query("SELECT * FROM transactions ORDER BY datetime(dateTime) ASC")
    fun getTransactions(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY datetime(dateTime) ASC")
    fun getTransactionsPaging(): PagingSource<Int, Transaction>

    @Query("SELECT * FROM transactions WHERE transactionTypeId = :typeId ORDER BY datetime(dateTime) ASC")
    fun getTransactionsPagingByType(typeId: Int): PagingSource<Int, Transaction>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY datetime(dateTime) ASC")
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
}