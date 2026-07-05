package com.pawnsafe.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pawnsafe.core.utils.DateUtils
import com.pawnsafe.data.local.dao.PledgeDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class OverdueCheckWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val pledgeDao: PledgeDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME        = "OverdueCheckWorker"
        const val CHANNEL_ID       = "pawnsafe_overdue"
        const val CHANNEL_NAME     = "Overdue Pledges"
        const val NOTIFICATION_ID  = 1001
    }

    override suspend fun doWork(): Result {
        return try {
            // Get all pledges that are still ACTIVE
            val activePledges = pledgeDao.getAllPledgesOnce()
                .filter { it.status == "ACTIVE" }

            // Find which ones are now overdue (> 365 days)
            val overduePledges = activePledges.filter { DateUtils.isOverdue(it.date) }

            if (overduePledges.isNotEmpty()) {
                // Mark each one as OVERDUE in DB
                overduePledges.forEach { pledge ->
                    pledgeDao.updateStatus(pledge.id, "OVERDUE")
                }

                // Fire a single notification summarising the count
                sendNotification(overduePledges.size)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification(count: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel (required on API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts for pledges overdue beyond 365 days"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Overdue Pledges — PawnSafe")
            .setContentText(
                if (count == 1) "1 pledge is overdue (> 365 days). Please review."
                else "$count pledges are overdue (> 365 days). Please review."
            )
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "$count pledge(s) have crossed 365 days without redemption and have been marked OVERDUE."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}