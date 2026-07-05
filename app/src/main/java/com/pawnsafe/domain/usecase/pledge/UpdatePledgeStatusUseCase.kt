package com.pawnsafe.domain.usecase.pledge

import com.pawnsafe.domain.repository.IPledgeRepository
import javax.inject.Inject

class UpdatePledgeStatusUseCase @Inject constructor(
    private val repository: IPledgeRepository
) {
    suspend operator fun invoke(id: Int, status: String) =
        repository.updateStatus(id, status)
}
