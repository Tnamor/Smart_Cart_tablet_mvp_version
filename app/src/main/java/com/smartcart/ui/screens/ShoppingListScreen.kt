package com.smartcart.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import com.smartcart.ui.theme.*

@Composable
fun ShoppingListScreen(
    onNavigateToCart: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateCats: () -> Unit,
    onNavigateWishlist: () -> Unit,
) {
    val t    = AppState.t()
    val lang = AppState.language
    val list = AppState.shoppingList
    val cart = AppState.cart

    var activeFilter by remember { mutableStateOf("all") }

    val filtered: List<ShoppingListItem> = when (activeFilter) {
        "produce" -> list.filter { it.product.category == "Produce" }
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
                    }
                }
            )

            Column(Modifier.weight(1f).fillMaxHeight()) {

                // Page header (white, matches React px-8 py-6)
                Surface(Modifier.fillMaxWidth(), color = White, shadowElevation = 0.dp,
                    border = BorderStroke(0.dp, Color.Transparent)) {
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
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                // Search
                                Surface(Modifier.width(240.dp), RoundedCornerShape(22.dp), Background,
                                    border = BorderStroke(1.dp, BorderStrong)) {
                                    Row(Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Rounded.Search, null, tint = TextMuted, modifier = Modifier.size(15.dp))
                                        Text(t.addItemSearch, fontSize = 12.sp, color = TextMuted)
                                    }
                                }
                                // User
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Anna K.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text("Loyalty Member", fontSize = 10.sp, color = Primary, fontWeight = FontWeight.Medium)
                                    }
                                    Box(
                                        Modifier.size(38.dp).background(PrimaryLight, CircleShape)
                                            .border(2.dp, White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("A", fontWeight = FontWeight.Bold, color = Primary, fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        // Filter bar
                        Row(
                            Modifier.fillMaxWidth().padding(start = 32.dp, end = 32.dp, bottom = 14.dp),
                            Arrangement.SpaceBetween, Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterPill(t.allItems,   activeFilter == "all",     { activeFilter = "all" })
                                FilterPill(t.produce,    activeFilter == "produce", { activeFilter = "produce" })
                                FilterPill(t.dairyEggs,  activeFilter == "dairy",   { activeFilter = "dairy" })
                                FilterPill(t.bakery,     activeFilter == "bakery",  { activeFilter = "bakery" })
                                // Sort by Aisle
                                Surface(shape = RoundedCornerShape(20.dp), color = White,
                                    border = BorderStroke(1.dp, BorderStrong)) {
                                    Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(t.sortByAisle, fontSize = 12.sp, color = TextSecondary)
                                        Icon(Icons.Rounded.KeyboardArrowDown, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            // Add all to cart
                            Button(onClick = { AppState.moveListToCart(); onNavigateToCart() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TextPrimary),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Icon(Icons.Rounded.ShoppingCart, null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(t.addAllToCart, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        HorizontalDivider(color = Border)
                    }
                }

                // Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f).padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(filtered, key = { it.product.id }) { item ->
                        ListItemCard(
                            product = item.product,
                            plannedQty = item.plannedQuantity,
                            inCart  = item.isInCart,
                            inCartQty = item.inCartQuantity,
                            onAddToCart   = { AppState.addToCart(item.product) },
                            onIncCart     = { AppState.updateCartQty(item.product.id, +1) },
                            onDecCart     = { AppState.updateCartQty(item.product.id, -1) },
                            onIncPlanned  = { AppState.updateShoppingListPlannedQty(item.product.id, +1) },
                            onDecPlanned  = { AppState.updateShoppingListPlannedQty(item.product.id, -1) },
                        )
                    }
                }
            }
        }

        // Bottom bar — absolute bottom (matches React absolute bottom-0)
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = White, shadowElevation = 8.dp,
            border = BorderStroke(1.dp, Border)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 14.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.size(44.dp).background(SuccessGreen.copy(0.1f), CircleShape),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.ShoppingCart, null, tint = SuccessGreen, modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Text(t.estimatedTotal, fontSize = 10.sp, color = TextSecondary,
                            fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                        Text(AppState.listTotal.asTenge(), fontSize = 22.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                    }
                    Box(Modifier.height(32.dp).width(1.dp).background(Border))
                    Column {
                        Text(t.itemsInCart, fontSize = 10.sp, color = TextSecondary,
                            fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${AppState.cartCount}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Primary)
                            Text(" / ${list.size}", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 1.dp))
                        }
                    }
                }
                Button(onClick = { AppState.moveListToCart(); onNavigateToCart() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
                ) {
                    Text(t.checkoutNow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Rounded.ArrowForward, null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, active: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (active) Primary else White,
        border = if (!active) BorderStroke(1.dp, BorderStrong) else null,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (active) Text("✓ ", fontSize = 12.sp, color = White, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 12.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
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
    onAddToCart: () -> Unit,
    onIncCart: () -> Unit,
    onDecCart: () -> Unit,
    onIncPlanned: () -> Unit,
    onDecPlanned: () -> Unit,
) {
    val lang  = AppState.language
    val name  = product.localizedName(lang)
    val badge = when (product.category) {
        "Produce" -> Pair("Bio",  SuccessGreen)
        "Bakery"  -> Pair("Sale", AccentOrange)
        else      -> null
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = White,
        border = BorderStroke(if (inCart) 1.5.dp else 1.dp, if (inCart) Primary.copy(0.3f) else Border),
        shadowElevation = if (inCart) 2.dp else 0.dp
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically) {

            // Image + badge
            Box(Modifier.size(72.dp)) {
                AsyncImage(model = product.imageUrl, contentDescription = name,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).background(Gray100),
                    contentScale = ContentScale.Crop)
                badge?.let { (text, color) ->
                    Surface(modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
                        shape = RoundedCornerShape(4.dp), color = color) {
                        Text(text, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                    }
                }
            }

            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("89g • ${product.category}", fontSize = 10.sp, color = TextMuted,
                            modifier = Modifier.padding(top = 2.dp))
                    }
                    // Checkbox
                    Box(
                        modifier = Modifier.size(20.dp)
                            .border(1.5.dp, if (inCart) Primary else BorderStrong, RoundedCornerShape(4.dp))
                            .background(if (inCart) PrimaryLight else Color.Transparent, RoundedCornerShape(4.dp))
                            .clickable { if (!inCart) onAddToCart() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (inCart) Icon(Icons.Rounded.Check, null, tint = Primary, modifier = Modifier.size(12.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text(product.formattedPriceOld(), fontSize = 9.sp, color = Color(0xFFCBD5E1),
                            style = LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough))
                        Text(product.formattedPrice(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(24.dp)
                                    .border(1.dp, BorderStrong, RoundedCornerShape(topStart = 7.dp, bottomStart = 7.dp))
                                    .background(Gray100, RoundedCornerShape(topStart = 7.dp, bottomStart = 7.dp))
                                    .clickable { onDecPlanned() },
                                contentAlignment = Alignment.Center
                            ) { Text("−", fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Normal) }

                            Box(
                                Modifier.width(32.dp).height(24.dp).border(BorderStroke(1.dp, BorderStrong)),
                                contentAlignment = Alignment.Center
                            ) { Text("$plannedQty", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary) }

                            Box(
                                Modifier.size(24.dp)
                                    .background(Primary, RoundedCornerShape(topEnd = 7.dp, bottomEnd = 7.dp))
                                    .clickable { onIncPlanned() },
                                contentAlignment = Alignment.Center
                            ) { Text("+", fontSize = 14.sp, color = White, fontWeight = FontWeight.Bold) }
                        }

                        Spacer(Modifier.height(4.dp))

                        if (inCart) {
                            Text(
                                text = "${AppState.t().itemsInCart}: $inCartQty",
                                fontSize = 10.sp,
                                color = SuccessGreen,
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryLight.copy(0.3f),
                                border = BorderStroke(1.dp, Primary.copy(0.2f)),
                                modifier = Modifier.clickable { onAddToCart() }
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(AppState.t().add, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                                    Icon(Icons.Rounded.Add, null, tint = Primary, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Double.asTenge() = "\$${String.format("%.2f", this)}"

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
