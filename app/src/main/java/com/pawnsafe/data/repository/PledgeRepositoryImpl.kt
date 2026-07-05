package com.pawnsafe.data.repository

import com.pawnsafe.data.local.dao.PledgeDao
import com.pawnsafe.data.mapper.toDomain
import com.pawnsafe.data.mapper.toEntity
import com.pawnsafe.domain.model.Pledge
import com.pawnsafe.domain.repository.IPledgeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PledgeRepositoryImpl @Inject constructor(
    private val dao: PledgeDao
) : IPledgeRepository {

    override fun getAllPledges(): Flow<List<Pledge>> =
        dao.getAllPledges().map { list -> list.map { it.toDomain() } }

    override fun searchPledges(query: String): Flow<List<Pledge>> =
        dao.searchPledges(query).map { list -> list.map { it.toDomain() } }

    override fun getPledgesByStatus(status: String): Flow<List<Pledge>> =
        dao.getPledgesByStatus(status).map { list -> list.map { it.toDomain() } }

    override suspend fun getPledgeById(id: Int): Pledge? =
        dao.getPledgeById(id)?.toDomain()

    override suspend fun getByTicketNo(ticketNo: String): Pledge? =
        dao.getPledgeByTicketNo(ticketNo)?.toDomain()

    override suspend fun addPledge(pledge: Pledge): Long =
        dao.insert(pledge.toEntity())

    override suspend fun updatePledge(pledge: Pledge) =
        dao.update(pledge.toEntity())

    override suspend fun updateStatus(id: Int, status: String) =
        dao.updateStatus(id, status)

    override suspend fun getAllOnce(): List<Pledge> =
        dao.getAllPledgesOnce().map { it.toDomain() }

    override suspend fun getLatestByPhone(phone: String): Pledge? =
        dao.getLatestByPhone(phone)?.toDomain()

    override suspend fun getAllByPhone(phone: String): List<Pledge> =
        dao.getAllByPhone(phone).map { it.toDomain() }

    override suspend fun getActiveByPhone(phone: String): List<Pledge> =
        dao.getActiveByPhone(phone).map { it.toDomain() }
}