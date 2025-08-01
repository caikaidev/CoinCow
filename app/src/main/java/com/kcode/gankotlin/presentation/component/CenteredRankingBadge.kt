package com.kcode.gankotlin.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 居中对齐的排序徽章组件
 * 解决数字在背景容器中的居中对齐问题，支持动态宽度调整
 */
@Composable
fun CenteredRankingBadge(
    rank: Int,
    modifier: Modifier = Modifier
) {
    // 根据数字位数动态调整容器宽度
    val badgeWidth = when {
        rank < 10 -> 28.dp      // 个位数
        rank < 100 -> 32.dp     // 两位数  
        else -> 36.dp           // 三位数及以上
    }
    
    Surface(
        modifier = modifier.width(badgeWidth),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 紧凑版本的排序徽章，用于较小的屏幕或空间受限的场景
 */
@Composable
fun CompactRankingBadge(
    rank: Int,
    modifier: Modifier = Modifier
) {
    val badgeWidth = when {
        rank < 10 -> 24.dp      // 个位数
        rank < 100 -> 28.dp     // 两位数  
        else -> 32.dp           // 三位数及以上
    }
    
    Surface(
        modifier = modifier.width(badgeWidth),
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}