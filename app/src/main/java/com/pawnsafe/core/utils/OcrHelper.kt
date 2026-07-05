package com.pawnsafe.core.utils

import android.util.Log
import com.google.mlkit.vision.text.Text

data class OcrResult(
    val fields: Map<String, String>,
    val filledKeys: Set<String>
) {
    fun isEmpty() = fields.isEmpty()
}

object OcrHelper {

    private const val TAG = "OcrHelper"

    object Fields {
        const val TICKET_NO      = "ticketNo"
        const val DATE           = "date"
        const val NAME           = "name"
        const val RELATION       = "relation"
        const val CROSS          = "cross"
        const val PLACE          = "place"
        const val TALUK          = "taluk"
        const val HOBLI          = "hobli"
        const val PHONE          = "phone"
        const val PROFESSION     = "profession"
        const val LOAN_AMOUNT    = "loanAmount"
        const val PURITY         = "purity"
        const val GROSS_WEIGHT_G = "grossWeightG"
        const val GROSS_WEIGHT_M = "grossWeightM"
        const val NETT_WEIGHT_G  = "nettWeightG"
        const val NETT_WEIGHT_M  = "nettWeightM"
        const val PRESENT_VALUE  = "presentValue"
    }

    fun extractFields(visionText: Text): OcrResult {
        val lines = visionText.textBlocks
            .flatMap { it.lines }
            .map { it.text.trim() }
            .filter { it.isNotBlank() }

        Log.d(TAG, "=== RAW OCR LINES ===")
        lines.forEachIndexed { i, l -> Log.d(TAG, "[$i] $l") }

        val result = mutableMapOf<String, String>()
        val fullText = visionText.text

        for (line in lines.take(10)) {
            val m = Regex("""(?:No\.?\s*:?\s*)?(\d{3,5})""").find(line)
            if (m != null) {
                val v = m.groupValues[1]
                if (v.length >= 3) { result[Fields.TICKET_NO] = v; break }
            }
        }

        Regex("""(\d{1,2})[\/\-\.](\d{1,2})[\/\-\.](\d{2,4})""")
            .find(fullText)?.let { m ->
                val d  = m.groupValues[1].padStart(2, '0')
                val mo = m.groupValues[2].padStart(2, '0')
                val yr = m.groupValues[3].let { if (it.length == 2) "20$it" else it }
                result[Fields.DATE] = "$yr-$mo-$d"
            }

        extractSameLine(lines, listOf("Name"))?.let { result[Fields.NAME] = it }

        for (line in lines) {
            val m = Regex(
                """[WwSsDd][\/\\][oO0]\s+[WwSsDd][\/\\][oO0]\s+[WwSsDd][\/\\][oO0]\s*[.\s]*([\w\s]+)"""
            ).find(line)
            if (m != null) {
                val v = m.groupValues[1].trim().trimDots()
                if (v.isNotBlank()) { result[Fields.RELATION] = v; break }
            }
            val m2 = Regex("""^[WwSsDd][\/\\][oO0][\s.]+([\w\s]{2,30})$""").find(line)
            if (m2 != null) {
                val v = m2.groupValues[1].trim().trimDots()
                if (v.isNotBlank()) { result[Fields.RELATION] = v; break }
            }
        }

        extractSameLine(lines, listOf("Cross", "CROSS"))?.let { result[Fields.CROSS] = it }
        extractSameLine(lines, listOf("Place", "PLACE"))?.let { result[Fields.PLACE] = it }
        extractSameLine(lines, listOf("Taluk", "TALUK", "Taluk/Town"))?.let { result[Fields.TALUK] = it }
        extractSameLine(lines, listOf("Hobli", "HOBLI"))?.let { result[Fields.HOBLI] = it }

        Regex("""(?:Ph|Phone|Mob|Mobile|Tel)[h]?[.\s:]*(\d{10})""", RegexOption.IGNORE_CASE)
            .find(fullText)?.let { result[Fields.PHONE] = it.groupValues[1] }
            ?: run {
                Regex("""(?<!\d)([6-9]\d{9})(?!\d)""").find(fullText)
                    ?.let { result[Fields.PHONE] = it.groupValues[1] }
            }

        extractSameLine(lines, listOf("Profession", "Working", "Profession/Working"))
            ?.let { result[Fields.PROFESSION] = it }

        for (i in lines.indices) {
            val line = lines[i]
            if (line.contains("Principal", ignoreCase = true) ||
                line.contains("Loan Amount", ignoreCase = true) ||
                line.contains("Amount Rs", ignoreCase = true)) {
                val m = Regex("""(?:Rs[\/.]?\s*)(\d[\d,]{2,})""", RegexOption.IGNORE_CASE).find(line)
                if (m != null) { result[Fields.LOAN_AMOUNT] = m.groupValues[1].replace(",", ""); break }
                if (i + 1 < lines.size) {
                    val m2 = Regex("""(\d[\d,]{2,})""").find(lines[i + 1])
                    if (m2 != null) { result[Fields.LOAN_AMOUNT] = m2.groupValues[1].replace(",", ""); break }
                }
            }
        }
        if (!result.containsKey(Fields.LOAN_AMOUNT)) {
            Regex("""Rs[\/]\s*(\d[\d,]{2,})""", RegexOption.IGNORE_CASE).find(fullText)
                ?.let { result[Fields.LOAN_AMOUNT] = it.groupValues[1].replace(",", "") }
        }

        for (i in lines.indices) {
            if (lines[i].contains("Purity", ignoreCase = true)) {
                val m = Regex("""(?:Purity\s*[:\|]?\s*)(\d{2,3}[Kk]?|\d{3})""", RegexOption.IGNORE_CASE).find(lines[i])
                if (m != null) { result[Fields.PURITY] = m.groupValues[1]; break }
                if (i + 1 < lines.size) {
                    val next = lines[i + 1].trim()
                    if (next.matches(Regex("""\d{2,3}[Kk]?"""))) { result[Fields.PURITY] = next; break }
                }
            }
        }

        for (i in lines.indices) {
            val line = lines[i]
            if (line.contains("Gross", ignoreCase = true)) {
                parseWeight(lines, i)?.let { (g, m) ->
                    result[Fields.GROSS_WEIGHT_G] = g
                    result[Fields.GROSS_WEIGHT_M] = m
                }
            }
            if (line.contains("Nett", ignoreCase = true) || line.contains("Net", ignoreCase = true)) {
                parseWeight(lines, i)?.let { (g, m) ->
                    result[Fields.NETT_WEIGHT_G] = g
                    result[Fields.NETT_WEIGHT_M] = m
                }
            }
        }

        for (i in lines.indices) {
            if (lines[i].contains("Present", ignoreCase = true) || lines[i].contains("Value", ignoreCase = true)) {
                if (i + 1 < lines.size) {
                    val m = Regex("""(\d[\d,]{2,})""").find(lines[i + 1])
                    if (m != null) { result[Fields.PRESENT_VALUE] = m.groupValues[1].replace(",", ""); break }
                }
            }
        }

        val filledKeys = result.filterValues { it.isNotBlank() }.keys.toSet()
        Log.d(TAG, "=== EXTRACTED: $result ===")
        Log.d(TAG, "=== FILLED BY OCR: $filledKeys ===")
        return OcrResult(fields = result, filledKeys = filledKeys)
    }

