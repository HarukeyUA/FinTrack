package com.harukeyua.fintrack.data.model

import java.time.OffsetDateTime

sealed class TransactionListItem {
    data class Item(val transaction: Transaction): TransactionListItem()
    data class Separator(val offsetDateTime: OffsetDateTime): TransactionListItem()
}