package com.pawnsafe.data.local.dao

import androidx.room.*
import com.pawnsafe.data.local.entity.PledgeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PledgeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PledgeEntity): Long

    @Update
    suspend fun update(entity: PledgeEntity)

    @Delete
    suspend fun delete(entity: PledgeEntity)

    @Query("SELECT * FROM pledge_book ORDER BY createdAt DESC")
    fun getAllPledges(): Flow<List<PledgeEntity>>

    @Query("SELECT * FROM pledge_book WHERE id = :id")
    suspend fun getPledgeById(id: Int): PledgeEntity?

    @Query("SELECT * FROM pledge_book WHERE id = :id")
    fun getPledgeByIdFlow(id: Int): Flow<PledgeEntity?>

    @Query("SELECT * FROM pledge_book WHERE ticketNo = :ticketNo LIMIT 1")
    suspend fun getPledgeByTicketNo(ticketNo: String): PledgeEntity?

    @Query("SELECT * FROM pledge_book WHERE name LIKE '%' || :query || '%' OR ticketNo LIKE '%' || :query || '%'")
    fun searchPledges(query: String): Flow<List<PledgeEntity>>

    @Query("SELECT * FROM pledge_book WHERE status = :status ORDER BY createdAt DESC")
    fun getPledgesByStatus(status: String): Flow<List<PledgeEntity>>

    @Query("SELECT * FROM pledge_book ORDER BY createdAt DESC")
    suspend fun getAllPledgesOnce(): List<PledgeEntity>

    @Query("UPDATE pledge_book SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    // Latest pledge by phone — for customer profile auto-fill
    @Query("SELECT * FROM pledge_book WHERE phone = :phone ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestByPhone(phone: String): PledgeEntity?

    // All pledges by phone — for history list
    @Query("SELECT * FROM pledge_book WHERE phone = :phone ORDER BY createdAt DESC")
    suspend fun getAllByPhone(phone: String): List<PledgeEntity>

    // Active (unredeemed) pledges by phone — for overdue warning
    @Query("SELECT * FROM pledge_book WHERE phone = :phone AND status = 'ACTIVE' ORDER BY createdAt DESC")
    suspend fun getActiveByPhone(phone: String): List<PledgeEntity>
}