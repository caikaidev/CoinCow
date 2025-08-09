package com.kcode.gankotlin.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kcode.gankotlin.presentation.screen.CoinDetailScreen
import com.kcode.gankotlin.presentation.screen.MarketScreen
import com.kcode.gankotlin.presentation.screen.SearchScreen
import com.kcode.gankotlin.presentation.screen.SettingsScreen
import com.kcode.gankotlin.presentation.screen.WatchlistScreen

/**
 * Navigation destinations
 */
sealed class Screen(val route: String) {
    object Watchlist : Screen("watchlist")
    object Market : Screen("market")
    object Search : Screen("search")
    object Settings : Screen("settings")
    object CoinDetail : Screen("coin_detail/{coinId}") {
        fun createRoute(coinId: String) = "coin_detail/$coinId"
    }
}

/**
 * Main navigation component
 */
@Composable
fun CryptoNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Watchlist.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Watchlist.route) {
            WatchlistScreen(
                onCoinClick = { coinId ->
                    navController.navigate(Screen.CoinDetail.createRoute(coinId))
                },
                onAddCoinClick = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }
        
        composable(Screen.Market.route) {
            MarketScreen(
                onCoinClick = { coinId ->
                    navController.navigate(Screen.CoinDetail.createRoute(coinId))
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }
        
        composable(Screen.Search.route) {
            SearchScreen(
                onCoinClick = { coinId ->
                    navController.navigate(Screen.CoinDetail.createRoute(coinId))
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        
        composable(Screen.CoinDetail.route) { backStackEntry ->
            val coinId = backStackEntry.arguments?.getString("coinId") ?: ""
            CoinDetailScreen(
                coinId = coinId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

