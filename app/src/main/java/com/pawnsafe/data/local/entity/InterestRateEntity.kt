package com.pawnsafe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interest_rates")
data class InterestRateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val rate: Double,                    // e.g. 1.16
    val effectiveFrom: String,           // ISO 8601: yyyy-MM-dd
    val effectiveTo: String? = null,     // NULL = currently active
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
