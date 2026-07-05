package com.pawnsafe.domain.usecase.pledge

import com.pawnsafe.domain.model.Pledge
import com.pawnsafe.domain.repository.IPledgeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchPledgeUseCase @Inject constructor(
    private val repository: IPledgeRepository
) {
    operator fun invoke(query: String): Flow<List<Pledge>> =
        repository.searchPledges(query)
}
