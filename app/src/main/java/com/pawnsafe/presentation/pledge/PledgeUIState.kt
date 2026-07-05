package com.pawnsafe.presentation.pledge

import com.pawnsafe.domain.model.Pledge

sealed class PledgeUIState {
    object Idle    : PledgeUIState()
    object Loading : PledgeUIState()
    data class Success(val data: List<Pledge>) : PledgeUIState()
    data class Error(val message: String)      : PledgeUIState()
}

sealed class PledgeFormState {
    object Idle       : PledgeFormState()
    object Loading    : PledgeFormState()
    object Saved      : PledgeFormState()
    data class Error(val message: String) : PledgeFormState()
}
