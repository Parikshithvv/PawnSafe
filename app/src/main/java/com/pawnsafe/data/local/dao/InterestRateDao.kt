package com.pawnsafe.data.local.dao

import androidx.room.*
import com.pawnsafe.data.local.entity.InterestRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InterestRateDao {

    @Query("SELECT * FROM interest_rates ORDER BY effectiveFrom DESC")
    fun getAllRates(): Flow<List<InterestRateEntity>>

    @Query("SELECT * FROM interest_rates WHERE effectiveTo IS NULL LIMIT 1")
    suspend fun getActiveRate(): InterestRateEntity?

    @Query("SELECT * FROM interest_rates WHERE effectiveFrom <= :date AND (effectiveTo IS NULL OR effectiveTo >= :date) LIMIT 1")
    suspend fun getRateForDate(date: String): InterestRateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(rate: InterestRateEntity): Long

    @Update
    suspend fun updateRate(rate: InterestRateEntity)

    @Query("UPDATE interest_rates SET effectiveTo = :endDate WHERE effectiveTo IS NULL")
    suspend fun closeCurrentRate(endDate: String)

    @Query("SELECT COUNT(*) FROM interest_rates")
    suspend fun count(): Int
}