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

package com.harukeyua.fintrack.repos

import android.util.Log
import androidx.lifecycle.liveData
import androidx.room.withTransaction
import com.harukeyua.fintrack.api.MonobankService
import com.harukeyua.fintrack.data.FinDatabase
import com.harukeyua.fintrack.data.model.Account
import com.harukeyua.fintrack.data.model.AccountType
import com.harukeyua.fintrack.data.model.api.AccountInfo
import com.harukeyua.fintrack.utils.Resource
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class MonobankApiRepo @Inject constructor(
    private val service: MonobankService,
    private val db: FinDatabase
) {

    fun initialMonoClientInfoSync(key: String) =
        liveData {
            emit(Resource.Loading)
            try {
                val clientInfo = service.getAccountInfo(key)
                replaceMonoAccounts(clientInfo.accounts)
                emit(Resource.Success(clientInfo))
            } catch (e: HttpException) {
                Log.e(TAG, "Error requesting client info", e)
                emit(Resource.ApiError(e.code()))
            } catch (e: IOException) {
                Log.e(TAG, "Error requesting client info", e)
                emit(Resource.ExceptionError(e.message ?: ""))
            } catch (e: Exception) {
                Log.e(TAG, "Error requesting client info", e)
                emit(Resource.ExceptionError(e.message ?: ""))
            }
        }

    fun updateMonoClientInfoSync(key: String) =
        liveData {
            emit(Resource.Loading)
            try {
                val clientInfo = service.getAccountInfo(key)
                updateMonoAccounts(clientInfo.accounts)
                emit(Resource.Success(clientInfo))
            } catch (e: HttpException) {
                Log.e(TAG, "Error requesting client info", e)
                emit(Resource.ApiError(e.code()))
            } catch (e: IOException) {
                Log.e(TAG, "Error requesting client info", e)
                emit(Resource.ExceptionError(e.message ?: ""))
            } catch (e: Exception) {
                Log.e(TAG, "Error requesting client info", e)
                emit(Resource.ExceptionError(e.message ?: ""))
            }
        }

    private suspend fun replaceMonoAccounts(monoAccounts: List<AccountInfo>) {
        db.withTransaction {
            db.finDao().deleteMonoAccounts()
            val accountsToInsert =
                monoAccounts.filter { account -> account.currencyCode == UAH_CODE }
            db.finDao().insertAccounts(accountsToInsert.map {
                Account(
                    name = it.type,
                    type = AccountType.MONO,
                    balance = it.balance,
                    monoCardType = it.type,
                    monoId = it.id
                )
            })
        }
    }

    private suspend fun updateMonoAccounts(monoAccounts: List<AccountInfo>) {
        db.withTransaction {
            val monoAccountsInDb = db.finDao().getMonoAccountsList()

            // Insert new accounts if any
            val monoAccountsToInsert =
                monoAccounts.filter { account -> monoAccountsInDb.find { it.monoCardType == account.type } == null && account.currencyCode == UAH_CODE }
            db.finDao().insertAccounts(monoAccountsToInsert.map {
                Account(
                    name = it.type,
                    type = AccountType.MONO,
                    balance = it.balance,
                    monoCardType = it.type,
                    monoId = it.id
                )
            })

            // Update existing accounts
            val monoAccountsToUpdate =
                monoAccountsInDb.mapNotNull { account ->
                    val monoAccount = monoAccounts.find { it.type == account.monoCardType }
                    if (monoAccount != null)
                        account.copy(balance = monoAccount.balance)
                    else
                        null
                }
            db.finDao().updateAccounts(monoAccountsToUpdate)

        }
    }

    companion object {
        const val TAG = "MonobankApiRepo"
        const val UAH_CODE = 980
    }
}