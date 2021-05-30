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

package com.harukeyua.fintrack.workers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.harukeyua.fintrack.R
import com.harukeyua.fintrack.data.model.MccCategory
import com.harukeyua.fintrack.repos.MonobankSyncRepo
import com.harukeyua.fintrack.utils.MONOBANK_KEY_PREF
import com.harukeyua.fintrack.utils.Resource
import com.harukeyua.fintrack.utils.SYNC_NOTIFICATION_ID
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class SyncFailures(val code: Int) {
    CONNECTION_ERROR(-1),
    RATE_LIMIT(429),
    AUTH_ERROR(429)
}

@HiltWorker
class MonoSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    @Inject
    lateinit var encryptedSharedPrefs: SharedPreferences

    @Inject
    lateinit var repo: MonobankSyncRepo

    private val moshi = Moshi.Builder().build()

    private val tryToRetry: Boolean = params.inputData.getBoolean("RETRY", false)
    private val data = Data.Builder()

    override suspend fun doWork(): Result {
        Log.d("MonoSyncWorker", "Started sync...")
        val token = encryptedSharedPrefs.getString(MONOBANK_KEY_PREF, "")
        token?.let {
            if (it.isNotEmpty()) {
                val list = loadMccList()
                return when (val updateUserInfoResult =
                    repo.monoClientInfoSync(token, list)) {
                    is Resource.ApiError -> handleApiError(updateUserInfoResult.errorCode)
                    is Resource.ExceptionError -> if (tryToRetry) Result.retry() else Result.failure(
                        data.putInt(RESULT_KEY, SyncFailures.CONNECTION_ERROR.code).build()
                    )
                    is Resource.Success -> Result.success()
                    else -> Result.failure()
                }
            }
        }

        return Result.failure()
    }

    private fun handleApiError(code: Int): Result {
        return when (code) {
            403 -> {
                val builder = NotificationCompat.Builder(applicationContext, SYNC_NOTIFICATION_ID)
                    .setSmallIcon(R.drawable.ic_cloud_off)
                    .setContentTitle(applicationContext.getString(R.string.sync_fail_notification_title))
                    .setContentText(applicationContext.getString(R.string.sync_fail_notification_body))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                NotificationManagerCompat.from(applicationContext).notify(1, builder.build())
                Result.failure(data.putInt(RESULT_KEY, SyncFailures.AUTH_ERROR.code).build())
            }
            429 -> {
                if (tryToRetry) Result.retry() else Result.failure(
                    data.putInt(
                        RESULT_KEY,
                        SyncFailures.RATE_LIMIT.code
                    ).build()
                )
            }
            else -> Result.failure(
                data.putInt(RESULT_KEY, SyncFailures.CONNECTION_ERROR.code).build()
            )
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun loadMccList(): List<MccCategory> {
        return withContext(Dispatchers.IO) {
            val mccString = applicationContext.resources.openRawResource(R.raw.mcc).bufferedReader()
                .use { it.readText() }
            val type = Types.newParameterizedType(List::class.java, MccCategory::class.java)
            val adapter = moshi.adapter<List<MccCategory>>(type)
            adapter.fromJson(mccString)!!
        }
    }

    companion object {
        const val RESULT_KEY = "sync_result"
        const val RETRY_DATA_KEY = "RETRY"
    }
}