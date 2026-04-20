package com.smartcart.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.TrendingUp
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.smartcart.R
import com.smartcart.data.model.Product
import com.smartcart.data.repository.AppState
import com.smartcart.ui.components.CameraDebugPanel
import com.smartcart.ui.components.CartPanel
import com.smartcart.ui.components.SharedSidebar
import com.smartcart.ui.components.SharedTopBar
import com.smartcart.ui.components.StoreMapOverlayDialog
import com.smartcart.data.CurrencyConfig
import com.smartcart.data.model.CartItem
import com.smartcart.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToCart: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateDeals: () -> Unit = {},
    onNavigateCats: () -> Unit = {},
    onNavigateWishlist: () -> Unit = {},
    onNavigateSupport: () -> Unit = {},
    onSessionEnded: () -> Unit,   // <-- добавить
) {


    val db = FirebaseFirestore.getInstance()
    val cartId = "cart_001"
    var listenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }

    LaunchedEffect(cartId) {
        listenerRegistration?.remove()

        listenerRegistration = db.collection("carts")
            .document(cartId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) return@addSnapshotListener
                if (snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val statusValue = snapshot.getString("status") ?: "available"

                if (statusValue == "available") {
                    AppState.cart.clear()
                    AppState.shoppingList.clear()
                    AppState.currentUser = null
                    onSessionEnded()
                    return@addSnapshotListener
                }

                val items = snapshot.get("items") as? List<Map<String, Any>> ?: emptyList()

                val parsedItems = items.mapNotNull { item ->
                    try {
                        val id = item["id"] as? String
                        val barcode = item["barcode"] as? String ?: ""
                        val name = item["name"] as? String ?: "Unknown product"
                        val quantity = (item["quantity"] as? Number)?.toInt() ?: 1
                        val price = (item["price"] as? Number)?.toDouble() ?: 0.0
                        val imageUrl = item["imageUrl"] as? String ?: ""
                        val brand = item["brand"] as? String ?: ""
                        val imageEmoji = item["imageEmoji"] as? String ?: ""

                        val existingProduct = AppState.products.find { p ->
                            (id != null && p.id == id) || (barcode.isNotBlank() && p.barcode == barcode)
                        }

                        val product = existingProduct ?: Product(
                            id = id ?: barcode.ifBlank { name },
                            nameEn = name,
                            nameRu = name,
                            nameKk = name,
                            price = price,
                            imageUrl = imageUrl,
                            category = brand.ifBlank { "Scanned" },
                            isNew = false,
                            barcode = barcode,
                            unit = "шт",
                            zoneId = id ?: barcode.ifBlank { name }
                        )

                        CartItem(
                            product = product,
                            quantity = quantity,
                            addedByCamera = item["addedByCamera"] as? Boolean ?: false,
                            addedManually = item["addedManually"] as? Boolean ?: false
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                AppState.cart.clear()
                AppState.cart.addAll(parsedItems)
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            listenerRegistration?.remove()
        }
    }


    val t        = AppState.t()
    val lang     = AppState.language
    val products = AppState.products
    val cart     = AppState.cart
    val totalAmount = AppState.cartTotal + AppState.cartTax - AppState.cartDiscount

    var tappedProductForStoreMap by rememberSaveable { mutableStateOf<Product?>(null) }

    val visibleProducts = remember(products, lang) { products.toList() }

    Box(Modifier.fillMaxSize().background(Background)) {

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically { it / 12 },
            exit = fadeOut()
        ) {
            Row(Modifier.fillMaxSize()) {

                SharedSidebar(activeRoute = "home", onNavigate = { r ->
                    when (r) {
                        "list" -> onNavigateToList()
                        "cats" -> onNavigateCats()
                        "favs" -> onNavigateWishlist()
                        "support" -> onNavigateSupport()
                    }
                })

                // Main content
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    SharedTopBar(
                        searchQuery = "",
                        onSearchQueryChange = {}
                    )
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)) {

                        // Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth().height(180.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(BannerStart, BannerEnd))
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(24.dp).widthIn(max = 500.dp)
                            ) {
                                Surface(shape = RoundedCornerShape(100.dp), color = White.copy(alpha = 0.2f)) {
                                    Text(t.flashSale, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                        color = White, letterSpacing = 1.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(t.weeklySavers, fontSize = 32.sp, fontWeight = FontWeight.Black, color = White, lineHeight = 38.sp)
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = onNavigateDeals, 
                                    colors = ButtonDefaults.buttonColors(containerColor = White),
                                    shape = RoundedCornerShape(100.dp)
                                ) {
                                    Text(t.shopNow, color = Primary, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.Rounded.ArrowForward, null, tint = Primary, modifier = Modifier.size(16.dp))
                                }
                            }
                            Box(Modifier.align(Alignment.CenterEnd).padding(end = 32.dp)) {
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=300",
                                    contentDescription = null,
                                    modifier = Modifier.size(100.dp).offset(x = (-60).dp, y = (-20).dp).clip(RoundedCornerShape(16.dp)).border(2.dp, White.copy(0.3f), RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1490474418585-ba9bad8fd0ea?w=300",
                                    contentDescription = null,
                                    modifier = Modifier.size(100.dp).offset(x = 0.dp, y = 20.dp).clip(RoundedCornerShape(16.dp)).border(2.dp, White.copy(0.3f), RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        // Trending section
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(
                                        imageVector = Icons.Rounded.TrendingUp,
                                        contentDescription = null,
                                        tint = Color(0xFF7C3AED), // Same Primary color
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(t.trendingOffers, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                                }
                                TextButton(onClick = onNavigateCats) {
                                    Text(t.viewAll, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Primary)
                                    Spacer(Modifier.width(4.dp))
                                    Text("→", fontSize = 16.sp, color = Primary)
                                }
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 8.dp)) {
                                items(visibleProducts, key = { it.id }) { product ->
                                    ProductCard(product, onProductTap = { tappedProductForStoreMap = product })
                                }
                            }
                        }

                        // Category grid shortcuts
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            CategoryBox(t.produce, Color(0xFFECFDF5), Color(0xFF065F46),
                                "https://images.unsplash.com/photo-1610348725531-843dff563e2c?w=200", Modifier.weight(1f)) { onNavigateCats() }
                            CategoryBox(t.bakery, Color(0xFFFFF7ED), Color(0xFF9A3412),
                                "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=200", Modifier.weight(1f)) { onNavigateCats() }
                        }
                    }
                }

                // Right cart panel
                CartPanel(onCheckout = onNavigateToCart)
            }
        }

        if (cart.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 72.dp, end = 300.dp, bottom = 24.dp),
                shadowElevation = 12.dp,
                color = White,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Primary.copy(0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.ShoppingCart,
                        null,
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(t.autoCart, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                        Text("${cart.sumOf { it.quantity }}${t.itemsSuffix}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(Modifier.width(24.dp))
                    Box(Modifier.width(1.dp).height(32.dp).background(Border))
                    Spacer(Modifier.width(24.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(t.total, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                        Text(CurrencyConfig.format(totalAmount), fontWeight = FontWeight.Black, fontSize = 20.sp, color = Primary)
                    }
                    Spacer(Modifier.width(24.dp))
                    Button(
                        onClick = onNavigateToCart,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(t.checkoutNow, color = White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Rounded.ArrowForward, null, tint = White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        CameraDebugPanel(modifier = Modifier.align(Alignment.BottomStart).padding(start = 88.dp, bottom = 24.dp))
    }

    tappedProductForStoreMap?.let { tapped ->
        StoreMapOverlayDialog(
            product = tapped,
            onDismiss = { tappedProductForStoreMap = null }
        )
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onProductTap: () -> Unit,
) {
    val lang    = AppState.language
    val name    = product.localizedName(lang)
    val context = LocalContext.current
    val inWishlist = AppState.isInWishlist(product.id)
    val badge   = when (product.id) { "1" -> Pair("20", ErrorRed); "7" -> Pair("15", ErrorRed); else -> null }

    Surface(
        modifier = Modifier.width(240.dp).height(340.dp).clickable { onProductTap() },
        shape = RoundedCornerShape(20.dp),
        color = White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Gray100)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(160.dp).background(Gray100)) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(product.imageUrl).crossfade(true).build(),
                    placeholder = painterResource(R.drawable.product_placeholder),
                    error = painterResource(R.drawable.product_placeholder),
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop
                )
                badge?.let { (text, color) ->
                    Surface(modifier = Modifier.padding(12.dp).align(Alignment.TopStart), shape = RoundedCornerShape(8.dp), color = color) {
                        Text("-$text%", fontSize = 11.sp, fontWeight = FontWeight.Black, color = White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
                IconButton(
                    onClick = { AppState.toggleWishlist(product.id) }, 
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                ) {
                    Box(Modifier.size(32.dp).background(White.copy(alpha = 0.8f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (inWishlist) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = if (inWishlist) ErrorRed else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Column(Modifier.padding(16.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(product.category, fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(top = 4.dp))
                }
                
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(product.formattedPrice(), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = product.formattedPriceOld(),
                            color = TextMuted,
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Gray100,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            Modifier.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Primary))
                            Spacer(Modifier.width(6.dp))
                            Text(AppState.t().willBeAddedByCamera, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBox(label: String, bg: Color, textColor: Color, imgUrl: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier.height(140.dp).clip(RoundedCornerShape(20.dp)).clickable { onClick() }, color = bg,
        border = BorderStroke(1.dp, bg.copy(alpha = 0.5f))) {
        Box(Modifier.fillMaxSize().padding(24.dp)) {
            Text(label, fontSize = 18.sp, fontWeight = FontWeight.Black, color = textColor)
            AsyncImage(model = imgUrl, contentDescription = null,
                modifier = Modifier.size(90.dp).align(Alignment.BottomEnd).offset(x = 10.dp, y = 10.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop)
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1920dp,height=1104dp,dpi=160")
@Composable
private fun Preview() { SmartCartTheme { HomeScreen({}, {}, {}, {}, {}, {}, {}) } }
