package com.pawnsafe.utils

import android.content.Context
import android.net.Uri
import com.pawnsafe.core.utils.ExcelExporter
import com.pawnsafe.core.utils.FileUtils
import com.pawnsafe.domain.model.Pledge
import com.pawnsafe.domain.model.Redemption
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ExcelExporterTest {

    private lateinit var context: Context
    private lateinit var uri: Uri
    private val outputStream = ByteArrayOutputStream()

    private val pledges = listOf(
        Pledge(
            id           = 1,
            ticketNo     = "001",
            date         = "2024-01-01",
            name         = "Ravi Kumar",
            loanAmountRs = "10000",
            status       = "ACTIVE",
            taluk        = "Tumkur"
        ),
        Pledge(
            id           = 2,
            ticketNo     = "002",
            date         = "2023-06-15",
            name         = "Sita Devi",
            loanAmountRs = "5000",
            status       = "REDEEMED"
        )
    )

    private val redemptions = listOf(
        Redemption(
            id           = 1,
            pledgeId     = 2,
            ticketNo     = "002",
            customerName = "Sita Devi",
            pledgeDate   = "2023-06-15",
            returnDate   = "2023-12-15",
            numberOfDays = 184,
            principalRs  = 5000.0,
            interestRs   = 357.0,
            totalAmount  = 5357.0
        )
    )

    @Before
    fun setUp() {
        context = mock()
        uri     = mock()
        val contentResolver = mock<android.content.ContentResolver>()
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(contentResolver.openOutputStream(any())).thenReturn(outputStream)
    }

    @Test
    fun `export creates workbook with 3 sheets`() {
        ExcelExporter.export(context, uri, pledges, redemptions)

        val workbook = XSSFWorkbook(outputStream.toByteArray().inputStream())
        assertEquals(3, workbook.numberOfSheets)
        workbook.close()
    }

    @Test
    fun `sheet 1 is named Pledge Book`() {
        ExcelExporter.export(context, uri, pledges, redemptions)

        val workbook = XSSFWorkbook(outputStream.toByteArray().inputStream())
        assertNotNull(workbook.getSheet("Pledge Book"))
        workbook.close()
    }

    @Test
    fun `sheet 2 is named Redemption Register`() {
        ExcelExporter.export(context, uri, pledges, redemptions)

        val workbook = XSSFWorkbook(outputStream.toByteArray().inputStream())
        assertNotNull(workbook.getSheet("Redemption Register"))
        workbook.close()
    }

    @Test
    fun `sheet 3 is named Summary`() {
        ExcelExporter.export(context, uri, pledges, redemptions)

        val workbook = XSSFWorkbook(outputStream.toByteArray().inputStream())
        assertNotNull(workbook.getSheet("Summary"))
        workbook.close()
    }

    @Test
    fun `pledge sheet has correct row count (header + data)`() {
        ExcelExporter.export(context, uri, pledges, redemptions)

        val workbook  = XSSFWorkbook(outputStream.toByteArray().inputStream())
        val sheet     = workbook.getSheet("Pledge Book")
        // Row 0 = header, rows 1..n = data
        assertEquals(pledges.size + 1, sheet.physicalNumberOfRows)
        workbook.close()
    }

    @Test
    fun `redemption sheet has correct row count`() {
        ExcelExporter.export(context, uri, pledges, redemptions)

        val workbook = XSSFWorkbook(outputStream.toByteArray().inputStream())
        val sheet    = workbook.getSheet("Redemption Register")
        assertEquals(redemptions.size + 1, sheet.physicalNumberOfRows)
        workbook.close()
    }

    @Test
    fun `pledge sheet first data row contains correct name`() {
        ExcelExporter.export(context, uri, pledges, redemptions)

        val workbook = XSSFWorkbook(outputStream.toByteArray().inputStream())
        val sheet    = workbook.getSheet("Pledge Book")
        val nameCell = sheet.getRow(1).getCell(2) // col 2 = Name
        assertEquals("Ravi Kumar", nameCell.stringCellValue)
        workbook.close()
    }

    @Test
    fun `redemption sheet first data row contains correct total`() {
        ExcelExporter.export(context, uri, pledges, redemptions)

        val workbook   = XSSFWorkbook(outputStream.toByteArray().inputStream())
        val sheet      = workbook.getSheet("Redemption Register")
        val totalCell  = sheet.getRow(1).getCell(6) // col 6 = Total (Rs)
        assertTrue(totalCell.numericCellValue > 0)
        workbook.close()
    }

    @Test
    fun `export with empty lists still creates 3 sheets`() {
        ExcelExporter.export(context, uri, emptyList(), emptyList())

        val workbook = XSSFWorkbook(outputStream.toByteArray().inputStream())
        assertEquals(3, workbook.numberOfSheets)
        workbook.close()
    }
}