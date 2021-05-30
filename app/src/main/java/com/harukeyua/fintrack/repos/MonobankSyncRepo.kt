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
import androidx.room.withTransaction
import com.harukeyua.fintrack.api.MonobankService
import com.harukeyua.fintrack.data.FinDatabase
import com.harukeyua.fintrack.data.model.*
import com.harukeyua.fintrack.data.model.api.AccountInfo
import com.harukeyua.fintrack.data.model.api.ClientInfo
import com.harukeyua.fintrack.data.model.api.StatementItem
import com.harukeyua.fintrack.utils.Resource
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject

class MonobankSyncRepo @Inject constructor(
    private val service: MonobankService,
    private val db: FinDatabase
) {

    suspend fun monoClientInfoSync(
        key: String,
        mccList: List<MccCategory>
    ): Resource<ClientInfo> {
        return try {
            val clientInfo = service.getAccountInfo(key)
            updateMonoAccounts(clientInfo.accounts)
            syncTransactions(key, mccList)
            Resource.Success(clientInfo)
        } catch (e: HttpException) {
            Log.e(TAG, "Error requesting client info", e)
            db.finDao().insertSyncInfo(SyncInfo(1, OffsetDateTime.now(), false))
            Resource.ApiError(e.code())
        } catch (e: IOException) {
            Log.e(TAG, "Error requesting client info, no connection?", e)
            db.finDao().insertSyncInfo(SyncInfo(1, OffsetDateTime.now(), false))
            Resource.ExceptionError(e.message ?: "")
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting client info, something borked", e)
            db.finDao().insertSyncInfo(SyncInfo(1, OffsetDateTime.now(), false))
            Resource.ExceptionError(e.message ?: "")
        }
    }

    private suspend fun updateMonoAccounts(monoAccounts: List<AccountInfo>) {
        db.withTransaction {
            val monoAccountsInDb = db.finDao().getMonoAccountsList()

            // Insert new accounts if any
            val monoAccountsToInsert =
                monoAccounts.filter { account -> monoAccountsInDb.find { it.monoCardType == account.type } == null && account.currencyCode == MonobankApiRepo.UAH_CODE }
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

    private suspend fun syncTransactions(key: String, mccList: List<MccCategory>) {
        val syncInfo = db.finDao().getLatestSyncInfoList()
        val monoAccounts = db.finDao().getMonoAccountsList()
        val from = if (syncInfo.isEmpty()) OffsetDateTime.now()
            .minusMonths(1) else syncInfo.first().syncDateTime

        monoAccounts.forEach { account ->
            val statements =
                service.getStatements(key, account.monoId ?: "0", from.toEpochSecond())

            val mccCategories = insertMissingTypes(statements, mccList)

            val toInsert = statements.map { statement ->
                val transactionType = mccCategories.find { it.mccCode == statement.mcc }
                val dateTime = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(statement.time),
                    ZoneOffset.systemDefault()
                )
                Transaction(
                    transactionTypeId = transactionType!!.id,
                    accountId = account.id,
                    amount = statement.amount,
                    description = statement.description,
                    balance = statement.balance,
                    monoId = statement.id,
                    dateTime = dateTime
                )
            }

            db.withTransaction {
                db.finDao().insertSyncInfo(SyncInfo(1, OffsetDateTime.now(), true))
                db.finDao().insertTransaction(toInsert)
                Log.i(
                    TAG,
                    "Successfully inserted ${toInsert.size} transaction entries for account ${account.monoCardType}"
                )
            }
        }

    }

    private suspend fun insertMissingTypes(
        statementsList: List<StatementItem>,
        mccList: List<MccCategory>
    ): List<TransactionType> {
        val mccCategories = db.finDao().getMccTransactionTypesList().toMutableList()
        db.withTransaction {
            // Check categories and add missing ones to db
            statementsList.forEach { statement ->
                if (mccCategories.find { it.mccCode == statement.mcc } == null) {
                    db.finDao().insertTransactionType(
                        TransactionType(
                            name = mccList.find { it.mcc == statement.mcc }?.shortDescription
                                ?: "Unknown",
                            mccCode = statement.mcc
                        ).also { mccCategories.add(it) }
                    )
                }
            }
        }
        return db.finDao().getMccTransactionTypesList()
    }

    companion object {
        const val TAG = "MonobankSyncRepo"
    }

}