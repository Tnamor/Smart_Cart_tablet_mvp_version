package com.smartcart.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.smartcart.R
import com.smartcart.data.model.Product
import com.smartcart.data.repository.AppState
import com.smartcart.ui.components.CartPanel
import com.smartcart.ui.components.SharedSidebar
import com.smartcart.ui.components.SharedTopBar
import com.smartcart.ui.components.StoreMapOverlayDialog
import com.smartcart.ui.theme.*

@Composable
fun WishlistScreen(
    onNavigateHome: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateCats: () -> Unit,
    onNavigateSupport: () -> Unit = {},
    onNavigateToCart: () -> Unit,
) {
    val t = AppState.t()
    val lang = AppState.language
    var searchQuery by remember { mutableStateOf("") }
    var tappedProductForStoreMap by rememberSaveable { mutableStateOf<Product?>(null) }

    val wishlistProducts = remember(AppState.wishlistIds, AppState.products) {
        AppState.products.filter { AppState.isInWishlist(it.id) }
    }
    
    val filteredProducts = remember(wishlistProducts, searchQuery) {
        if (searchQuery.isBlank()) wishlistProducts
        else {
            val q = searchQuery.trim().lowercase()
            wishlistProducts.filter {
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
                    "support" -> onNavigateSupport()
                }
            }
        )
        Column(Modifier.weight(1f).fillMaxSize()) {
            SharedTopBar(searchQuery = searchQuery, onSearchQueryChange = { searchQuery = it })
            Column(
                Modifier.weight(1f).padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            t.wishlistHeader,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            t.wishlistHint,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (filteredProducts.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.Favorite, null, tint = Gray200, modifier = Modifier.size(80.dp))
                            Spacer(Modifier.height(16.dp))
                            Text(t.emptyWishlist, fontSize = 18.sp, color = TextMuted)
                            Spacer(Modifier.height(24.dp))
                            Button(onClick = onNavigateCats, shape = RoundedCornerShape(12.dp)) {
                                Text(t.goToCatalog)
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(filteredProducts) { product ->
                            WishlistProductCard(
                                product = product,
                                onRemove = { AppState.toggleWishlist(product.id) },
                                onProductTap = { tappedProductForStoreMap = product }
                            )
                        }
                    }
                }
            }
        }
        CartPanel(onCheckout = onNavigateToCart)

        tappedProductForStoreMap?.let { product ->
            StoreMapOverlayDialog(product = product, onDismiss = { tappedProductForStoreMap = null })
        }
    }
}

@Composable
private fun WishlistProductCard(
    product: Product,
    onRemove: () -> Unit,
    onProductTap: () -> Unit,
) {
    val lang = AppState.language
    val name = product.localizedName(lang)
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth().height(140.dp).clickable { onProductTap() },
        shape = RoundedCornerShape(20.dp),
        color = White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Gray100)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(product.imageUrl).crossfade(true).build(),
                    placeholder = painterResource(R.drawable.product_placeholder),
                    error = painterResource(R.drawable.product_placeholder),
                    contentDescription = name,
                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)).background(Gray100),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.9f))
                        .clickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Favorite, null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(product.category, fontSize = 12.sp, color = TextMuted)
                }
                
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(product.formattedPrice(), fontSize = 18.sp, fontWeight = FontWeight.Black, color = Primary)
                    
                    Surface(
                        color = PrimaryLight.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { onProductTap() }
                    ) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Map, null, tint = Primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Карта", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Primary)
                        }
                    }
                }
            }
        }
    }
}
