package com.kcode.gankotlin.presentation.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcode.gankotlin.ui.theme.InstagramColors
import kotlinx.coroutines.delay

/**
 * Instagram-style snackbar for user feedback
 */
@Composable
fun InstagramSnackbar(
    message: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    type: SnackbarType = SnackbarType.Info,
    duration: Long = 3000L
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = when (type) {
                            SnackbarType.Success -> Brush.horizontalGradient(
                                listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
                            )
                            SnackbarType.Error -> Brush.horizontalGradient(
                                listOf(Color(0xFFFF5722), Color(0xFFFF9800))
                            )
                            SnackbarType.Warning -> Brush.horizontalGradient(
                                listOf(Color(0xFFFF9800), Color(0xFFFFC107))
                            )
                            SnackbarType.Info -> InstagramColors.PrimaryGradientBrush
                        }
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = when (type) {
                            SnackbarType.Success -> "✅"
                            SnackbarType.Error -> "❌"
                            SnackbarType.Warning -> "⚠️"
                            SnackbarType.Info -> "ℹ️"
                        },
                        fontSize = 20.sp
                    )
                    
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
    
    LaunchedEffect(visible) {
        if (visible) {
            delay(duration)
            onDismiss()
        }
    }
}

enum class SnackbarType {
    Success,
    Error,
    Warning,
    Info
}