package com.smartcart.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.smartcart.data.repository.AppState
import com.smartcart.ui.theme.*

private fun Double.asTenge() = "${(this * 130).toInt()}₸"

// ── Sidebar ────────────────────────────────────────────────────
@Composable
fun SharedSidebar(
    activeRoute: String = "home",
    onNavigate: (String) -> Unit = {}
) {
    val t = AppState.t()
    val items = listOf(
        Triple("home",  Icons.Rounded.Home,            t.home),
        Triple("list",  Icons.Rounded.ShoppingBag,     t.myShoppingList),
        Triple("cats",  Icons.Rounded.GridView,        t.navCats),
        Triple("favs",  Icons.Rounded.FavoriteBorder,  t.navSaved),
    )

    Column(
        modifier = Modifier
            .width(68.dp)
            .fillMaxHeight()
            .background(White)
            .border(BorderStroke(1.dp, Border)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // Logo
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(Primary, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.ShoppingCart, null, tint = White, modifier = Modifier.size(20.dp))
        }

        Spacer(Modifier.height(20.dp))

        items.forEach { (route, icon, label) ->
            SideNavItem(
                icon = icon, label = label,
                isActive = activeRoute == route,
                onClick = { onNavigate(route) }
            )
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.weight(1f))

        // Avatar
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(PrimaryLight, CircleShape)
                .border(2.dp, Purple300, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("A", fontWeight = FontWeight.Bold, color = Primary, fontSize = 13.sp)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SideNavItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
            .background(if (isActive) PrimaryLight else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(3.dp).height(28.dp)
                    .background(Primary, RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, label,
                tint = if (isActive) Primary else TextMuted,
                modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 8.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) Primary else TextMuted)
        }
    }
}

// ── TopBar ─────────────────────────────────────────────────────
@Composable
fun SharedTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
) {
    val t = AppState.t()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = White, shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Smart Cart", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

            // Search pill (interactive)
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp),
                color = Background,
                border = BorderStroke(1.dp, BorderStrong)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Search, null, tint = TextMuted, modifier = Modifier.size(15.dp))
                    androidx.compose.material3.TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(t.searchPlaceholder, fontSize = 12.sp, color = TextMuted) },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = Primary
                        )
                    )
                    Icon(Icons.Rounded.Mic, null, tint = TextMuted, modifier = Modifier.size(15.dp))
                }
            }

            LanguageSwitcher()

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Wifi, null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                Text("50", fontSize = 10.sp, color = TextSecondary)
                Icon(Icons.Rounded.BatteryFull, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Text("84%", fontSize = 10.sp, color = TextSecondary)
            }

            Box {
                Icon(Icons.Rounded.NotificationsNone, null, tint = TextSecondary, modifier = Modifier.size(22.dp))
                Box(Modifier.size(6.dp).background(ErrorRed, CircleShape).align(Alignment.TopEnd))
            }
        }
    }
}

// ── Shared Cart Panel (identical on all screens) ─────────────────────────────────
@Composable
fun CartPanel(onCheckout: () -> Unit) {
    val t = AppState.t()
    val lang = AppState.language
    val cart = AppState.cart
    val subtotal = AppState.cartTotal
    val tax = AppState.cartTax
    val discount = AppState.cartDiscount
    val total = (subtotal + tax - discount).coerceAtLeast(0.0)

    Surface(modifier = Modifier.width(300.dp).fillMaxHeight(), color = White, shadowElevation = 4.dp) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text(t.yourCart, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(t.scanItems, fontSize = 10.sp, color = TextMuted)
                }
                Surface(shape = RoundedCornerShape(20.dp), color = PrimaryLight) {
                    Text("${cart.sumOf { it.quantity }}${t.itemsSuffix}", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = PrimaryDark, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
            HorizontalDivider(color = Border)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (cart.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.ShoppingCartCheckout, null, tint = Color(0xFFDDE1EA), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Cart is empty", fontSize = 13.sp, color = TextMuted)
                        }
                    }
                } else {
                    cart.forEach { item ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AsyncImage(model = item.product.imageUrl, contentDescription = null,
                                modifier = Modifier.size(42.dp).clip(CircleShape).background(Gray100),
                                contentScale = ContentScale.Crop)
                            Column(Modifier.weight(1f)) {
                                Text(item.product.localizedName(lang), fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${item.quantity} × ${item.product.formattedPrice()}", fontSize = 11.sp, color = TextSecondary)
                            }
                            Text(item.product.formattedPrice(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Box(Modifier.size(22.dp).background(Gray100, CircleShape).clickable { AppState.removeFromCart(item.product.id) },
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Close, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }
            Column(Modifier.background(Background).padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CartTRow(t.subtotal, subtotal.asTenge())
                CartTRow(t.tax, tax.asTenge())
                CartTRow(t.discounts, "-${discount.asTenge()}", SuccessGreen)
                HorizontalDivider(Modifier.padding(vertical = 4.dp), color = BorderStrong)
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(t.total, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(total.asTenge(), fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                }
            }
            Box(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Button(onClick = onCheckout, Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = cart.isNotEmpty()) {
                    Text("${t.checkout} →", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Rounded.ArrowForward, null, Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun CartTRow(label: String, value: String, valueColor: Color = TextSecondary) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = TextMuted)
        Text(value, fontSize = 12.sp, color = valueColor, fontWeight = FontWeight.Medium)
    }
}
