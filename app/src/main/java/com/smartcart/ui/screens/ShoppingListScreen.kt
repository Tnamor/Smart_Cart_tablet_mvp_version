package com.smartcart.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.smartcart.data.model.Product
import com.smartcart.data.model.ShoppingListItem
import com.smartcart.data.repository.AppState
import com.smartcart.ui.components.SharedSidebar
import com.smartcart.ui.components.SharedTopBar
import com.smartcart.ui.components.StoreMapOverlayDialog
import com.smartcart.ui.theme.*

@Composable
fun ShoppingListScreen(
    onNavigateToCart: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateCats: () -> Unit,
    onNavigateWishlist: () -> Unit,
    onNavigateSupport: () -> Unit = {},
) {
    val t    = AppState.t()
    val list = AppState.shoppingList

    var activeFilter by remember { mutableStateOf("all") }
    var tappedProductForStoreMap by remember { mutableStateOf<Product?>(null) }

    val filtered: List<ShoppingListItem> = when (activeFilter) {
        "produce" -> list.filter { it.product.category == "Fruits" || it.product.category == "Vegetables" }
        "dairy"   -> list.filter { it.product.category == "Dairy" }
        "bakery"  -> list.filter { it.product.category == "Bakery" }
        else      -> list.toList()
    }

    Box(Modifier.fillMaxSize().background(Background)) {
        Row(Modifier.fillMaxSize()) {
            SharedSidebar(
                activeRoute = "list",
                onNavigate = { r ->
                    when (r) {
                        "home" -> onNavigateHome()
                        "cats" -> onNavigateCats()
                        "favs" -> onNavigateWishlist()
                        "support" -> onNavigateSupport()
                    }
                }
            )

            Column(Modifier.weight(1f).fillMaxHeight()) {
                SharedTopBar(
                    searchQuery = "",
                    onSearchQueryChange = {}
                )

                // Page header
                Surface(Modifier.fillMaxWidth(), color = White, shadowElevation = 0.dp) {
                    Column {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 20.dp),
                            Arrangement.SpaceBetween, Alignment.CenterVertically
                        ) {
                            Column {
                                Text(t.myShoppingList, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(t.foundItems.replace("{n}", list.size.toString()),
                                    fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                            }
                            Text(
                                text = "AI-камера автоматически отмечает товары из списка при добавлении в корзину.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.width(300.dp)
                            )
                        }

                        Row(
                            Modifier.fillMaxWidth().padding(start = 32.dp, end = 32.dp, bottom = 14.dp),
                            Arrangement.SpaceBetween, Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                FilterPill(t.allItems,   activeFilter == "all",     { activeFilter = "all" })
                                FilterPill(t.produce,    activeFilter == "produce", { activeFilter = "produce" })
                                FilterPill(t.dairyEggs,  activeFilter == "dairy",   { activeFilter = "dairy" })
                                FilterPill(t.bakery,     activeFilter == "bakery",  { activeFilter = "bakery" })
                            }
                            
                            Surface(shape = RoundedCornerShape(12.dp), color = White, border = BorderStroke(1.dp, BorderStrong)) {
                                Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(t.sortByAisle, fontSize = 12.sp, color = TextSecondary)
                                    Icon(Icons.Rounded.KeyboardArrowDown, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                        HorizontalDivider(color = Border)
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f).padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(filtered, key = { it.product.id }) { item ->
                        ListItemCard(
                            product = item.product,
                            plannedQty = item.plannedQuantity,
                            inCart  = item.isInCart,
                            inCartQty = item.inCartQuantity,
                            onOpenStoreMap = { tappedProductForStoreMap = item.product }
                        )
                    }
                }
            }
        }

        // Bottom bar
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(start = 72.dp),
            color = White, shadowElevation = 8.dp, border = BorderStroke(1.dp, Border)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column {
                        Text(t.estimatedTotal, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(AppState.listTotal.asPrice(), fontSize = 24.sp, fontWeight = FontWeight.Black, color = Primary)
                    }
                    Box(Modifier.height(40.dp).width(1.dp).background(Border))
                    Column {
                        Text(t.itemsInCart, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${AppState.cartCount}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = SuccessGreen)
                            Text(" / ${list.size}", fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
                Button(onClick = onNavigateToCart,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(t.checkoutNow, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Rounded.ArrowForward, null, modifier = Modifier.size(18.dp))
                }
            }
        }

        tappedProductForStoreMap?.let { product ->
            StoreMapOverlayDialog(product = product, onDismiss = { tappedProductForStoreMap = null })
        }
    }
}

@Composable
private fun FilterPill(label: String, active: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (active) Primary else White,
        border = if (!active) BorderStroke(1.dp, BorderStrong) else null,
        modifier = Modifier.clickable { onClick() }.height(38.dp)
    ) {
        Box(Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            Text(label, fontSize = 13.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                color = if (active) White else TextSecondary)
        }
    }
}

@Composable
private fun ListItemCard(
    product: Product,
    plannedQty: Int,
    inCart: Boolean,
    inCartQty: Int,
    onOpenStoreMap: () -> Unit,
) {
    val lang  = AppState.language
    val name  = product.localizedName(lang)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = White,
        border = BorderStroke(1.dp, if (inCart) SuccessGreen.copy(0.5f) else Border),
        modifier = Modifier.fillMaxWidth().height(120.dp).clickable { onOpenStoreMap() }
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = product.imageUrl, contentDescription = name,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Gray100),
                contentScale = ContentScale.Crop)
            
            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                        Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        
                        if (inCart) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                        }
                    }
                    Text(product.category, fontSize = 12.sp, color = TextMuted)
                }

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) {
                    Text(product.formattedPrice(), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
                    
                    Surface(
                        color = if (inCart) SuccessGreen.copy(0.1f) else Gray100,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (inCart) "${AppState.t().inCartQty}: $inCartQty/$plannedQty" else "${AppState.t().planned}: $plannedQty",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (inCart) SuccessGreen else TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun Double.asPrice() = com.smartcart.data.CurrencyConfig.format(this)

@Preview(showBackground = true, device = "spec:width=1920dp,height=1104dp,dpi=160")
@Composable
private fun Preview() {
    SmartCartTheme {
        ShoppingListScreen(
            onNavigateToCart = {},
            onNavigateHome = {},
            onNavigateCats = {},
            onNavigateWishlist = {},
        )
    }
}
