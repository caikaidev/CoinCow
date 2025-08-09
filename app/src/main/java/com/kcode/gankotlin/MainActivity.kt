package com.kcode.gankotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.lifecycleScope
import com.kcode.gankotlin.data.performance.PerformanceMonitor
import com.kcode.gankotlin.data.recovery.CrashRecoveryManager
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.kcode.gankotlin.presentation.component.BottomNavigationBar
import com.kcode.gankotlin.presentation.component.GradientButton
import com.kcode.gankotlin.presentation.component.InstagramStyleCard
import com.kcode.gankotlin.presentation.component.OnboardingScreen
import com.kcode.gankotlin.presentation.navigation.CryptoNavigation
import com.kcode.gankotlin.presentation.navigation.Screen
import com.kcode.gankotlin.presentation.viewmodel.OnboardingViewModel
import com.kcode.gankotlin.presentation.viewmodel.SettingsViewModel
import com.kcode.gankotlin.ui.theme.CryptoTrackerTheme

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var performanceMonitor: PerformanceMonitor
    
    @Inject
    lateinit var crashRecoveryManager: CrashRecoveryManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize performance monitoring
        lifecycleScope.launch {
            performanceMonitor.recordMemoryUsage()
        }
        
        // Check for crash recovery
        lifecycleScope.launch {
            if (crashRecoveryManager.shouldStartInRecoveryMode()) {
                // Handle crash recovery if needed
                val recoveryState = crashRecoveryManager.restoreAppState()
                // Log recovery state for debugging
                println("App recovered from crash: ${recoveryState.lastCrashMessage}")
            }
            
            // Record successful launch
            crashRecoveryManager.recordSuccessfulLaunch()
        }
        
        enableEdgeToEdge()
        setContent {
            CryptoTrackerApp(intent = intent)
        }
    }
    
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            performanceMonitor.recordMemoryUsage()
        }
    }
    
    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            crashRecoveryManager.saveAppState("main")
        }
    }
}

@Composable
fun CryptoTrackerApp(
    intent: android.content.Intent? = null,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onboardingViewModel: OnboardingViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val currentTheme by settingsViewModel.currentTheme.collectAsStateWithLifecycle()
    val showWelcome by onboardingViewModel.isFirstLaunch.collectAsStateWithLifecycle()
    
    // Determine dark theme based on preference
    val darkTheme = when (currentTheme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme() // "system" default
    }
    
    CryptoTrackerTheme(darkTheme = darkTheme) {
    

    
    if (showWelcome) {
        // Onboarding Screen
        OnboardingScreen(
            onComplete = { onboardingViewModel.completeOnboarding() },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // Main App with Navigation
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomNavigationBar(navController = navController)
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                CryptoNavigation(
                    navController = navController,
                    startDestination = Screen.Watchlist.route
                )
            }
        }
    }
    }
}