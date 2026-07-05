package com.pawnsafe.domain.usecase.redemption

import com.pawnsafe.domain.model.Redemption
import com.pawnsafe.domain.repository.IRedemptionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllRedemptionsUseCase @Inject constructor(
    private val repository: IRedemptionRepository
) {
    operator fun invoke(): Flow<List<Redemption>> =
        repository.getAllRedemptions()
}
