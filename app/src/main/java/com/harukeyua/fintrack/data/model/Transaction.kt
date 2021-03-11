package com.harukeyua.fintrack.data.model

import androidx.room.*
import java.time.OffsetDateTime

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            childColumns = ["accountId"],
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TransactionType::class,
            childColumns = ["transactionTypeId"],
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(index = true) val transactionTypeId: Int,
    @ColumnInfo(index = true) val accountId: Int,
    val amount: Long,
    @Embedded val location: Location,
    val dateTime: OffsetDateTime,
    val description: String = ""
)
