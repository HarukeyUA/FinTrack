package com.harukeyua.fintrack.viewmodels

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.harukeyua.fintrack.data.FinDao
import com.harukeyua.fintrack.data.model.Account
import com.harukeyua.fintrack.data.model.Transaction
import com.harukeyua.fintrack.data.model.TransactionType
import com.harukeyua.fintrack.utils.getConvertedBalance
import com.harukeyua.fintrack.repos.FinInfoRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val finInfoRepo: FinInfoRepo,
    private val dao: FinDao
) : ViewModel() {

    private val _selectedMoneyStore = MutableLiveData<Account?>()

    val accountsList = finInfoRepo.accountsList

    val transactionTypes = finInfoRepo.typesList

    private val _totalBalance = MutableLiveData<String>()
    val totalBalance: LiveData<String>
        get() = _totalBalance


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

    fun insertMoneyStore(store: Account) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.insertAccount(store)
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

    fun showTransactionsForStore(account: Account) {
        clearListCh.offer(Unit)
        _selectedMoneyStore.value = account
    }

    fun showAllTransactions() {
        clearListCh.offer(Unit)
        _selectedMoneyStore.value = null
    }

    fun updateTotalBalance(accounts: List<Account>) {
        _totalBalance.value = getConvertedBalance(accounts.sumOf { it.balance })
    }
}