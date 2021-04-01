package com.harukeyua.fintrack.repos

import com.harukeyua.fintrack.data.FinDao
import javax.inject.Inject

class SyncInfoRepo @Inject constructor(private val dao: FinDao) {

    val lastSyncInfo = dao.getLatestSyncInfo()
}