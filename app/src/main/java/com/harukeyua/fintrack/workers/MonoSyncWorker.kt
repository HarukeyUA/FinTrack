package com.harukeyua.fintrack.workers

import android.content.Context
import android.content.SharedPreferences
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
        val token = encryptedSharedPrefs.getString(MONOBANK_KEY_PREF, "")
        token?.let {
            if (it.isNotEmpty()) {
                when (val updateUserInfoResult = repo.updateMonoClientInfoSync(token)) {
                    is Resource.ApiError -> return handleApiError(updateUserInfoResult.errorCode)
                    is Resource.ExceptionError -> return if (tryToRetry) Result.retry() else Result.failure(
                        data.putInt(RESULT_KEY, SyncFailures.CONNECTION_ERROR.code).build()
                    )
                    else -> Unit
                }

                val list = loadMccList()
                return when (val result = repo.syncTransactions(token, list)) {
                    is Resource.ApiError -> handleApiError(result.errorCode)
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