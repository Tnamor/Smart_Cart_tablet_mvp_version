package com.smartcart.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartcart.data.repository.AppState
import com.smartcart.ui.screens.*

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val start = if (AppState.currentUser != null) "home" else "login"

    NavHost(nav, startDestination = start) {
        composable("login") {
            LoginScreen(onLoginSuccess = {
                nav.navigate("home") {
                    popUpTo(nav.graph.startDestinationId) { inclusive = true }
                }
            })
        }
        composable("home") {
            HomeScreen(
                onNavigateToCart = { nav.navigate("cart") },
                onNavigateToList = { nav.navigate("list") },
                onNavigateDeals = { nav.navigate("deals") { launchSingleTop = true } },
                onNavigateCats = { nav.navigate("cats") { launchSingleTop = true } },
                onNavigateWishlist = { nav.navigate("favs") { launchSingleTop = true } }
            )
        }
        composable("deals") {
            DealsScreen(
                onNavigateHome = { nav.navigate("home") { launchSingleTop = true } },
                onNavigateCats = { nav.navigate("cats") { launchSingleTop = true } },
                onNavigateWishlist = { nav.navigate("favs") { launchSingleTop = true } },
                onNavigateToCart = { nav.navigate("cart") }
            )
        }
        composable("cats") {
            CategoriesScreen(
                onNavigateHome = { nav.navigate("home") { launchSingleTop = true } },
                onNavigateDeals = { nav.navigate("deals") { launchSingleTop = true } },
                onNavigateWishlist = { nav.navigate("favs") { launchSingleTop = true } },
                onNavigateToCart = { nav.navigate("cart") }
            )
        }
        composable("favs") {
            WishlistScreen(
                onNavigateHome = { nav.navigate("home") { launchSingleTop = true } },
                onNavigateDeals = { nav.navigate("deals") { launchSingleTop = true } },
                onNavigateCats = { nav.navigate("cats") { launchSingleTop = true } },
                onNavigateToCart = { nav.navigate("cart") }
            )
        }
        composable("list") {
            ShoppingListScreen(onNavigateToCart = {
                nav.navigate("cart") { popUpTo("list") { inclusive = true } }
            })
        }
        composable("cart") {
            CartScreen(
                onNavigateToList = { nav.navigate("list") },
                onNavigateToReceipt = { nav.navigate("receipt") }
            )
        }
        composable("receipt") {
            ReceiptScreen(onBackToLogin = {
                AppState.logout()
                nav.navigate("login") {
                    popUpTo(nav.graph.startDestinationId) { inclusive = true }
                }
            })
        }
    }
}
