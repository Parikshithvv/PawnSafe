package com.pawnsafe.domain.model

data class InterestRate(
    val id: Int = 0,
    val rate: Double,
    val effectiveFrom: String,           // ISO 8601: yyyy-MM-dd
    val effectiveTo: String? = null,     // null = currently active
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
