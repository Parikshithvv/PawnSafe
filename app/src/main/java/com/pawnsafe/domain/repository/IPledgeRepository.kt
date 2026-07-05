package com.pawnsafe.domain.repository

import com.pawnsafe.domain.model.Pledge
import kotlinx.coroutines.flow.Flow

interface IPledgeRepository {
    fun getAllPledges(): Flow<List<Pledge>>
    fun searchPledges(query: String): Flow<List<Pledge>>
    fun getPledgesByStatus(status: String): Flow<List<Pledge>>
    suspend fun getPledgeById(id: Int): Pledge?
    suspend fun getByTicketNo(ticketNo: String): Pledge?
    suspend fun addPledge(pledge: Pledge): Long
    suspend fun updatePledge(pledge: Pledge)
    suspend fun updateStatus(id: Int, status: String)
    suspend fun getAllOnce(): List<Pledge>
    suspend fun getLatestByPhone(phone: String): Pledge?
    suspend fun getAllByPhone(phone: String): List<Pledge>
    suspend fun getActiveByPhone(phone: String): List<Pledge>
}