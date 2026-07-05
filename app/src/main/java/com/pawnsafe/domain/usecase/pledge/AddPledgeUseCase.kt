package com.pawnsafe.domain.usecase.pledge

import com.pawnsafe.domain.model.Pledge
import com.pawnsafe.domain.repository.IPledgeRepository
import javax.inject.Inject

class AddPledgeUseCase @Inject constructor(
    private val repository: IPledgeRepository
) {
    suspend operator fun invoke(pledge: Pledge): Long =
        repository.addPledge(pledge)
}
