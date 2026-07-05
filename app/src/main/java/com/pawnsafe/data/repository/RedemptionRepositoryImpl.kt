package com.pawnsafe.data.repository

import com.pawnsafe.data.local.dao.RedemptionDao
import com.pawnsafe.data.mapper.toDomain
import com.pawnsafe.data.mapper.toEntity
import com.pawnsafe.domain.model.Redemption
import com.pawnsafe.domain.repository.IRedemptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RedemptionRepositoryImpl @Inject constructor(
    private val dao: RedemptionDao
) : IRedemptionRepository {

    override fun getAllRedemptions(): Flow<List<Redemption>> =
        dao.getAllRedemptions().map { list -> list.map { it.toDomain() } }

    override fun getRedemptionsByPledgeId(pledgeId: Int): Flow<List<Redemption>> =
        dao.getRedemptionsByPledgeId(pledgeId).map { list -> list.map { it.toDomain() } }

    override suspend fun getRedemptionById(id: Int): Redemption? =
        dao.getRedemptionById(id)?.toDomain()

    override suspend fun addRedemption(redemption: Redemption): Long =
        dao.insert(redemption.toEntity())

    override suspend fun getAllOnce(): List<Redemption> =
        dao.getAllRedemptionsOnce().map { it.toDomain() }
}
