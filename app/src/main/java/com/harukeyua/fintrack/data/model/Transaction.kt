package com.harukeyua.fintrack.data.model

import androidx.room.*
import java.time.OffsetDateTime

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = MoneyStore::class,
            childColumns = ["moneyStoreId"],
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
    @ColumnInfo(index = true) val moneyStoreId: Int,
    val amount: Long,
    @Embedded val location: Location,
    val dateTime: OffsetDateTime,
    val description: String = ""
) {
    fun getConvertedBalance(): Float = (amount / 100).toFloat()
}
