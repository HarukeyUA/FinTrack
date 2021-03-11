package com.harukeyua.fintrack.repos

import com.harukeyua.fintrack.data.FinDao
import com.harukeyua.fintrack.data.model.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FinEditRepo @Inject constructor(private val dao: FinDao) {

    suspend fun insertAccount(item: Account) {
        withContext(Dispatchers.IO) {
            dao.insertAccount(item)
        }
    }
}