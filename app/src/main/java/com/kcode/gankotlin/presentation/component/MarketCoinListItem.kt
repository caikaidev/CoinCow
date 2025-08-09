package com.kcode.gankotlin.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kcode.gankotlin.domain.model.CoinMarketData
import com.kcode.gankotlin.domain.model.getDisplayName
import com.kcode.gankotlin.domain.model.getFormattedMarketCap
import com.kcode.gankotlin.domain.model.shouldShowMarketCap
import com.kcode.gankotlin.ui.theme.InstagramColors
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketCoinListItem(
    coin: CoinMarketData,
    onClick: () -> Unit,
    onAddToWatchlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isAddingToWatchlist by remember { mutableStateOf(false) }
    
    // Cached formatted values to avoid recomputation
    val formattedPrice = remember(coin.currentPrice) {
        formatPrice(coin.currentPrice)
    }
    
    val displayName = remember(coin.name) {
        coin.getDisplayName(20)
    }
    
    val formattedMarketCap = remember(coin.marketCap) {
        coin.getFormattedMarketCap()
    }
    
    // Animation for price changes
    val priceChangeColor = if (coin.isPriceUp()) {
        InstagramColors.PriceUp
    } else {
        InstagramColors.PriceDown
    }
    
    val animatedColor by animateColorAsState(
        targetValue = priceChangeColor,
        animationSpec = tween(durationMillis = 300),
        label = "price_color_animation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Market Cap Rank - Using new centered ranking badge
            coin.marketCapRank?.let { rank ->
                CenteredRankingBadge(rank = rank)
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // Coin Info - Optimized layout with integrated icon
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Primary information row: Icon + Name + Price
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Smaller coin icon integrated with text
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(coin.image)
                            .crossfade(true)
                            .build(),
                        contentDescription = "${coin.name} logo",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text(
                        text = formattedPrice,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Secondary information row: Symbol + Price Change (aligned with text above)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add spacing to align with text above (icon width + spacer)
                    Spacer(modifier = Modifier.width(40.dp)) // 32dp icon + 8dp spacer
                    
                    Text(
                        text = coin.symbol.uppercase(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Price Change
                    coin.priceChangePercentage24h?.let { changePercentage ->
                        Text(
                            text = coin.getFormattedPriceChangePercentage(),
                            style = MaterialTheme.typography.bodySmall,
                            color = animatedColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Market Cap information (separate row to avoid crowding)
                formattedMarketCap?.let { marketCap ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Row {
                        Spacer(modifier = Modifier.width(40.dp)) // Align with text above
                        Text(
                            text = "Market Cap: $marketCap",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Add to Watchlist Button
            IconButton(
                onClick = {
                    isAddingToWatchlist = true
                    onAddToWatchlist()
                },
                enabled = !isAddingToWatchlist,
                modifier = Modifier.size(32.dp)
            ) {
                if (isAddingToWatchlist) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add to watchlist",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
    
    // Reset adding state after a delay
    LaunchedEffect(isAddingToWatchlist) {
        if (isAddingToWatchlist) {
            kotlinx.coroutines.delay(1000)
            isAddingToWatchlist = false
        }
    }
}

private fun formatPrice(price: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return when {
        price >= 1 -> formatter.format(price)
        price >= 0.01 -> String.format("$%.4f", price)
        price >= 0.0001 -> String.format("$%.6f", price)
        else -> String.format("$%.8f", price)
    }
}

private fun formatMarketCap(amount: Double): String {
    return when {
        amount >= 1_000_000_000_000 -> String.format("%.2fT", amount / 1_000_000_000_000)
        amount >= 1_000_000_000 -> String.format("%.2fB", amount / 1_000_000_000)
        amount >= 1_000_000 -> String.format("%.2fM", amount / 1_000_000)
        amount >= 1_000 -> String.format("%.2fK", amount / 1_000)
        else -> String.format("%.0f", amount)
    }
}