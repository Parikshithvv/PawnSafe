package com.pawnsafe.domain.usecase.redemption

import com.pawnsafe.domain.model.Redemption
import com.pawnsafe.domain.repository.IRedemptionRepository
import com.pawnsafe.domain.repository.IPledgeRepository
import javax.inject.Inject

class AddRedemptionUseCase @Inject constructor(
    private val redemptionRepository: IRedemptionRepository,
    private val pledgeRepository: IPledgeRepository
) {
    suspend operator fun invoke(redemption: Redemption): Long {
        val id = redemptionRepository.addRedemption(redemption)
        // Auto-mark the pledge as REDEEMED
        pledgeRepository.updateStatus(redemption.pledgeId, "REDEEMED")
        return id
    }
}
