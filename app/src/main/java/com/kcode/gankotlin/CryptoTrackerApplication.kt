package com.kcode.gankotlin

import android.app.Application
import android.content.ComponentCallbacks2
import com.kcode.gankotlin.startup.AppStartupOptimizer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CryptoTrackerApplication : Application(), ComponentCallbacks2 {
    
    @Inject
    lateinit var appStartupOptimizer: AppStartupOptimizer
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize app with optimized startup
        appStartupOptimizer.initializeApp()
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        
        // Optimize for low memory conditions
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                appStartupOptimizer.optimizeForLowMemory()
            }
        }
    }
}