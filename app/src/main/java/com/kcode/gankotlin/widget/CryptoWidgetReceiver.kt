package com.kcode.gankotlin.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Widget receiver for handling crypto widget lifecycle
 */
@AndroidEntryPoint
class CryptoWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = CryptoWidget()

    @Inject
    lateinit var widgetUpdateScheduler: WidgetUpdateScheduler

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // Trigger a background refresh via WorkManager; Glance will render on next data change
        widgetUpdateScheduler.triggerImmediateUpdate()
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        widgetUpdateScheduler.scheduleWidgetUpdates()
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        widgetUpdateScheduler.cancelWidgetUpdates()
    }
}