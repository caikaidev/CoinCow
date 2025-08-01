package com.kcode.gankotlin.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object InstagramColors {
    // Primary gradient colors - Instagram style
    val PrimaryGradient = listOf(
        Color(0xFF833AB4), // Purple
        Color(0xFFE1306C), // Pink  
        Color(0xFFFD1D1D)  // Red
    )
    
    val SecondaryGradient = listOf(
        Color(0xFFF58529), // Orange
        Color(0xFFDD2A7B), // Pink
        Color(0xFF8134AF)  // Purple
    )
    
    // Background colors
    val BackgroundLight = Color(0xFFFAFAFA)
    val BackgroundDark = Color(0xFF121212)
    
    // Card backgrounds
    val CardBackground = Color(0xFFFFFFFF)
    val CardBackgroundDark = Color(0xFF1E1E1E)
    
    // Text colors
    val TextPrimary = Color(0xFF262626)
    val TextSecondary = Color(0xFF8E8E93)
    val TextPrimaryDark = Color(0xFFFFFFFF)
    val TextSecondaryDark = Color(0xFFB0B0B0)
    
    // Price colors
    val PriceUp = Color(0xFF00C851)   // Green
    val PriceDown = Color(0xFFFF4444) // Red
    
    // Surface colors
    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceDark = Color(0xFF1E1E1E)
    
    // Border colors
    val BorderLight = Color(0xFFE0E0E0)
    val BorderDark = Color(0xFF333333)
    
    // Ranking badge colors
    val RankingBadgeBackground = Color(0xFFF0F0F0)
    val RankingBadgeBackgroundDark = Color(0xFF2A2A2A)
    val RankingTextColor = Color(0xFF262626)
    val RankingTextColorDark = Color(0xFFFFFFFF)
    
    // Market cap text colors
    val MarketCapText = Color(0xFF8E8E93)
    val MarketCapTextDark = Color(0xFF6D6D70)
    
    // Skeleton loading colors
    val SkeletonLight = Color(0xFFE0E0E0)
    val SkeletonDark = Color(0xFF333333)
    
    // Gradient brushes
    val PrimaryGradientBrush = Brush.horizontalGradient(PrimaryGradient)
    val SecondaryGradientBrush = Brush.horizontalGradient(SecondaryGradient)
    val VerticalGradientBrush = Brush.verticalGradient(PrimaryGradient)
}