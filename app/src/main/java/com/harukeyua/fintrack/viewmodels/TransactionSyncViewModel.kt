package com.harukeyua.fintrack.viewmodels

import androidx.lifecycle.ViewModel
import com.harukeyua.fintrack.repos.SyncInfoRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TransactionSyncViewModel @Inject constructor(val repo: SyncInfoRepo): ViewModel() {
    val lastSyncInfo = repo.lastSyncInfo
}