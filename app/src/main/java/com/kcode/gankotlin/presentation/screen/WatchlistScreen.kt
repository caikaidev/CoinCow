package com.kcode.gankotlin.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kcode.gankotlin.R
import com.kcode.gankotlin.domain.model.CoinMarketData
import com.kcode.gankotlin.presentation.component.*
import com.kcode.gankotlin.presentation.viewmodel.WatchlistViewModel
import com.kcode.gankotlin.ui.theme.InstagramColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    onCoinClick: (String) -> Unit,
    onAddCoinClick: () -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadWatchlist()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Watchlist",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshWatchlist() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                    IconButton(onClick = onAddCoinClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Coin"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = TopAppBarDefaults.windowInsets
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refreshWatchlist() },
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    uiState.isLoading && uiState.watchlistCoins.isEmpty() -> {
                        LoadingContent()
                    }
                    uiState.error != null && uiState.watchlistCoins.isEmpty() -> {
                        ErrorContent(
                            error = uiState.error ?: "Unknown error",
                            onRetry = { viewModel.loadWatchlist() }
                        )
                    }
                    uiState.watchlistCoins.isEmpty() -> {
                        EmptyWatchlistContent(onAddCoinClick = onAddCoinClick)
                    }
                    else -> {
                        WatchlistContent(
                            coins = uiState.watchlistCoins,
                            onCoinClick = onCoinClick,
                            onRemoveCoin = { coinId -> viewModel.removeCoinFromWatchlist(coinId) }
                        )
                    }
                }
                // Show error snackbar if there's an error but we have cached data
                uiState.error?.let { error ->
                    if (uiState.watchlistCoins.isNotEmpty()) {
                        LaunchedEffect(error) {
                            // Show snackbar for error while having cached data
                        }
                    }
                }
            }
        }
        
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            InstagramLoadingIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading your watchlist...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        InstagramStyleCard(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Oops! Something went wrong",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                GradientButton(
                    text = "Try Again",
                    onClick = onRetry
                )
            }
        }
    }
}

@Composable
private fun EmptyWatchlistContent(
    onAddCoinClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        InstagramStyleCard(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Watchlist is Empty",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add some cryptocurrencies to track their prices and performance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                GradientButton(
                    text = "Add Coins",
                    onClick = onAddCoinClick
                )
            }
        }
    }
}

@Composable
private fun WatchlistContent(
    coins: List<CoinMarketData>,
    onCoinClick: (String) -> Unit,
    onRemoveCoin: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = coins,
            key = { coin -> coin.id }
        ) { coin ->
            CoinListItem(
                coin = coin,
                onClick = { onCoinClick(coin.id) },
                onRemove = { onRemoveCoin(coin.id) }
            )
        }
    }
}