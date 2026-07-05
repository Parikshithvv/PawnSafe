package com.pawnsafe.core.utils

import kotlin.math.floor

/**
 * Stateless interest calculator — rate is always passed as a parameter,
 * never hardcoded, so the same logic works for historical rate lookups
 * and future Ktor backend reuse.
 *
 * Formula:
 *   interest = principal × (rate / 100) ÷ 30 × numberOfDays
 *   total    = principal + interest
 */
object InterestCalculator {

    data class InterestResult(
        val principal: Double,
        val rate: Double,          // e.g. 1.16
        val numberOfDays: Int,
        val interestRs: Double,
        val interestPs: Double,    // paise (fractional rupees × 100)
        val totalAmount: Double
    )

    /**
     * @param principal    Loan amount in rupees (e.g. 10000.0)
     * @param rate         Monthly rate percent (e.g. 1.16 for 1.16%)
     * @param numberOfDays Inclusive day count from pledge to return
     */
    fun calculate(principal: Double, rate: Double, numberOfDays: Int): InterestResult {
        val interest = principal * (rate / 100.0) / 30.0 * numberOfDays
        val interestRs = floor(interest)
        val interestPs = (interest - interestRs) * 100
        val total = principal + interest
        return InterestResult(
            principal    = principal,
            rate         = rate,
            numberOfDays = numberOfDays,
            interestRs   = interestRs,
            interestPs   = interestPs,
            totalAmount  = total
        )
    }

    /**
     * Convenience overload: derive numberOfDays from ISO date strings.
     * pledgeDateIso and returnDateIso must be yyyy-MM-dd.
     */
    fun calculate(
        principal: Double,
        rate: Double,
        pledgeDateIso: String,
        returnDateIso: String
    ): InterestResult {
        val days = DateUtils.daysBetweenInclusive(pledgeDateIso, returnDateIso)
        return calculate(principal, rate, days)
    }
}
