package com.harukeyua.fintrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.harukeyua.fintrack.data.model.Account
import com.harukeyua.fintrack.data.model.Transaction
import com.harukeyua.fintrack.data.model.TransactionType

const val DATABASE_NAME = "fin.db"

@Database(
    entities = [TransactionType::class, Account::class, Transaction::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FinDatabase : RoomDatabase() {
    abstract fun finDao(): FinDao

    companion object {
        @Volatile
        private var instance: FinDatabase? = null

        fun getInstance(context: Context): FinDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): FinDatabase {
            return Room.databaseBuilder(context, FinDatabase::class.java, DATABASE_NAME).build()
        }
    }
}