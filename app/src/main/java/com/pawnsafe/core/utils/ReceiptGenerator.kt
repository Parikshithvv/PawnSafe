package com.pawnsafe.core.utils

import android.content.Context
import android.graphics.*
import java.io.File
import java.io.FileOutputStream

object ReceiptGenerator {

    fun generateReceiptBitmap(
        shopName    : String,
        ticketNo    : String,
        name        : String,
        phone       : String?,
        date        : String,
        article     : String?,
        purity      : String?,
        grossWeight : String?,
        nettWeight  : String?,
        principal   : Double,
        days        : Int,
        rate        : Double,
        interest    : Double,
        total       : Double,
        status      : String
    ): Bitmap {
        val width   = 800
        val padding = 48f
        val lineH   = 48f
        val gap     = 20f

        val bgPaint = Paint().apply { color = Color.WHITE }
        val accentPaint = Paint().apply { color = Color.parseColor("#1A1A2E") }

        val shopPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color     = Color.parseColor("#1A1A2E")
            textSize  = 48f
            typeface  = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color     = Color.parseColor("#4A4A6A")
            textSize  = 26f
            typeface  = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color    = Color.parseColor("#666666")
            textSize = 28f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color    = Color.parseColor("#1A1A1A")
            textSize = 28f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val totalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color    = Color.parseColor("#1A1A2E")
            textSize = 36f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val totalValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color    = Color.parseColor("#C0392B")
            textSize = 36f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val dividerPaint = Paint().apply {
            color       = Color.parseColor("#CCCCCC")
            strokeWidth = 2f
        }
        val dashedPaint = Paint().apply {
            color       = Color.parseColor("#AAAAAA")
            strokeWidth = 2f
            pathEffect  = DashPathEffect(floatArrayOf(12f, 8f), 0f)
        }
        val statusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = when (status) {
                "ACTIVE"   -> Color.parseColor("#1565C0")
                "OVERDUE"  -> Color.parseColor("#C62828")
                "REDEEMED" -> Color.parseColor("#2E7D32")
                else       -> Color.GRAY
            }
            textSize  = 28f
            typeface  = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color     = Color.parseColor("#999999")
            textSize  = 22f
            textAlign = Paint.Align.CENTER
        }

        // -- count actual rows to get exact height --
        var rowCount = 4 // ticket, name, date, always present
        if (!phone.isNullOrBlank())       rowCount++
        if (!article.isNullOrBlank())     rowCount++
        if (!purity.isNullOrBlank())      rowCount++
        if (!grossWeight.isNullOrBlank()) rowCount++
        if (!nettWeight.isNullOrBlank())  rowCount++
        rowCount += 4 // principal, days, rate, interest

        val height = (
            10f +           // top accent
            padding +       // top padding
            60f +           // shop name
            36f +           // subtitle
            gap * 3 +       // gaps
            lineH * rowCount +
            gap * 4 +       // section gaps
            lineH * 2 +     // total + status
            lineH +         // footer
            padding +       // bottom padding
            20f             // bottom accent
        ).toInt()

        val bmp    = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        canvas.drawRect(0f, 0f, width.toFloat(), 10f, accentPaint)

        var y = 10f + padding

        y += 52f
        canvas.drawText(shopName, width / 2f, y, shopPaint)
        y += 36f
        canvas.drawText("Pledge Receipt", width / 2f, y, subPaint)
        y += gap + 16f

        canvas.drawLine(padding, y, width - padding, y, dashedPaint)
        y += gap + 4f

        fun row(label: String, value: String) {
            canvas.drawText(label, padding, y, labelPaint)
            canvas.drawText(value, width - padding, y, valuePaint)
            y += lineH
        }

        row("Ticket No", "#$ticketNo")
        row("Name",      name)
        if (!phone.isNullOrBlank())           row("Phone",    phone)
        row("Date",      date)
        if (!article.isNullOrBlank())         row("Article",  article)
        if (!purity.isNullOrBlank())          row("Purity",   purity)
        if (!grossWeight.isNullOrBlank())     row("Gross Wt", "${grossWeight}g")
        if (!nettWeight.isNullOrBlank())      row("Nett Wt",  "${nettWeight}g")

        y += 4f
        canvas.drawLine(padding, y, width - padding, y, dashedPaint)
        y += gap

        row("Principal", "Rs. ${"%.2f".format(principal)}")
        row("Days",      "$days days")
        row("Rate",      "$rate% / month")
        row("Interest",  "Rs. ${"%.2f".format(interest)}")

        y += 4f
        canvas.drawLine(padding, y, width - padding, y, dividerPaint)
        y += 3f
        canvas.drawLine(padding, y, width - padding, y, dividerPaint)
        y += gap + 4f

        canvas.drawText("TOTAL DUE",                      padding,         y, totalLabelPaint)
        canvas.drawText("Rs. ${"%.2f".format(total)}",   width - padding, y, totalValuePaint)
        y += lineH + gap

        canvas.drawLine(padding, y, width - padding, y, dashedPaint)
        y += gap

        canvas.drawText("Status: $status", width / 2f, y, statusPaint)
        y += lineH + gap

        canvas.drawText("Thank you for choosing us!", width / 2f, y, footerPaint)
        y += 32f

        canvas.drawRect(0f, y, width.toFloat(), y + 10f, accentPaint)

        return bmp
    }

    fun saveBitmapToCache(context: Context, bitmap: Bitmap): File {
        val dir  = File(context.cacheDir, "receipts").also { it.mkdirs() }
        val file = File(dir, "receipt_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file
    }
}