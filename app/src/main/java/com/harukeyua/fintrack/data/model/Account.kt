package com.harukeyua.fintrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: AccountType,
    val balance: Long,
    val monoCardType: String? = null
) {
    fun getConvertedBalance(): String {
        val balanceString = balance.toString()
        return if (balance == 0L)
            "0.00"
        else
            StringBuilder(balanceString).insert(balanceString.length - 2, ".").toString()
    }
}

enum class AccountType {
    CARD, CASH, MONO
}
