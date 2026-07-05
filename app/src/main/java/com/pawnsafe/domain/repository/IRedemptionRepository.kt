package com.pawnsafe.domain.repository

import com.pawnsafe.domain.model.Redemption
import kotlinx.coroutines.flow.Flow

interface IRedemptionRepository {
    fun getAllRedemptions(): Flow<List<Redemption>>
    fun getRedemptionsByPledgeId(pledgeId: Int): Flow<List<Redemption>>
    suspend fun getRedemptionById(id: Int): Redemption?
    suspend fun addRedemption(redemption: Redemption): Long
    suspend fun getAllOnce(): List<Redemption>
}
