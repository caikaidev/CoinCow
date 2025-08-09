package com.kcode.gankotlin.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kcode.gankotlin.domain.model.CoinMarketData
import com.kcode.gankotlin.ui.theme.InstagramColors
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinListItem(
    coin: CoinMarketData,
    onClick: () -> Unit,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
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
    
    // Icon size aligned with text height
    val iconSize = 24.dp
    val secondaryStartPadding = iconSize + 8.dp
    
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
            // Coin Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Smaller coin icon integrated with the title text
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(coin.image)
                            .crossfade(true)
                            .build(),
                        contentDescription = "${coin.name} logo",
                        modifier = Modifier
                            .size(iconSize)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = coin.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    coin.marketCapRank?.let { rank ->
                        Spacer(modifier = Modifier.width(8.dp))
                        CompactRankingBadge(rank = rank)
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Align start with the title after the icon
                    Spacer(modifier = Modifier.width(secondaryStartPadding))
                    Text(
                        text = coin.symbol.uppercase(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Market Cap (if available)
                coin.marketCap?.let { marketCap ->
                    Row {
                        Spacer(modifier = Modifier.width(secondaryStartPadding))
                        Text(
                            text = "Market Cap: ${formatCurrency(marketCap.toDouble())}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Price Info
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatPrice(coin.currentPrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
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
            
            // Remove button (if provided)
            onRemove?.let {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove from watchlist",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text("Remove from Watchlist")
            },
            text = {
                Text("Are you sure you want to remove ${coin.name} from your watchlist?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemove?.invoke()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
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

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return when {
        amount >= 1_000_000_000 -> String.format("$%.2fB", amount / 1_000_000_000)
        amount >= 1_000_000 -> String.format("$%.2fM", amount / 1_000_000)
        amount >= 1_000 -> String.format("$%.2fK", amount / 1_000)
        else -> formatter.format(amount)
    }
}