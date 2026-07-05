package com.pawnsafe.core.utils

object NumberToWords {

    private val ones = listOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    )
    private val tens = listOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    private fun twoDigits(n: Int): String {
        return when {
            n < 20  -> ones[n]
            n % 10 == 0 -> tens[n / 10]
            else    -> "${tens[n / 10]} ${ones[n % 10]}"
        }
    }

    private fun threeDigits(n: Int): String {
        return if (n >= 100)
            "${ones[n / 100]} Hundred ${twoDigits(n % 100)}".trim()
        else
            twoDigits(n)
    }

    fun convert(amount: Long): String {
        if (amount == 0L) return "Zero Rupees Only"
        var n = amount
        val parts = mutableListOf<String>()

        val crore = n / 10_000_000
        n %= 10_000_000
        val lakh = n / 100_000
        n %= 100_000
        val thousand = n / 1_000
        n %= 1_000
        val rest = n.toInt()

        if (crore > 0)    parts.add("${threeDigits(crore.toInt())} Crore")
        if (lakh > 0)     parts.add("${threeDigits(lakh.toInt())} Lakh")
        if (thousand > 0) parts.add("${threeDigits(thousand.toInt())} Thousand")
        if (rest > 0)     parts.add(threeDigits(rest))

        return parts.joinToString(" ") + " Rupees Only"
    }
}