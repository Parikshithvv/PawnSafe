package com.pawnsafe.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkerScheduler {

    /**
     * Schedules OverdueCheckWorker to run once per day.
     * Uses KEEP policy — if the worker is already scheduled, does nothing.
     * Call this from PawnSafeApp.onCreate().
     */
    fun scheduleDailyOverdueCheck(context: Context) {
        val request = PeriodicWorkRequestBuilder<OverdueCheckWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            OverdueCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}