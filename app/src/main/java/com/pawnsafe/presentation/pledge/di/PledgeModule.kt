package com.pawnsafe.presentation.pledge.di

import com.pawnsafe.data.repository.PledgeRepositoryImpl
import com.pawnsafe.domain.repository.IPledgeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PledgeModule {

    @Binds
    @Singleton
    abstract fun bindPledgeRepository(
        impl: PledgeRepositoryImpl
    ): IPledgeRepository
}
