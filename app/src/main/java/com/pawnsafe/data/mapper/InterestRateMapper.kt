package com.pawnsafe.data.mapper

import com.pawnsafe.data.local.entity.InterestRateEntity
import com.pawnsafe.domain.model.InterestRate

fun InterestRateEntity.toDomain(): InterestRate = InterestRate(
    id            = id,
    rate          = rate,
    effectiveFrom = effectiveFrom,
    effectiveTo   = effectiveTo,
    note          = note,
    createdAt     = createdAt
)

fun InterestRate.toEntity(): InterestRateEntity = InterestRateEntity(
    id            = id,
    rate          = rate,
    effectiveFrom = effectiveFrom,
    effectiveTo   = effectiveTo,
    note          = note,
    createdAt     = createdAt
)
