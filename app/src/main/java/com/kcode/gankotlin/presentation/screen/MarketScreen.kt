package com.kcode.gankotlin.presentation.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kcode.gankotlin.domain.model.CoinMarketData
import com.kcode.gankotlin.presentation.component.*
import com.kcode.gankotlin.presentation.viewmodel.MarketViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MarketScreen(
    onCoinClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: MarketViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current
    
    LaunchedEffect(Unit) {
        viewModel.loadMarketData()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Market",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.combinedClickable(
                            onClick = { },
                            onLongClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.testApiConnectivity()
                            }
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
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
            onRefresh = { viewModel.refreshMarketData() },
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    uiState.isLoading && uiState.marketData.isEmpty() -> {
                        SkeletonLoadingList()
                    }
                    uiState.error != null && uiState.marketData.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            NetworkErrorPlaceholder(
                                onRetry = { viewModel.loadMarketData() },
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    else -> {
                        MarketContent(
                            coins = uiState.marketData,
                            onCoinClick = onCoinClick,
                            onAddToWatchlist = { coinId -> viewModel.addToWatchlist(coinId) }
                        )
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
                text = "Loading market data...",
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
                    text = "Unable to load market data",
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
                    text = "Retry",
                    onClick = onRetry
                )
            }
        }
    }
}

@Composable
private fun MarketContent(
    coins: List<CoinMarketData>,
    onCoinClick: (String) -> Unit,
    onAddToWatchlist: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Top Cryptocurrencies",
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(
            items = coins,
            key = { coin -> coin.id }
        ) { coin ->
            SafeMarketCoinListItem(
                coin = coin,
                onClick = { onCoinClick(coin.id) },
                onAddToWatchlist = { onAddToWatchlist(coin.id) }
            )
        }
    }
}