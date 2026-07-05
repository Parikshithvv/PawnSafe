package com.pawnsafe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pledge_book")
data class PledgeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ticketNo: String,
    val date: String,
    val name: String,
    val relation: String? = null,
    val cross: String? = null,
    val place: String? = null,
    val post: String? = null,
    val taluk: String? = null,
    val hobli: String? = null,
    val profession: String? = null,
    val phone: String? = null,
    val loanAmountRs: String,
    val loanAmountWords: String? = null,
    val articleDescription: String? = null,
    val purity: String? = null,
    val grossWeightG: String? = null,
    val grossWeightM: String? = null,
    val nettWeightG: String? = null,
    val nettWeightM: String? = null,
    val presentValue: String? = null,
    val status: String = "ACTIVE",
    val shopId: String? = null,
    val customerId: String? = null,
    val isSynced: Boolean = false,
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)