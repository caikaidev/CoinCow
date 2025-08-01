package com.kcode.gankotlin.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kcode.gankotlin.presentation.component.InstagramStyleCard
import com.kcode.gankotlin.presentation.viewmodel.WidgetConfigurationViewModel
import com.kcode.gankotlin.ui.theme.CryptoTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Configuration activity for the crypto widget
 * Allows users to select which coins to display in the widget
 */
@AndroidEntryPoint
class CryptoWidgetConfigurationActivity : ComponentActivity() {
    
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set the result to CANCELED initially
        setResult(Activity.RESULT_CANCELED)
        
        // Get the widget ID from the intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        
        // If the widget ID is invalid, finish the activity
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        setContent {
            CryptoTrackerTheme {
                WidgetConfigurationScreen(
                    appWidgetId = appWidgetId,
                    onConfigurationComplete = { selectedCoins ->
                        // Save configuration and finish
                        val resultValue = Intent().apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        }
                        setResult(Activity.RESULT_OK, resultValue)
                        finish()
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetConfigurationScreen(
    appWidgetId: Int,
    onConfigurationComplete: (List<String>) -> Unit,
    onCancel: () -> Unit,
    viewModel: WidgetConfigurationViewModel = hiltViewModel()
) {
    val availableCoins by viewModel.availableCoins.collectAsStateWithLifecycle()
    val selectedCoins by viewModel.selectedCoins.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadAvailableCoins()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configure Widget",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            WidgetConfigurationBottomBar(
                selectedCount = selectedCoins.size,
                onSave = {
                    onConfigurationComplete(selectedCoins)
                },
                onCancel = onCancel,
                enabled = selectedCoins.isNotEmpty()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Instructions
            InstagramStyleCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Select Coins for Widget",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Choose up to 5 cryptocurrencies to display in your home screen widget.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Coin selection list
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableCoins) { coin ->
                        CoinSelectionItem(
                            coin = coin,
                            isSelected = selectedCoins.contains(coin.id),
                            onSelectionChange = { isSelected ->
                                if (isSelected && selectedCoins.size < 5) {
                                    viewModel.selectCoin(coin.id)
                                } else if (!isSelected) {
                                    viewModel.deselectCoin(coin.id)
                                }
                            },
                            enabled = selectedCoins.contains(coin.id) || selectedCoins.size < 5
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CoinSelectionItem(
    coin: com.kcode.gankotlin.domain.model.CoinMarketData,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = { onSelectionChange(!isSelected) },
                enabled = enabled
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChange,
                enabled = enabled
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Coin info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = coin.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = coin.symbol.uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Current price
            Text(
                text = "$${String.format("%.2f", coin.currentPrice)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun WidgetConfigurationBottomBar(
    selectedCount: Int,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = onSave,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            ) {
                Text("Save ($selectedCount/5)")
            }
        }
    }
}