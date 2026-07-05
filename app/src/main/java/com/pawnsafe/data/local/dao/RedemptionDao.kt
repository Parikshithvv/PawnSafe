package com.pawnsafe.data.local.dao

import androidx.room.*
import com.pawnsafe.data.local.entity.RedemptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RedemptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RedemptionEntity): Long

    @Update
    suspend fun update(entity: RedemptionEntity)

    @Delete
    suspend fun delete(entity: RedemptionEntity)

    @Query("SELECT * FROM redemption_book ORDER BY createdAt DESC")
    fun getAllRedemptions(): Flow<List<RedemptionEntity>>

    @Query("SELECT * FROM redemption_book WHERE pledgeId = :pledgeId ORDER BY createdAt DESC")
    fun getRedemptionsByPledgeId(pledgeId: Int): Flow<List<RedemptionEntity>>

    @Query("SELECT * FROM redemption_book WHERE id = :id")
    suspend fun getRedemptionById(id: Int): RedemptionEntity?

    @Query("SELECT * FROM redemption_book ORDER BY createdAt DESC")
    suspend fun getAllRedemptionsOnce(): List<RedemptionEntity>
}
