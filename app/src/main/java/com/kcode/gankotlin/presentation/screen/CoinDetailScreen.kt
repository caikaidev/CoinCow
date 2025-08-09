package com.kcode.gankotlin.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kcode.gankotlin.presentation.component.CoinPriceChart
import com.kcode.gankotlin.presentation.component.ChartTimePeriod
import com.kcode.gankotlin.presentation.component.InstagramStyleCard
import com.kcode.gankotlin.presentation.component.NetworkErrorPlaceholder
import com.kcode.gankotlin.presentation.component.SkeletonMarketCoinListItem
import com.kcode.gankotlin.presentation.util.ErrorHandler
import com.kcode.gankotlin.presentation.viewmodel.CoinDetailViewModel
import com.kcode.gankotlin.presentation.viewmodel.state.CoinDetailUiState
import com.kcode.gankotlin.ui.theme.InstagramColors

/**
 * Coin detail screen showing comprehensive information about a cryptocurrency
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailScreen(
    coinId: String,
    onBackClick: () -> Unit,
    viewModel: CoinDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isInWatchlist by viewModel.isInWatchlist.collectAsStateWithLifecycle()
    val priceHistoryState by viewModel.priceHistoryState.collectAsStateWithLifecycle()
    val selectedChartPeriod by viewModel.selectedChartPeriod.collectAsStateWithLifecycle()
    
    // Load coin details when screen is first displayed
    LaunchedEffect(coinId) {
        viewModel.loadCoinDetails(coinId)
        viewModel.loadPriceHistory(coinId, ChartTimePeriod.SEVEN_DAYS)
    }
    
    Scaffold(
        topBar = {
            CoinDetailTopBar(
                coinName = when (val state = uiState) {
                    is CoinDetailUiState.Success -> state.coinDetails.name
                    else -> ""
                },
                isInWatchlist = isInWatchlist,
                onBackClick = onBackClick,
                onWatchlistToggle = {
                    if (isInWatchlist) {
                        viewModel.removeFromWatchlist(coinId)
                    } else {
                        viewModel.addToWatchlist(coinId)
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is CoinDetailUiState.Loading -> {
                CoinDetailLoadingContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            
            is CoinDetailUiState.Success -> {
                CoinDetailContent(
                    coinDetails = state.coinDetails,
                    priceHistoryState = priceHistoryState,
                    selectedChartPeriod = selectedChartPeriod,
                    onChartPeriodChange = { period ->
                        viewModel.loadPriceHistory(coinId, period)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            
            is CoinDetailUiState.Error -> {
                NetworkErrorPlaceholder(
                    onRetry = { viewModel.loadCoinDetails(coinId, forceRefresh = true) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoinDetailTopBar(
    coinName: String,
    isInWatchlist: Boolean,
    onBackClick: () -> Unit,
    onWatchlistToggle: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = coinName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = onWatchlistToggle) {
                Icon(
                    imageVector = if (isInWatchlist) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = if (isInWatchlist) "Remove from watchlist" else "Add to watchlist",
                    tint = if (isInWatchlist) InstagramColors.PriceUp else MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        windowInsets = TopAppBarDefaults.windowInsets
    )
}

@Composable
private fun CoinDetailLoadingContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Use existing skeleton components
        repeat(5) {
            SkeletonMarketCoinListItem()
        }
    }
}

@Composable
private fun CoinDetailContent(
    coinDetails: com.kcode.gankotlin.domain.model.CoinDetails,
    priceHistoryState: com.kcode.gankotlin.presentation.viewmodel.state.PriceHistoryUiState,
    selectedChartPeriod: ChartTimePeriod,
    onChartPeriodChange: (ChartTimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Coin header with image and basic info
        CoinDetailHeader(coinDetails = coinDetails)
        
        // Current price and change info
        CoinPriceInfo(coinDetails = coinDetails)
        
        // Price chart
        CoinPriceChart(
            priceHistory = when (priceHistoryState) {
                is com.kcode.gankotlin.presentation.viewmodel.state.PriceHistoryUiState.Success -> priceHistoryState.priceHistory
                else -> null
            },
            selectedPeriod = selectedChartPeriod,
            onPeriodChange = onChartPeriodChange,
            isLoading = priceHistoryState is com.kcode.gankotlin.presentation.viewmodel.state.PriceHistoryUiState.Loading
        )
        
        // Market data details
        CoinMarketDataSection(coinDetails = coinDetails)
        
        // Description section
        if (coinDetails.description.isNotEmpty()) {
            CoinDescriptionSection(description = coinDetails.description)
        }
    }
}

@Composable
private fun CoinDetailHeader(
    coinDetails: com.kcode.gankotlin.domain.model.CoinDetails
) {
    InstagramStyleCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Coin image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(coinDetails.image.large)
                    .crossfade(true)
                    .build(),
                contentDescription = "${coinDetails.name} logo",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(32.dp)),
                contentScale = ContentScale.Crop
            )
            
            // Coin info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = coinDetails.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = coinDetails.symbol.uppercase(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CoinPriceInfo(
    coinDetails: com.kcode.gankotlin.domain.model.CoinDetails
) {
    val currentPrice = coinDetails.marketData.currentPrice["usd"] ?: 0.0
    val priceChange24h = coinDetails.marketData.priceChangePercentage24h ?: 0.0
    val isPositive = priceChange24h >= 0
    
    InstagramStyleCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Current price
            Text(
                text = "$${String.format("%.2f", currentPrice)}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Price change
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${if (isPositive) "+" else ""}${String.format("%.2f", priceChange24h)}%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (isPositive) InstagramColors.PriceUp else InstagramColors.PriceDown
                )
                
                Text(
                    text = "24h",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // High/Low 24h
            val high24h = coinDetails.marketData.high24h["usd"] ?: 0.0
            val low24h = coinDetails.marketData.low24h["usd"] ?: 0.0
            
            if (high24h > 0 && low24h > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "24h High",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${String.format("%.2f", high24h)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Column {
                        Text(
                            text = "24h Low",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${String.format("%.2f", low24h)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}



@Composable
private fun CoinMarketDataSection(
    coinDetails: com.kcode.gankotlin.domain.model.CoinDetails
) {
    val marketCap = coinDetails.marketData.marketCap["usd"] ?: 0.0
    val totalVolume = coinDetails.marketData.totalVolume["usd"] ?: 0.0
    val circulatingSupply = coinDetails.marketData.circulatingSupply
    val maxSupply = coinDetails.marketData.maxSupply
    
    InstagramStyleCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Market Data",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Market cap
            MarketDataItem(
                label = "Market Cap",
                value = if (marketCap > 0) "$${formatLargeNumber(marketCap)}" else "N/A"
            )
            
            // Volume
            MarketDataItem(
                label = "24h Volume",
                value = if (totalVolume > 0) "$${formatLargeNumber(totalVolume)}" else "N/A"
            )
            
            // Circulating supply
            circulatingSupply?.let { supply ->
                MarketDataItem(
                    label = "Circulating Supply",
                    value = "${formatLargeNumber(supply)} ${coinDetails.symbol.uppercase()}"
                )
            }
            
            // Max supply
            maxSupply?.let { supply ->
                MarketDataItem(
                    label = "Max Supply",
                    value = "${formatLargeNumber(supply)} ${coinDetails.symbol.uppercase()}"
                )
            }
        }
    }
}

@Composable
private fun MarketDataItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CoinDescriptionSection(
    description: String
) {
    InstagramStyleCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = description.take(500) + if (description.length > 500) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

private fun formatLargeNumber(number: Double): String {
    return when {
        number >= 1_000_000_000_000 -> String.format("%.2fT", number / 1_000_000_000_000.0)
        number >= 1_000_000_000 -> String.format("%.2fB", number / 1_000_000_000.0)
        number >= 1_000_000 -> String.format("%.2fM", number / 1_000_000.0)
        number >= 1_000 -> String.format("%.2fK", number / 1_000.0)
        else -> String.format("%.0f", number)
    }
}