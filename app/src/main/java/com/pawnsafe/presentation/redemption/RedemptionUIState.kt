package com.pawnsafe.presentation.redemption

import com.pawnsafe.domain.model.Redemption
import com.pawnsafe.core.utils.InterestCalculator

sealed class RedemptionUIState {
    object Idle    : RedemptionUIState()
    object Loading : RedemptionUIState()
    data class Success(val data: List<Redemption>) : RedemptionUIState()
    data class Error(val message: String)          : RedemptionUIState()
}

sealed class RedemptionFormState {
    object Idle    : RedemptionFormState()
    object Loading : RedemptionFormState()
    object Saved   : RedemptionFormState()
    data class InterestReady(val result: InterestCalculator.InterestResult) : RedemptionFormState()
    data class Error(val message: String) : RedemptionFormState()
}
