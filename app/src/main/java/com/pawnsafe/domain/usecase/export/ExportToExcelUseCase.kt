package com.pawnsafe.domain.usecase.export

import android.content.Context
import android.net.Uri
import com.pawnsafe.core.utils.ExcelExporter
import com.pawnsafe.data.local.dao.PledgeDao
import com.pawnsafe.data.local.dao.RedemptionDao
import com.pawnsafe.data.mapper.toDomain
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class ExportType { PLEDGE, REDEMPTION, BOTH }

sealed class ExportResult {
    data class Success(val uri: Uri, val fileName: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

class ExportToExcelUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pledgeDao: PledgeDao,
    private val redemptionDao: RedemptionDao
) {
    suspend operator fun invoke(
        exportType: ExportType,
        destinationUri: Uri
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val pledges = when (exportType) {
                ExportType.PLEDGE, ExportType.BOTH ->
                    pledgeDao.getAllPledgesOnce().map { it.toDomain() }
                ExportType.REDEMPTION -> emptyList()
            }

            val redemptions = when (exportType) {
                ExportType.REDEMPTION, ExportType.BOTH ->
                    redemptionDao.getAllRedemptionsOnce().map { it.toDomain() }
                ExportType.PLEDGE -> emptyList()
            }

            ExcelExporter.export(
                context      = context,
                uri          = destinationUri,
                pledges      = pledges,
                redemptions  = redemptions
            )

            ExportResult.Success(destinationUri, "PawnSafe_Export.xlsx")
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "Export failed")
        }
    }
}