package com.harukeyua.fintrack.data.model

import androidx.room.DatabaseView
import java.time.OffsetDateTime

@DatabaseView(
    "SELECT transactions.id, transactions.amount," +
            " transactions.dateTime, transactionTypes.name AS transactionTypeName" +
            " FROM transactions INNER JOIN transactionTypes" +
            " ON transactions.transactionTypeId = transactionTypes.id"
)
data class TransactionInfo(
    val id: Int,
    val transactionTypeName: String,
    val amount: Long,
    val dateTime: OffsetDateTime
)