package com.harukeyua.fintrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity
data class SyncInfo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val syncDateTime: OffsetDateTime,
    val isSuccess: Boolean
)
