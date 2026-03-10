package com.smartcart.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.smartcart.R
import com.smartcart.data.model.Product
import com.smartcart.data.repository.AppState
import com.smartcart.ui.components.CameraDebugPanel
import com.smartcart.ui.components.CartPanel
import com.smartcart.ui.components.SharedSidebar
import com.smartcart.ui.components.SharedTopBar
import com.smartcart.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToCart: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateDeals: () -> Unit = {},
    onNavigateCats: () -> Unit = {},
    onNavigateWishlist: () -> Unit = {},
) {
    val t        = AppState.t()
    val lang     = AppState.language
    val products = AppState.products
    val cart     = AppState.cart
    val totalAmount = AppState.cartTotal + AppState.cartTax - AppState.cartDiscount

    val visibleProducts = remember(products, lang) { products.toList() }

    Box(Modifier.fillMaxSize().background(Background)) {

        Row(Modifier.fillMaxSize()) {

            SharedSidebar(activeRoute = "home", onNavigate = { r ->
                when (r) {
                    "list" -> onNavigateToList()
                    "deals" -> onNavigateDeals()
                    "cats" -> onNavigateCats()
                    "favs" -> onNavigateWishlist()
                }
            })

            // Main content
            Column(Modifier.weight(1f).fillMaxHeight()) {
                SharedTopBar(
                    searchQuery = "",
                    onSearchQueryChange = {}
                )
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)) {

                // Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(160.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(listOf(BannerStart, BannerEnd))
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 24.dp).widthIn(max = 420.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(100.dp), color = White.copy(alpha = 0.2f)) {
                            Text(t.flashSale, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                color = White, letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(t.weeklySavers, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = White)
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = {}, border = BorderStroke(1.5.dp, White), shape = RoundedCornerShape(100.dp)) {
                            Text(t.shopNow, color = White)
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Rounded.ArrowForward, null, tint = White, modifier = Modifier.size(14.dp))
                        }
                    }
                    Box(Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=200",
                            contentDescription = null,
                            modifier = Modifier.size(74.dp).offset(x = (-40).dp, y = (-10).dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1490474418585-ba9bad8fd0ea?w=200",
                            contentDescription = null,
                            modifier = Modifier.size(74.dp).offset(x = 0.dp, y = 10.dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Trending section
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🔥", fontSize = 16.sp)
                            Text(t.trendingOffers, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        TextButton(onClick = {}) {
                            Text("${t.viewAll} →", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Primary)
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(visibleProducts, key = { it.id }) { product ->
                            ProductCard(product)
                        }
                    }
                }

                // Category grid
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CategoryBox(t.produce, Color(0xFFEFF6FF), Color(0xFF1E40AF),
                        "https://picsum.photos/seed/produce/100/100", Modifier.weight(1f))
                    CategoryBox(t.bakery, Color(0xFFFFF7ED), Color(0xFF9A3412),
                        "https://picsum.photos/seed/bakery/100/100", Modifier.weight(1f))
                }
            }
        }

            // Right cart panel
            CartPanel(onCheckout = onNavigateToCart)
        }

        if (cart.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 80.dp, end = 300.dp, bottom = 12.dp),
                shadowElevation = 10.dp,
                color = White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Icon(Icons.Outlined.ShoppingCart, null, tint = Primary, modifier = Modifier.size(24.dp))
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ErrorRed),
                            contentAlignment = Alignment.Center
                        ) { Text("${cart.sumOf { it.quantity }}", color = White, fontSize = 10.sp) }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(t.autoCart, fontSize = 12.sp, color = TextSecondary)
                        Text("${cart.sumOf { it.quantity }}${t.itemsSuffix}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(t.total, fontSize = 11.sp, color = TextSecondary)
                        Text("${(totalAmount * 130).toInt()} ₸", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(16.dp))
                    Button(
                        onClick = onNavigateToCart,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(t.checkoutNow, color = White)
                        Spacer(Modifier.width(6.dp))
                        Text("→", color = White)
                    }
                }
            }
        }

        CameraDebugPanel(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 76.dp))
    }
}

@Composable
private fun ProductCard(product: Product) {
    val lang    = AppState.language
    val name    = product.localizedName(lang)
    val context = LocalContext.current
    val inWishlist = AppState.isInWishlist(product.id)
    val badge   = when (product.id) { "1" -> Pair("20", ErrorRed); "7" -> Pair("15", ErrorRed); else -> null }

    Surface(
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        color = White,
        shadowElevation = 2.dp
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(180.dp).background(Gray100)) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(product.imageUrl).crossfade(true).build(),
                    placeholder = painterResource(R.drawable.product_placeholder),
                    error = painterResource(R.drawable.product_placeholder),
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
                badge?.let { (text, color) ->
                    Surface(modifier = Modifier.padding(10.dp).align(Alignment.TopStart), shape = RoundedCornerShape(6.dp), color = color) {
                        Text("-$text%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = White,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                    }
                }
                IconButton(onClick = { AppState.toggleWishlist(product.id) }, modifier = Modifier.align(Alignment.TopEnd)) {
                    if (inWishlist) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = ErrorRed
                        )
                    } else {
                        Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = White)
                    }
                }
            }
            Column(Modifier.padding(12.dp)) {
                Text(name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${product.unit} • ${product.category}", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.formattedPriceOld(),
                        color = Color(0xFF9CA3AF),
                        fontSize = 13.sp,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(product.formattedPrice(), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFF3F4F6)).padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Primary))
                        Spacer(Modifier.width(6.dp))
                        Text("Auto-detected by AI camera", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBox(label: String, bg: Color, textColor: Color, imgUrl: String, modifier: Modifier) {
    Surface(modifier.height(150.dp).clip(RoundedCornerShape(16.dp)).clickable {}, color = bg,
        border = BorderStroke(1.dp, bg.copy(alpha = 0.5f))) {
        Box(Modifier.fillMaxSize().padding(20.dp)) {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
            AsyncImage(model = imgUrl, contentDescription = null,
                modifier = Modifier.size(80.dp).align(Alignment.BottomEnd).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop)
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1920dp,height=1104dp,dpi=160")
@Composable
private fun Preview() { SmartCartTheme { HomeScreen({}, {}) } }
