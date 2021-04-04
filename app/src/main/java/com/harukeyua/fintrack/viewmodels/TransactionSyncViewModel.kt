package com.harukeyua.fintrack.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harukeyua.fintrack.repos.FinEditRepo
import com.harukeyua.fintrack.repos.SyncInfoRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionSyncViewModel @Inject constructor(
    private val repo: SyncInfoRepo,
    private val editRepo: FinEditRepo
) : ViewModel() {
    val lastSyncInfo = repo.lastSyncInfo

    fun removeMonoAccounts() {
        viewModelScope.launch {
            editRepo.deleteMonoAccounts()
        }
    }
}