package com.kcode.gankotlin.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

import com.kcode.gankotlin.widget.data.WidgetCoinData
import com.kcode.gankotlin.widget.data.WidgetDataProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Crypto price widget using Glance framework
 */
class CryptoWidget : GlanceAppWidget() {
    
    @Inject
    lateinit var widgetDataProvider: WidgetDataProvider
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Get widget data
        val widgetData = try {
            widgetDataProvider.getWidgetData()
        } catch (e: Exception) {
            // Fallback to sample data
            getSampleData()
        }
        
        provideContent {
            GlanceTheme {
                CryptoWidgetContent(widgetData)
            }
        }
    }
    
    private fun getSampleData(): List<WidgetCoinData> {
        return listOf(
            WidgetCoinData(
                id = "bitcoin",
                symbol = "BTC",
                name = "Bitcoin",
                currentPrice = 45000.0,
                priceChangePercentage24h = 2.5,
                imageUrl = ""
            ),
            WidgetCoinData(
                id = "ethereum",
                symbol = "ETH", 
                name = "Ethereum",
                currentPrice = 3200.0,
                priceChangePercentage24h = -1.2,
                imageUrl = ""
            ),
            WidgetCoinData(
                id = "solana",
                symbol = "SOL",
                name = "Solana",
                currentPrice = 98.5,
                priceChangePercentage24h = 5.8,
                imageUrl = ""
            )
        )
    }
}

@Composable
private fun CryptoWidgetContent(coins: List<WidgetCoinData>) {
    
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color.White))
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Widget header
        WidgetHeader()
        
        Spacer(modifier = GlanceModifier.height(12.dp))
        
        // Coin list
        if (coins.isNotEmpty()) {
            coins.forEach { coin ->
                CoinWidgetItem(coin = coin)
                Spacer(modifier = GlanceModifier.height(8.dp))
            }
        } else {
            // Show loading or error state
            Text(
                text = "Loading...",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(Color.Gray)
                )
            )
        }
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Last updated text
        Text(
            text = "Updated just now",
            style = TextStyle(
                fontSize = 10.sp,
                color = ColorProvider(Color.Gray)
            )
        )
    }
}

@Composable
private fun WidgetHeader() {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🚀",
            style = TextStyle(fontSize = 16.sp)
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = "Crypto Tracker",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(Color.Black)
            )
        )
    }
}

@Composable
private fun CoinWidgetItem(coin: WidgetCoinData) {
    val priceChangeColor = if (coin.priceChangePercentage24h >= 0) {
        Color(0xFF00C851) // Green
    } else {
        Color(0xFFFF4444) // Red
    }
    
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(ColorProvider(Color(0xFFF8F9FA)))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Coin symbol
        Text(
            text = coin.symbol,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(Color.Black)
            ),
            modifier = GlanceModifier.width(40.dp)
        )
        
        Spacer(modifier = GlanceModifier.width(8.dp))
        
        // Price
        Column(
            modifier = GlanceModifier.defaultWeight(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "$${String.format("%.2f", coin.currentPrice)}",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(Color.Black)
                )
            )
        }
        
        // Price change
        Text(
            text = "${if (coin.priceChangePercentage24h >= 0) "+" else ""}${String.format("%.1f", coin.priceChangePercentage24h)}%",
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = ColorProvider(priceChangeColor)
            )
        )
    }
}

// (Receiver moved to dedicated file `CryptoWidgetReceiver.kt`)