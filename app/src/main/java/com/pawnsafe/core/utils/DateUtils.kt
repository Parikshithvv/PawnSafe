package com.pawnsafe.core.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {

    private val ISO_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val DISPLAY_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /** Parse ISO 8601 string (yyyy-MM-dd) → LocalDate */
    fun parseIso(isoDate: String): LocalDate =
        LocalDate.parse(isoDate, ISO_FMT)

    /** Parse display string (dd/MM/yyyy) → LocalDate */
    fun parseDisplay(displayDate: String): LocalDate =
        LocalDate.parse(displayDate, DISPLAY_FMT)

    /** LocalDate → ISO 8601 string (yyyy-MM-dd) */
    fun toIso(date: LocalDate): String =
        date.format(ISO_FMT)

    /** LocalDate → display string (dd/MM/yyyy) */
    fun toDisplay(date: LocalDate): String =
        date.format(DISPLAY_FMT)

    /** ISO 8601 string → display string (dd/MM/yyyy) */
    fun isoToDisplay(isoDate: String): String =
        toDisplay(parseIso(isoDate))

    /** display string → ISO 8601 string */
    fun displayToIso(displayDate: String): String =
        toIso(parseDisplay(displayDate))

    /**
     * Calculate inclusive day count between two ISO dates.
     * e.g. pledgeDate=2024-01-01, returnDate=2024-01-10 → 10 days
     */
    fun daysBetweenInclusive(pledgeDateIso: String, returnDateIso: String): Int {
        val start = parseIso(pledgeDateIso)
        val end   = parseIso(returnDateIso)
        return (ChronoUnit.DAYS.between(start, end) + 1).toInt()
    }

    /** Today as ISO 8601 string */
    fun todayIso(): String = toIso(LocalDate.now())

    /** Today as display string */
    fun todayDisplay(): String = toDisplay(LocalDate.now())

    /**
     * Check if a pledge is overdue: pledgeDate is > 365 days ago.
     * pledgeDateIso must be ISO 8601.
     */
    fun isOverdue(pledgeDateIso: String): Boolean {
        val pledgeDate = parseIso(pledgeDateIso)
        return ChronoUnit.DAYS.between(pledgeDate, LocalDate.now()) > 365
    }
}
