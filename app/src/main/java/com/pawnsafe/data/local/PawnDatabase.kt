package com.pawnsafe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pawnsafe.data.local.dao.InterestRateDao
import com.pawnsafe.data.local.dao.PledgeDao
import com.pawnsafe.data.local.dao.RedemptionDao
import com.pawnsafe.data.local.entity.InterestRateEntity
import com.pawnsafe.data.local.entity.PledgeEntity
import com.pawnsafe.data.local.entity.RedemptionEntity

@Database(
    entities = [
        PledgeEntity::class,
        RedemptionEntity::class,
        InterestRateEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PawnDatabase : RoomDatabase() {

    abstract fun pledgeDao(): PledgeDao
    abstract fun redemptionDao(): RedemptionDao
    abstract fun interestRateDao(): InterestRateDao

    companion object {
        const val DATABASE_NAME = "pawn_safe.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pledge_book ADD COLUMN photoUri TEXT")
            }
        }

        fun seedCallback() = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    """
                    INSERT INTO interest_rates (rate, effectiveFrom, effectiveTo, note, createdAt)
                    VALUES (1.16, '2024-01-01', NULL, 'Default rate', ${System.currentTimeMillis()})
                    """.trimIndent()
                )
            }
        }
    }
}