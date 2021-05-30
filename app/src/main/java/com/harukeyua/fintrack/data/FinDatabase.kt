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

package com.harukeyua.fintrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.harukeyua.fintrack.data.model.*

const val DATABASE_NAME = "fin.db"

@Database(
    entities = [TransactionType::class, Account::class, Transaction::class, SyncInfo::class],
    views = [TransactionInfo::class],
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