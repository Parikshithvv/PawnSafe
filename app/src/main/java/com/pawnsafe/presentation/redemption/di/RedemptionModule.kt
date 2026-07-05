package com.pawnsafe.presentation.redemption.di

import com.pawnsafe.data.repository.RedemptionRepositoryImpl
import com.pawnsafe.domain.repository.IRedemptionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RedemptionModule {

    @Binds
    @Singleton
    abstract fun bindRedemptionRepository(
        impl: RedemptionRepositoryImpl
    ): IRedemptionRepository
}
