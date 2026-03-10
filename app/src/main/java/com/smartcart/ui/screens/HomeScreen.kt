package com.smartcart.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
    val finalTotal = (AppState.cartTotal + AppState.cartTax - AppState.cartDiscount).coerceAtLeast(0.0)

    var searchQuery by rememberSaveable { mutableStateOf("") }
    val visibleProducts = remember(products, searchQuery, lang) {
        if (searchQuery.isBlank()) {
            products.toList()
        } else {
            val q = searchQuery.trim().lowercase()
            products.filter { product ->
                product.localizedName(lang).lowercase().contains(q) ||
                    product.category.lowercase().contains(q)
            }
        }
    }

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
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it }
                )
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)) {

                // Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(listOf(
                                Primary, Color(0xFFB45AD1), Color(0xFFE05CA0), Color(0xFFF07850)
                            ))
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 32.dp).widthIn(max = 400.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(20.dp), color = White.copy(alpha = 0.2f)) {
                            Text("⚡ ${t.flashSale}", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                color = White, letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(t.weeklySavers, fontSize = 34.sp, fontWeight = FontWeight.Black,
                            color = White, lineHeight = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(t.bannerSub, fontSize = 13.sp, color = White.copy(alpha = 0.9f), fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(16.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, White),
                            modifier = Modifier.clickable {}
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(t.shopNow, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = White)
                                Icon(Icons.Rounded.ArrowForward, null, tint = White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    // Decorative shapes
                    Box(Modifier.align(Alignment.CenterEnd).padding(end = 40.dp)) {
                        Box(Modifier.size(120.dp, 150.dp).offset(20.dp, (-10).dp)
                            .background(White.copy(0.1f), RoundedCornerShape(14.dp))
                            .border(1.dp, White.copy(0.2f), RoundedCornerShape(14.dp)))
                        Box(Modifier.size(120.dp, 150.dp).offset(0.dp, 10.dp)
                            .background(White.copy(0.15f), RoundedCornerShape(14.dp))
                            .border(1.dp, White.copy(0.3f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.ShoppingBag, null, tint = White.copy(0.6f), modifier = Modifier.size(40.dp))
                        }
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
                            ProductCard(product, onAdd = { AppState.addToCart(product) })
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

        CameraDebugPanel(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun ProductCard(product: Product, onAdd: () -> Unit) {
    val lang    = AppState.language
    val name    = product.localizedName(lang)
    val quantity = AppState.cartQty(product.id)
    val inCart  = quantity > 0
    val context = LocalContext.current
    val badge   = when (product.id) { "1" -> Pair("-20%", ErrorRed); "7" -> Pair("BOGO", Primary); else -> null }

    Surface(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(16.dp),
        color = White,
        shadowElevation = 2.dp
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(150.dp).background(Gray100)) {
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
                        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = White,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                    }
                }
                Icon(Icons.Rounded.FavoriteBorder, null, tint = Color(0xFFCBD5E1),
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).size(16.dp))
            }
            Column(Modifier.padding(12.dp)) {
                Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${product.category}", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text(product.formattedPriceOld(), fontSize = 10.sp, color = Color(0xFFCBD5E1),
                            style = LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough))
                        Text(product.formattedPrice(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    if (inCart) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { AppState.updateCartQty(product.id, -1) }) {
                                Icon(Icons.Rounded.Remove, contentDescription = null, tint = Primary)
                            }
                            Text("$quantity", fontWeight = FontWeight.Bold, color = TextPrimary)
                            IconButton(onClick = { AppState.updateCartQty(product.id, 1) }) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = Primary)
                            }
                        }
                    } else {
                        FloatingActionButton(
                            onClick = onAdd,
                            containerColor = AccentOrange,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = White, modifier = Modifier.size(18.dp))
                        }
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
