package com.harukeyua.fintrack.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionTypeWithTransactions(
    @Embedded val transactionType: TransactionType,
    @Relation(
        parentColumn = "id",
        entityColumn = "transactionTypeId"
    )
    val transactions: List<Transaction>
)
