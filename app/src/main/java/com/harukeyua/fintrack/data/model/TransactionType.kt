package com.harukeyua.fintrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactionTypes")
data class TransactionType(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
