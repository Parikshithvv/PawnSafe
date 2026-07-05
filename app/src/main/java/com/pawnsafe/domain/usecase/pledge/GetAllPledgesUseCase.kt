package com.pawnsafe.domain.usecase.pledge

import com.pawnsafe.domain.model.Pledge
import com.pawnsafe.domain.repository.IPledgeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllPledgesUseCase @Inject constructor(
    private val repository: IPledgeRepository
) {
    operator fun invoke(): Flow<List<Pledge>> =
        repository.getAllPledges()
}
