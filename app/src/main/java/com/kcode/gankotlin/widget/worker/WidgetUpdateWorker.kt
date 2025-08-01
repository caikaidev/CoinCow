package com.kcode.gankotlin.widget.worker

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kcode.gankotlin.widget.CryptoWidget
import com.kcode.gankotlin.widget.data.WidgetDataProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker to update widget data periodically
 */
@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val widgetDataProvider: WidgetDataProvider
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // Check if data needs refresh
            if (widgetDataProvider.shouldRefreshData()) {
                // Get fresh widget data
                val widgetData = widgetDataProvider.getWidgetData()
                
                // Update all widget instances
                val glanceAppWidgetManager = GlanceAppWidgetManager(applicationContext)
                val glanceIds = glanceAppWidgetManager.getGlanceIds(CryptoWidget::class.java)
                
                glanceIds.forEach { glanceId ->
                    CryptoWidget().update(applicationContext, glanceId)
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            // Retry on failure
            Result.retry()
        }
    }
    
    companion object {
        const val WORK_NAME = "widget_update_work"
    }
}