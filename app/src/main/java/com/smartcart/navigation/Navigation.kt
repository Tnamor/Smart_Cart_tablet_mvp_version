package com.smartcart.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartcart.data.repository.AppState
import com.smartcart.ui.screens.*
import androidx.compose.runtime.LaunchedEffect
import android.util.Log

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val start = if (AppState.isLoggedIn) "home" else "login"

    fun resetToLogin() {
        Log.d("CART_DEBUG", "resetToLogin() called")

        AppState.logout()

        nav.navigate("login") {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    LaunchedEffect(AppState.releaseEvent) {
        if (AppState.releaseEvent > 0) {
            Log.d("CART_DEBUG", "releaseEvent received -> navigate to login")

            nav.navigate("login") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

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
                onNavigateWishlist = { nav.navigate("favs") { launchSingleTop = true } },
                onNavigateSupport = { nav.navigate("support") { launchSingleTop = true } },
                onSessionEnded = {
                    nav.navigate("login") {
                        popUpTo(0)
                    }
                },
                onOpenCamera = { nav.navigate("camera") }

            )
        }
        composable("deals") {
            DealsScreen(
                onNavigateHome = { nav.navigate("home") { launchSingleTop = true } },
                onNavigateToList = { nav.navigate("list") { launchSingleTop = true } },
                onNavigateCats = { nav.navigate("cats") { launchSingleTop = true } },
                onNavigateWishlist = { nav.navigate("favs") { launchSingleTop = true } },
                onNavigateSupport = { nav.navigate("support") { launchSingleTop = true } },
                onNavigateToCart = { nav.navigate("cart") }
            )
        }
        composable("cats") {
            CategoriesScreen(
                onNavigateHome = { nav.navigate("home") { launchSingleTop = true } },
                onNavigateToList = { nav.navigate("list") { launchSingleTop = true } },
                onNavigateWishlist = { nav.navigate("favs") { launchSingleTop = true } },
                onNavigateSupport = { nav.navigate("support") { launchSingleTop = true } },
                onNavigateToCart = { nav.navigate("cart") }
            )
        }
        composable("favs") {
            WishlistScreen(
                onNavigateHome = { nav.navigate("home") { launchSingleTop = true } },
                onNavigateToList = { nav.navigate("list") { launchSingleTop = true } },
                onNavigateCats = { nav.navigate("cats") { launchSingleTop = true } },
                onNavigateSupport = { nav.navigate("support") { launchSingleTop = true } },
                onNavigateToCart = { nav.navigate("cart") }
            )
        }
        composable("support") {
            SupportScreen(
                onNavigateHome = { nav.navigate("home") { launchSingleTop = true } },
                onNavigateToList = { nav.navigate("list") { launchSingleTop = true } },
                onNavigateCats = { nav.navigate("cats") { launchSingleTop = true } },
                onNavigateFavs = { nav.navigate("favs") { launchSingleTop = true } },
                onNavigateToCart = { nav.navigate("cart") }
            )
        }
        composable("list") {
            ShoppingListScreen(
                onNavigateToCart = {
                    nav.navigate("cart") { popUpTo("list") { inclusive = true } }
                },
                onNavigateHome = { nav.navigate("home") { launchSingleTop = true } },
                onNavigateCats = { nav.navigate("cats") { launchSingleTop = true } },
                onNavigateWishlist = { nav.navigate("favs") { launchSingleTop = true } }
            )
        }
        composable("cart") {
            CartScreen(
                onNavigateToList = { nav.navigate("list") },
                onNavigateToReceipt = { receiptId ->
                    nav.navigate("receipt/$receiptId")
                }
            )
        }
        composable("receipt/{receiptId}") { backStackEntry ->
            val receiptId = backStackEntry.arguments?.getString("receiptId") ?: ""

            ReceiptScreen(
                receiptId = receiptId,
                onBack = {
                    resetToLogin()
                }
            )
        }

        composable("camera") {
            CameraScreen(
                cartId = "cart_001",
                onBack = {
                    nav.popBackStack()
                }
            )
        }

    }
}
