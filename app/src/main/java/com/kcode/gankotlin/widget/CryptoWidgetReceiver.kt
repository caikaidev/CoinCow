package com.kcode.gankotlin.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Widget receiver for handling crypto widget updates
 */
@AndroidEntryPoint
class CryptoWidgetReceiver : GlanceAppWidgetReceiver() {
    
    override val glanceAppWidget: GlanceAppWidget = CryptoWidget()
    
    private val coroutineScope = MainScope()
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        
        // Trigger widget update
        coroutineScope.launch {
            appWidgetIds.forEach { appWidgetId ->
                glanceAppWidget.update(context, appWidgetId)
            }
        }
    }
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Start periodic updates when first widget is added
        WidgetUpdateScheduler.schedulePeriodicUpdates(context)
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Cancel periodic updates when last widget is removed
        WidgetUpdateScheduler.cancelPeriodicUpdates(context)
    }
}