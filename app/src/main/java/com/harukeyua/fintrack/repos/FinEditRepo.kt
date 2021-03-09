package com.harukeyua.fintrack.repos

import com.harukeyua.fintrack.data.FinDao
import com.harukeyua.fintrack.data.model.MoneyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FinEditRepo @Inject constructor(private val dao: FinDao) {

    suspend fun insertMoneyStore(item: MoneyStore) {
        withContext(Dispatchers.IO) {
            dao.insertMoneyStore(item)
        }
    }
}