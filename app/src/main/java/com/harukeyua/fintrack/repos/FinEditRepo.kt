package com.harukeyua.fintrack.repos

import com.harukeyua.fintrack.data.FinDao
import com.harukeyua.fintrack.data.model.Account
import com.harukeyua.fintrack.data.model.Transaction
import com.harukeyua.fintrack.data.model.TransactionType
import javax.inject.Inject

class FinEditRepo @Inject constructor(private val dao: FinDao) {

    suspend fun insertAccount(item: Account) {
        dao.insertAccount(item)
    }

    suspend fun updateAccount(item: Account) {
        dao.updateAccount(item)
    }

    suspend fun insertType(type: TransactionType) {
        dao.insertTransactionType(type)
    }

    suspend fun insertTransaction(transaction: Transaction) {
        dao.insertTransaction(transaction)
    }

    suspend fun deleteMonoAccounts() {
        dao.deleteMonoAccounts()
        dao.deleteSyncInfo()
    }
}