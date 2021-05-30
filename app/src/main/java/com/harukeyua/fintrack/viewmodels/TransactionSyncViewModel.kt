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