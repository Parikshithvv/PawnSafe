package com.pawnsafe.core.utils

import android.content.Context
import android.net.Uri
import java.io.OutputStream

/**
 * SAF (Storage Access Framework) helpers for .xlsx export.
 *
 * The URI is obtained by the caller via ActivityResultContracts.CreateDocument
 * (see ExportViewModel / ExportScreen), then passed here for writing.
 */
object FileUtils {

    /**
     * Open a writable OutputStream for the given SAF URI.
     * Caller is responsible for closing — use .use {} on the returned stream.
     *
     * @throws IllegalStateException if the content resolver cannot open the URI.
     */
    fun openOutputStream(context: Context, uri: Uri): OutputStream {
        return context.contentResolver.openOutputStream(uri)
            ?: error("Cannot open OutputStream for URI: $uri")
    }

    /**
     * Suggested filename for the exported workbook.
     * Format: PawnSafe_Export_YYYY-MM-DD.xlsx
     */
    fun suggestedFileName(): String {
        val today = DateUtils.todayIso()          // yyyy-MM-dd
        return "PawnSafe_Export_$today.xlsx"
    }

    /**
     * MIME type required by ACTION_CREATE_DOCUMENT for .xlsx files.
     */
    const val XLSX_MIME_TYPE =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
}
