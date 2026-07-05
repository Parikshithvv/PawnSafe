package com.pawnsafe.data.di

import android.content.Context
import androidx.room.Room
import com.pawnsafe.data.local.PawnDatabase
import com.pawnsafe.data.local.dao.InterestRateDao
import com.pawnsafe.data.local.dao.PledgeDao
import com.pawnsafe.data.local.dao.RedemptionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePawnDatabase(@ApplicationContext context: Context): PawnDatabase =
        Room.databaseBuilder(context, PawnDatabase::class.java, PawnDatabase.DATABASE_NAME)
            .addMigrations(PawnDatabase.MIGRATION_1_2)
            .addCallback(PawnDatabase.seedCallback())
            .build()

    @Provides
    fun providePledgeDao(db: PawnDatabase): PledgeDao = db.pledgeDao()

    @Provides
    fun provideRedemptionDao(db: PawnDatabase): RedemptionDao = db.redemptionDao()

    @Provides
    fun provideInterestRateDao(db: PawnDatabase): InterestRateDao = db.interestRateDao()
}