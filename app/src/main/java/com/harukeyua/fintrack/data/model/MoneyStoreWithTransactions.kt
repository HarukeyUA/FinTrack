package com.harukeyua.fintrack.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class MoneyStoreWithTransactions(
    @Embedded val moneyStore: MoneyStore,
    @Relation(
        parentColumn = "id",
        entityColumn = "moneyStoreId"
    )
    val transactions: List<Transaction>
)
