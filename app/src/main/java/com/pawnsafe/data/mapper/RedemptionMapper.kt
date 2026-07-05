package com.pawnsafe.data.mapper

import com.pawnsafe.data.local.entity.RedemptionEntity
import com.pawnsafe.domain.model.Redemption

fun RedemptionEntity.toDomain(): Redemption = Redemption(
    id           = id,
    pledgeId     = pledgeId,
    ticketNo     = ticketNo,
    customerName = customerName,
    address      = address,
    pledgeDate   = pledgeDate,
    returnDate   = returnDate,
    numberOfDays = numberOfDays,
    principalRs  = principalRs,
    principalPs  = principalPs,
    interestRs   = interestRs,
    interestPs   = interestPs,
    totalAmount  = totalAmount,
    shopId       = shopId,
    customerId   = customerId,
    isSynced     = isSynced,
    createdAt    = createdAt
)

fun Redemption.toEntity(): RedemptionEntity = RedemptionEntity(
    id           = id,
    pledgeId     = pledgeId,
    ticketNo     = ticketNo,
    customerName = customerName,
    address      = address,
    pledgeDate   = pledgeDate,
    returnDate   = returnDate,
    numberOfDays = numberOfDays,
    principalRs  = principalRs,
    principalPs  = principalPs,
    interestRs   = interestRs,
    interestPs   = interestPs,
    totalAmount  = totalAmount,
    shopId       = shopId,
    customerId   = customerId,
    isSynced     = isSynced,
    createdAt    = createdAt
)
