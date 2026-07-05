package com.pawnsafe.data.mapper

import com.pawnsafe.data.local.entity.PledgeEntity
import com.pawnsafe.domain.model.Pledge

fun PledgeEntity.toDomain(): Pledge = Pledge(
    id                 = id,
    ticketNo           = ticketNo,
    date               = date,
    name               = name,
    relation           = relation,
    cross              = cross,
    place              = place,
    post               = post,
    taluk              = taluk,
    hobli              = hobli,
    profession         = profession,
    phone              = phone,
    loanAmountRs       = loanAmountRs,
    loanAmountWords    = loanAmountWords,
    articleDescription = articleDescription,
    purity             = purity,
    grossWeightG       = grossWeightG,
    grossWeightM       = grossWeightM,
    nettWeightG        = nettWeightG,
    nettWeightM        = nettWeightM,
    presentValue       = presentValue,
    status             = status,
    shopId             = shopId,
    customerId         = customerId,
    isSynced           = isSynced,
    photoUri           = photoUri,
    createdAt          = createdAt
)

fun Pledge.toEntity(): PledgeEntity = PledgeEntity(
    id                 = id,
    ticketNo           = ticketNo,
    date               = date,
    name               = name,
    relation           = relation,
    cross              = cross,
    place              = place,
    post               = post,
    taluk              = taluk,
    hobli              = hobli,
    profession         = profession,
    phone              = phone,
    loanAmountRs       = loanAmountRs,
    loanAmountWords    = loanAmountWords,
    articleDescription = articleDescription,
    purity             = purity,
    grossWeightG       = grossWeightG,
    grossWeightM       = grossWeightM,
    nettWeightG        = nettWeightG,
    nettWeightM        = nettWeightM,
    presentValue       = presentValue,
    status             = status,
    shopId             = shopId,
    customerId         = customerId,
    isSynced           = isSynced,
    photoUri           = photoUri,
    createdAt          = createdAt
)