package com.pawnsafe.core.utils

import android.content.Context
import android.net.Uri
import com.pawnsafe.domain.model.Pledge
import com.pawnsafe.domain.model.Redemption
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

object ExcelExporter {

    private val PLEDGE_COL_WIDTHS   = intArrayOf(12,14,24,16,20,14,14,16,24,10,14,14,14,10)
    private val REDEMPTION_COL_WIDTHS = intArrayOf(14,12,28,14,16,16,16,8)

    fun export(
        context: Context,
        uri: Uri,
        pledges: List<Pledge>,
        redemptions: List<Redemption>
    ) {
        // Point POI temp files at app cache dir — writable on all Android versions
        val tmpDir = File(context.cacheDir, "poi_tmp").also { it.mkdirs() }
        System.setProperty("org.apache.poi.util.TempFile.tmpdir", tmpDir.absolutePath)

        // Use plain XSSFWorkbook — simpler, reliable, fine for <5000 rows
        // SXSSFWorkbook needs correct tmp dir which varies by device
        val baos = ByteArrayOutputStream(512 * 1024)
        val wb   = XSSFWorkbook()
        try {
            writePledgeSheet(wb, pledges)
            writeRedemptionSheet(wb, redemptions)
            writeSummarySheet(wb, pledges, redemptions)
            wb.write(baos)
        } finally {
            wb.close()
        }

        val bytes = baos.toByteArray()
        if (bytes.size < 200)
            throw IOException("Export produced invalid file — only ${bytes.size} bytes")

        val stream = context.contentResolver.openOutputStream(uri)
            ?: throw IOException("Cannot open output stream — try picking the file location again")
        stream.use { out -> out.write(bytes); out.flush() }
    }

    // ── Pledge Sheet ──────────────────────────────────────────────────────────

    private val PLEDGE_HEADERS = listOf(
        "Ticket No","Date","Name","Relation","Place","Taluk",
        "Phone","Loan Amount (Rs)","Article","Purity",
        "Gross Wt","Nett Wt","Present Value","Status"
    )

    private fun writePledgeSheet(wb: XSSFWorkbook, pledges: List<Pledge>) {
        val sheet = wb.createSheet("Pledge Book")
        val hs    = headerStyle(wb)
        PLEDGE_COL_WIDTHS.forEachIndexed { i, w -> sheet.setColumnWidth(i, w * 256) }
        val hr = sheet.createRow(0)
        PLEDGE_HEADERS.forEachIndexed { i, t ->
            hr.createCell(i).also { it.setCellValue(t); it.cellStyle = hs }
        }
        pledges.forEachIndexed { idx, p ->
            val row = sheet.createRow(idx + 1)
            row.createCell(0).setCellValue(p.ticketNo)
            row.createCell(1).setCellValue(DateUtils.isoToDisplay(p.date))
            row.createCell(2).setCellValue(p.name)
            row.createCell(3).setCellValue(p.relation ?: "")
            row.createCell(4).setCellValue(listOfNotNull(p.place, p.post).joinToString(", "))
            row.createCell(5).setCellValue(p.taluk ?: "")
            row.createCell(6).setCellValue(p.phone ?: "")
            row.createCell(7).setCellValue(p.loanAmountRs)
            row.createCell(8).setCellValue(p.articleDescription ?: "")
            row.createCell(9).setCellValue(p.purity ?: "")
            row.createCell(10).setCellValue("${p.grossWeightG ?: ""}g ${p.grossWeightM ?: ""}m")
            row.createCell(11).setCellValue("${p.nettWeightG ?: ""}g ${p.nettWeightM ?: ""}m")
            row.createCell(12).setCellValue(p.presentValue ?: "")
            row.createCell(13).setCellValue(p.status)
        }
    }

    // ── Redemption Sheet ──────────────────────────────────────────────────────

    private val REDEMPTION_HEADERS = listOf(
        "Date of Delivery","Pledge No","Name & Address",
        "Date of Pledge","Principal (Rs)","Interest (Rs)","Total (Rs)","Days"
    )

