/*
 * Copyright  2021 Nazar Rusnak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.harukeyua.fintrack.viewmodels

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.harukeyua.fintrack.data.model.Account
import com.harukeyua.fintrack.repos.FinInfoRepo
import com.harukeyua.fintrack.utils.getConvertedBalance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val finInfoRepo: FinInfoRepo
) : ViewModel() {

    private val _selectedMoneyStore = MutableLiveData<Account?>()

    val accountsList = finInfoRepo.accountsList

    val transactionTypes = finInfoRepo.typesList

    private val _totalBalance = MutableLiveData<String>()
    val totalBalance: LiveData<String>
        get() = _totalBalance

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