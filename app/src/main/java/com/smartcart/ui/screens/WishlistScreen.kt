package com.smartcart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.smartcart.data.model.Product
import com.smartcart.data.repository.AppState
import com.smartcart.ui.components.CartPanel
import com.smartcart.ui.components.SharedSidebar
import com.smartcart.ui.components.SharedTopBar
import com.smartcart.ui.theme.*

@Composable
fun WishlistScreen(
    onNavigateHome: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateCats: () -> Unit,
    onNavigateToCart: () -> Unit,
) {
    val t = AppState.t()
    val lang = AppState.language
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("price_up") }

    val wishlistProducts = remember(AppState.wishlistIds, AppState.products) {
        AppState.products.filter { AppState.isInWishlist(it.id) }
    }
    val sortedProducts = remember(wishlistProducts, sortBy) {
        when (sortBy) {
            "price_down" -> wishlistProducts.sortedByDescending { it.price }
            "new" -> wishlistProducts.sortedByDescending { it.isNew }
            else -> wishlistProducts.sortedBy { it.price }
        }
    }
    val filteredProducts = remember(sortedProducts, searchQuery) {
        if (searchQuery.isBlank()) sortedProducts
        else {
            val q = searchQuery.trim().lowercase()
            sortedProducts.filter {
                it.localizedName(lang).lowercase().contains(q) ||
                    it.category.lowercase().contains(q)
            }
        }
    }

    Row(Modifier.background(Background).fillMaxSize()) {
        SharedSidebar(
            activeRoute = "favs",
            onNavigate = { r ->
                when (r) {
                    "home" -> onNavigateHome()
                    "list" -> onNavigateToList()
                    "cats" -> onNavigateCats()
                }
            }
        )
        Column(Modifier.weight(1f).fillMaxSize()) {
            SharedTopBar(searchQuery = searchQuery, onSearchQueryChange = { searchQuery = it })
            Column(
                Modifier.weight(1f).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "❤️ ${t.wishlistHeader}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (filteredProducts.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "price_up" to t.sortPriceUp,
                                "price_down" to t.sortPriceDown,
                                "new" to t.sortNew,
                            ).forEach { (id, label) ->
                                Surface(
                                    modifier = Modifier.clickable { sortBy = id },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (sortBy == id) PrimaryLight else White,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (sortBy == id) Primary else BorderStrong
                                    )
                                ) {
                                    Text(
                                        label,
                                        fontSize = 12.sp,
                                        fontWeight = if (sortBy == id) FontWeight.Bold else FontWeight.Normal,
                                        color = if (sortBy == id) Primary else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    filteredProducts.forEach { AppState.addToCart(it) }
                                    onNavigateToCart()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                Text(t.addAllToCart, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                if (filteredProducts.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFE5E7EB),
                                modifier = Modifier.size(80.dp)
                            )
                            Text(
                                t.emptyWishlist,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextMuted
                            )
                            Button(
                                onClick = onNavigateCats,
                                shape = RoundedCornerShape(12.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                Text(t.goToCatalog)
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(220.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredProducts) { product ->
                            WishlistProductCard(
                                product = product,
                                onAdd = { AppState.addToCart(product) },
                                onRemove = { AppState.toggleWishlist(product.id) }
                            )
                        }
                    }
                }
            }
        }
        CartPanel(onCheckout = onNavigateToCart)
    }
}

@Composable
private fun WishlistProductCard(
    product: Product,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
) {
    val lang = AppState.language
    val name = product.localizedName(lang)
    val inCart = AppState.isInCart(product.id)

    Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = White,
            shadowElevation = 2.dp
        ) {
            Column {
                Box(Modifier.fillMaxWidth().height(150.dp).background(Gray100)) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(White.copy(alpha = 0.9f))
                            .clickable { onRemove() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(Modifier.padding(12.dp)) {
                    Text(
                        name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        product.category,
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            product.formattedPrice(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(if (inCart) SuccessGreen else AccentOrange, CircleShape)
                                .clickable { onAdd() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (inCart) Icons.Rounded.Check else Icons.Rounded.Add,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
}