    private fun writeRedemptionSheet(wb: XSSFWorkbook, redemptions: List<Redemption>) {
        val sheet = wb.createSheet("Redemption Register")
        val hs    = headerStyle(wb)
        val ns    = numStyle(wb)
        REDEMPTION_COL_WIDTHS.forEachIndexed { i, w -> sheet.setColumnWidth(i, w * 256) }
        val hr = sheet.createRow(0)
        REDEMPTION_HEADERS.forEachIndexed { i, t ->
            hr.createCell(i).also { it.setCellValue(t); it.cellStyle = hs }
        }
        redemptions.forEachIndexed { idx, r ->
            val row = sheet.createRow(idx + 1)
            row.createCell(0).setCellValue(DateUtils.isoToDisplay(r.returnDate))
            row.createCell(1).setCellValue(r.ticketNo)
            row.createCell(2).setCellValue(
                if (!r.address.isNullOrBlank()) "${r.customerName}\n${r.address}"
                else r.customerName
            )
            row.createCell(3).setCellValue(DateUtils.isoToDisplay(r.pledgeDate))
            numCell(row, 4, r.principalRs, ns)
            numCell(row, 5, r.interestRs,  ns)
            numCell(row, 6, r.totalAmount,  ns)
            row.createCell(7).setCellValue(r.numberOfDays.toDouble())
        }
    }

    // ── Summary Sheet ─────────────────────────────────────────────────────────

    private fun writeSummarySheet(
        wb: XSSFWorkbook,
        pledges: List<Pledge>,
        redemptions: List<Redemption>
    ) {
        val sheet = wb.createSheet("Summary")
        val ts    = titleStyle(wb)
        val ls    = labelStyle(wb)
        val ns    = numStyle(wb)
        sheet.setColumnWidth(0, 36 * 256)
        sheet.setColumnWidth(1, 20 * 256)
        var r = 0

        fun title(text: String) {
            val row  = sheet.createRow(r++)
            val cell = row.createCell(0)
            cell.setCellValue(text); cell.cellStyle = ts
            sheet.addMergedRegion(CellRangeAddress(r-1, r-1, 0, 2))
        }
        fun dataRow(label: String, value: String) {
            val row = sheet.createRow(r++)
            row.createCell(0).also { it.setCellValue(label); it.cellStyle = ls }
            row.createCell(1).setCellValue(value)
        }
        fun numRow(label: String, value: Double) {
            val row = sheet.createRow(r++)
            row.createCell(0).also { it.setCellValue(label); it.cellStyle = ls }
            numCell(row, 1, value, ns)
        }

        title("Pledge Summary")
        dataRow("Total Pledges", pledges.size.toString())
        dataRow("Active",   pledges.count { it.status == "ACTIVE" }.toString())
        dataRow("Redeemed", pledges.count { it.status == "REDEEMED" }.toString())
        dataRow("Overdue",  pledges.count { it.status == "OVERDUE" }.toString())
        sheet.createRow(r++)
        title("Financial Summary")
        numRow("Total Principal Given (Rs)",  redemptions.sumOf { it.principalRs })
        numRow("Total Interest Earned (Rs)",  redemptions.sumOf { it.interestRs })
        numRow("Total Amount Recovered (Rs)", redemptions.sumOf { it.totalAmount })
    }

    // ── Styles ────────────────────────────────────────────────────────────────

    private fun headerStyle(wb: XSSFWorkbook): XSSFCellStyle =
        wb.createCellStyle().also {
            val f = wb.createFont(); f.bold = true; f.fontHeightInPoints = 11; it.setFont(f)
            it.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            it.fillPattern = FillPatternType.SOLID_FOREGROUND
            it.borderBottom = BorderStyle.THIN
        }

    private fun titleStyle(wb: XSSFWorkbook): XSSFCellStyle =
        wb.createCellStyle().also {
            val f = wb.createFont(); f.bold = true; f.fontHeightInPoints = 13; it.setFont(f)
        }

    private fun labelStyle(wb: XSSFWorkbook): XSSFCellStyle =
        wb.createCellStyle().also {
            val f = wb.createFont(); f.bold = true; it.setFont(f)
        }

    private fun numStyle(wb: XSSFWorkbook): XSSFCellStyle =
        wb.createCellStyle().also {
            it.dataFormat = wb.createDataFormat().getFormat("#,##0.00")
        }

    private fun numCell(
        row: org.apache.poi.ss.usermodel.Row,
        col: Int,
        value: Double,
        style: XSSFCellStyle
    ) { row.createCell(col).also { it.setCellValue(value); it.cellStyle = style } }
}