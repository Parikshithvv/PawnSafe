package com.pawnsafe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "redemption_book")
data class RedemptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val pledgeId: Int,
    val ticketNo: String,
    val customerName: String,
    val address: String? = null,
    val pledgeDate: String,              // ISO 8601: yyyy-MM-dd
    val returnDate: String,              // ISO 8601: yyyy-MM-dd
    val numberOfDays: Int,
    val principalRs: Double,
    val principalPs: Double = 0.0,
    val interestRs: Double,
    val interestPs: Double = 0.0,
    val totalAmount: Double,
    val shopId: String? = null,
    val customerId: String? = null,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