    private fun extractSameLine(lines: List<String>, labels: List<String>): String? {
        for (line in lines) {
            for (label in labels) {
                val idx = line.indexOf(label, ignoreCase = true)
                if (idx != -1) {
                    val after = line.substring(idx + label.length)
                        .trimStart('.', '-', ':', ' ', '\t', '/')
                        .trim()
                    if (after.length >= 2 &&
                        !after.contains("JEWELLERS", ignoreCase = true) &&
                        !after.contains("BROKER", ignoreCase = true) &&
                        !after.contains("NANJUND", ignoreCase = true)) {
                        return after.trimDots()
                    }
                }
            }
        }
        return null
    }

    private fun parseWeight(lines: List<String>, labelIdx: Int): Pair<String, String>? {
        for (offset in 0..2) {
            val idx = labelIdx + offset
            if (idx >= lines.size) break
            val line = lines[idx]
            val m1 = Regex("""(\d{1,3})\s*[\-\.]\s*(\d{1,4})""").find(line)
            if (m1 != null) return Pair(m1.groupValues[1], m1.groupValues[2])
            val m2 = Regex("""(\d{1,3})\s{2,}(\d{1,4})""").find(line)
            if (m2 != null) return Pair(m2.groupValues[1], m2.groupValues[2])
        }
        return null
    }

    private fun String.trimDots() = this.trim('.', '-', ' ', '\t', '/', '\\', '|', '_')
}