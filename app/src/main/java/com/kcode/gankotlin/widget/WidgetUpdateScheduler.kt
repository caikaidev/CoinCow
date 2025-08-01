package com.kcode.gankotlin.widget

import android.content.Context
import androidx.work.*
import com.kcode.gankotlin.widget.worker.WidgetUpdateWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scheduler for widget updates using WorkManager
 */
@Singleton
class WidgetUpdateScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Schedule periodic widget updates
     */
    fun scheduleWidgetUpdates() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val updateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            repeatInterval = 15, // 15 minutes minimum for periodic work
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            WidgetUpdateWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            updateRequest
        )
    }
    
    /**
     * Cancel widget updates
     */
    fun cancelWidgetUpdates() {
        workManager.cancelUniqueWork(WidgetUpdateWorker.WORK_NAME)
    }
    
    /**
     * Trigger immediate widget update
     */
    fun triggerImmediateUpdate() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val immediateUpdateRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .setConstraints(constraints)
            .build()
        
        workManager.enqueue(immediateUpdateRequest)
    }
}