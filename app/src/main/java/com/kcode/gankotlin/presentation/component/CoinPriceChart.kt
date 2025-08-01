package com.kcode.gankotlin.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import com.kcode.gankotlin.domain.model.CoinPriceHistory
import com.kcode.gankotlin.ui.theme.InstagramColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * Time period options for the price chart
 */
enum class ChartTimePeriod(val displayName: String, val apiValue: String) {
    ONE_HOUR("1H", "1"),
    TWENTY_FOUR_HOURS("24H", "1"),
    SEVEN_DAYS("7D", "7"),
    THIRTY_DAYS("30D", "30")
}

/**
 * Coin price chart component using Vico library
 */
@Composable
fun CoinPriceChart(
    priceHistory: CoinPriceHistory?,
    selectedPeriod: ChartTimePeriod,
    onPeriodChange: (ChartTimePeriod) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    InstagramStyleCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Chart header with period selector
            ChartHeader(
                selectedPeriod = selectedPeriod,
                onPeriodChange = onPeriodChange,
                priceHistory = priceHistory
            )
            
            // Chart content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                when {
                    isLoading -> {
                        ChartLoadingState()
                    }
                    priceHistory != null && priceHistory.getValidPrices().isNotEmpty() -> {
                        PriceLineChart(
                            priceHistory = priceHistory,
                            selectedPeriod = selectedPeriod
                        )
                    }
                    else -> {
                        ChartErrorState()
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartHeader(
    selectedPeriod: ChartTimePeriod,
    onPeriodChange: (ChartTimePeriod) -> Unit,
    priceHistory: CoinPriceHistory?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Price Chart",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Price change indicator
            priceHistory?.let { history ->
                val changePercentage = history.getPriceChangePercentage()
                changePercentage?.let { change ->
                    val isPositive = change >= 0
                    Text(
                        text = "${if (isPositive) "+" else ""}${String.format("%.2f", change)}%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isPositive) InstagramColors.PriceUp else InstagramColors.PriceDown
                    )
                }
            }
        }
        
        // Period selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChartTimePeriod.values().forEach { period ->
                PeriodChip(
                    period = period,
                    isSelected = period == selectedPeriod,
                    onClick = { onPeriodChange(period) }
                )
            }
        }
    }
}

@Composable
private fun PeriodChip(
    period: ChartTimePeriod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = {
            Text(
                text = period.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            )
        },
        selected = isSelected,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
private fun PriceLineChart(
    priceHistory: CoinPriceHistory,
    selectedPeriod: ChartTimePeriod
) {
    val validPrices = priceHistory.getValidPrices()
    
    // Convert price data to chart entries
    val chartEntries = remember(validPrices) {
        validPrices.mapIndexed { index, pricePoint ->
            FloatEntry(index.toFloat(), pricePoint.price.toFloat())
        }
    }
    
    val chartEntryModelProducer = remember { ChartEntryModelProducer() }
    
    LaunchedEffect(chartEntries) {
        chartEntryModelProducer.setEntries(chartEntries)
    }
    
    if (chartEntries.isNotEmpty()) {
        ProvideChartStyle(
            chartStyle = rememberChartStyle()
        ) {
            Chart(
                chart = lineChart(
                    lines = listOf(
                        LineChart.LineSpec(
                            lineColor = InstagramColors.PriceUp.toArgb(),
                            lineBackgroundShader = DynamicShaders.fromBrush(
                                Brush.verticalGradient(
                                    listOf(
                                        InstagramColors.PriceUp.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                        )
                    )
                ),
                chartModelProducer = chartEntryModelProducer,
                startAxis = rememberStartAxis(
                    itemPlacer = AxisItemPlacer.Vertical.default(maxItemCount = 5)
                ),
                bottomAxis = rememberBottomAxis(
                    itemPlacer = AxisItemPlacer.Horizontal.default(spacing = 1)
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun rememberChartStyle() = com.patrykandpatrick.vico.compose.style.ChartStyle(
    axis = com.patrykandpatrick.vico.compose.style.ChartStyle.Axis(
        axisLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        axisGuidelineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        axisLineColor = MaterialTheme.colorScheme.outline
    ),
    columnChart = com.patrykandpatrick.vico.compose.style.ChartStyle.ColumnChart(
        columns = emptyList()
    ),
    lineChart = com.patrykandpatrick.vico.compose.style.ChartStyle.LineChart(
        lines = emptyList()
    ),
    marker = com.patrykandpatrick.vico.compose.style.ChartStyle.Marker(),
    elevationOverlayColor = Color.Transparent
)

@Composable
private fun ChartLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = InstagramColors.PrimaryGradient.map { it.copy(alpha = 0.1f) }
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
            Text(
                text = "Loading chart data...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChartErrorState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📊",
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = "Chart data unavailable",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Unable to load price history",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}