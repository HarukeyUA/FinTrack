package com.harukeyua.fintrack

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.harukeyua.fintrack.data.FinDao
import com.harukeyua.fintrack.data.FinDatabase
import com.harukeyua.fintrack.data.model.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InvalidObjectException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors


@RunWith(AndroidJUnit4::class)
class FinDatabaseReadWriteTest {
    private lateinit var finDao: FinDao
    private lateinit var db: FinDatabase

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, FinDatabase::class.java
        ).setTransactionExecutor(Executors.newSingleThreadExecutor()).build()
        finDao = db.finDao()
    }

    @After
    @Throws(Exception::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeTransactionTypeAndRead() {
        val transactionType = TransactionType(id = 1, name = "Test type")
        runBlocking {
            finDao.insertTransactionType(transactionType)
        }

        val types = finDao.getTransactionTypes()
        val result = types.getValueBlocking()
            ?: throw InvalidObjectException("null returned instead of types")
        assertThat(result[0]).isEqualTo(transactionType)
    }

    @Test
    @Throws(Exception::class)
    fun writeMoneyStoreAndRead() {
        val moneyStore =
            MoneyStore(id = 1, name = "Test money store", type = StoreType.CARD, balance = 500)
        runBlocking {
            finDao.insertMoneyStore(moneyStore)
        }

        val stores = finDao.getMoneyStores()
        val result = stores.getValueBlocking()
            ?: throw InvalidObjectException("null returned instead of types")
        assertThat(result[0]).isEqualTo(moneyStore)
    }

    @Test
    @Throws(Exception::class)
    fun writeTransactionAndRead() {
        val transactionType = TransactionType(id = 1, name = "Test type")
        val moneyStore =
            MoneyStore(id = 1, name = "Test money store", type = StoreType.CARD, balance = 500)
        val location = Location(lon = 1f, lat = 1f, "Test Shop")
        val dateTime = OffsetDateTime.parse(
            "2017-10-17T11:01:12.972-02:00",
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
        )
        val transaction =
            Transaction(
                id = 1,
                transactionTypeId = 1,
                moneyStoreId = 1,
                500,
                location = location,
                dateTime = dateTime,
                description = "Test description"
            )

        runBlocking {
            finDao.insertMoneyStore(moneyStore)
            finDao.insertTransactionType(transactionType)
            finDao.insertTransaction(transaction)
        }

        val transactions = finDao.getTransactions()
        val moneyStoreWithTransactions = finDao.getMoneyStoreWithTransactions()
        val typeWithTransactions = finDao.getTransactionTypeWithTransactions()
        val result = transactions.getValueBlocking()
            ?: throw InvalidObjectException("null returned instead of types")
        val resultStoreWithTransactions = moneyStoreWithTransactions.getValueBlocking()
            ?: throw InvalidObjectException("null returned instead of types")
        val resultTypeWithTransactions = typeWithTransactions.getValueBlocking()
            ?: throw InvalidObjectException("null returned instead of types")

        assertThat(result[0]).isEqualTo(transaction)
        assertThat(resultStoreWithTransactions[0].moneyStore).isEqualTo(moneyStore)
        assertThat(resultStoreWithTransactions[0].transactions[0]).isEqualTo(transaction)
        assertThat(resultTypeWithTransactions[0].transactionType).isEqualTo(transactionType)
        assertThat(resultTypeWithTransactions[0].transactions[0]).isEqualTo(transaction)
    }
}