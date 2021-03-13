package com.harukeyua.fintrack.repos

import com.harukeyua.fintrack.data.FinDao
import com.harukeyua.fintrack.data.model.Account
import com.harukeyua.fintrack.data.model.Transaction
import com.harukeyua.fintrack.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FinEditRepo @Inject constructor(private val dao: FinDao) {

    suspend fun insertAccount(item: Account) {
        withContext(Dispatchers.IO) {
            dao.insertAccount(item)
        }
    }

    suspend fun updateAccount(item: Account) {
        withContext(Dispatchers.IO) {
            dao.updateAccount(item)
        }
    }

    suspend fun insertType(type: TransactionType) {
        withContext(Dispatchers.IO) {
            dao.insertTransactionType(type)
        }
    }

    suspend fun insertTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            dao.insertTransaction(transaction)
        }
    }
}