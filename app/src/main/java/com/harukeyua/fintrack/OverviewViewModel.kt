package com.harukeyua.fintrack

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.harukeyua.fintrack.data.FinDao
import com.harukeyua.fintrack.data.model.MoneyStore
import com.harukeyua.fintrack.data.model.Transaction
import com.harukeyua.fintrack.data.model.TransactionType
import com.harukeyua.fintrack.repos.FinInfoRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(private val finInfoRepo: FinInfoRepo, private val dao: FinDao) :
    ViewModel() {

    private val _selectedMoneyStore = MutableLiveData<MoneyStore?>()

    val moneyStoreList = finInfoRepo.moneyStoreList

    val transactionTypes = finInfoRepo.typesList

    fun insert(transaction: Transaction) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.insertTransaction(transaction)
            }
        }
    }

    fun insertType(type: TransactionType) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.insertTransactionType(type)
            }
        }
    }

    fun insertMoneyStore(store: MoneyStore) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.insertMoneyStore(store)
            }
        }
    }

    private val clearListCh = Channel<Unit>(Channel.CONFLATED)

    @FlowPreview
    @ExperimentalCoroutinesApi
    val transactions = flowOf(
        clearListCh.receiveAsFlow().map { PagingData.empty() },
        _selectedMoneyStore.asFlow().flatMapLatest { finInfoRepo.transactionsListByStore(it?.id) }
            .cachedIn(viewModelScope)
    ).flattenMerge(2)

    fun showTransactionsForStore(moneyStore: MoneyStore) {
        clearListCh.offer(Unit)
        _selectedMoneyStore.value = moneyStore
    }

    fun showAllTransactions() {
        clearListCh.offer(Unit)
        _selectedMoneyStore.value = null
    }
}