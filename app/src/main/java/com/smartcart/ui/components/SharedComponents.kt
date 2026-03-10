package com.smartcart.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.smartcart.R
import com.smartcart.data.repository.AppState
import com.smartcart.ui.theme.*
import kotlin.math.absoluteValue

private fun Double.asTenge() = "${(this * 130).toInt()}₸"

// ── Sidebar ────────────────────────────────────────────────────
@Composable
fun SharedSidebar(
    activeRoute: String = "home",
    onNavigate: (String) -> Unit = {}
) {
    val items = listOf(
        Triple("home",  Icons.Outlined.Home,        stringResource(R.string.nav_home)),
        Triple("list",  Icons.Outlined.LocalOffer,  stringResource(R.string.nav_deals)),
        Triple("cats",  Icons.Outlined.GridView,    stringResource(R.string.nav_categories)),
        Triple("favs",  Icons.Outlined.Bookmark,    stringResource(R.string.nav_favorites)),
    )

    Column(
        modifier = Modifier
            .width(64.dp)
            .fillMaxHeight()
            .background(White)
            .border(BorderStroke(1.dp, Border)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // Logo
        Box(modifier = Modifier.size(36.dp).background(Primary, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.ShoppingCart, null, tint = White, modifier = Modifier.size(18.dp))
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
            .width(64.dp)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isActive) PrimaryLight else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    label,
                    tint = if (isActive) Primary else TextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = if (isActive) Primary else TextMuted,
                modifier = Modifier.width(60.dp)
            )
        }
    }
}

// ── TopBar ─────────────────────────────────────────────────────
@Composable
fun SharedTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = White, shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Smart Cart", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.weight(1f))
            LanguageSwitcher()

            Icon(Icons.Outlined.Wifi, null, tint = TextMuted, modifier = Modifier.size(18.dp))
            Icon(Icons.Outlined.Chat, null, tint = TextMuted, modifier = Modifier.size(18.dp))
            Box(
                Modifier
                    .size(32.dp)
                    .background(Color(0xFFF97316), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("JD", color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
    val suggestedProducts = remember(cart) { AppState.products.filterNot { p -> cart.any { it.product.id == p.id } }.take(3) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf(((AppState.budgetTenge ?: 0.0).toInt()).toString()) }
    val budget = AppState.budgetTenge

    Surface(modifier = Modifier.width(280.dp).fillMaxHeight(), color = White, shadowElevation = 0.dp, border = BorderStroke(1.dp, Border)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text(AppState.t().yourCart, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(AppState.t().scanItems, fontSize = 12.sp, color = TextSecondary)
                }
                Box(modifier = Modifier.size(28.dp).background(Primary, CircleShape), contentAlignment = Alignment.Center) {
                    Text("${cart.sumOf { it.quantity }}", color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            if (budget == null) {
                OutlinedButton(
                    onClick = { showBudgetDialog = true },
                    border = BorderStroke(1.dp, Primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(36.dp)
                ) {
                    Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.set_budget), color = Primary, fontSize = 13.sp)
                }
            } else {
                BudgetTracker(cartTotalTenge = total * 130.0, budgetTenge = budget, onSetBudget = {
                    AppState.budgetTenge = it
                })
            }
            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (cart.isEmpty()) {
                    item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.ShoppingCartCheckout, null, tint = Color(0xFFDDE1EA), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(AppState.t().emptyWishlist, fontSize = 13.sp, color = TextMuted)
                        }
                    }
                    }
                } else {
                    items(cart, key = { it.product.id }) { item ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current).data(item.product.imageUrl).crossfade(true).build(),
                                placeholder = painterResource(R.drawable.product_placeholder),
                                error = painterResource(R.drawable.product_placeholder),
                                contentDescription = null,
                                modifier = Modifier.size(42.dp).clip(CircleShape).background(Gray100),
                                contentScale = ContentScale.Crop
                            )
                            Column(Modifier.weight(1f)) {
                                Text(item.product.localizedName(lang), fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${item.quantity} × ${item.product.formattedPrice()}", fontSize = 11.sp, color = TextSecondary)
                                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFECFDF5)) {
                                    Text("AI: ${90 + (item.product.id.hashCode().absoluteValue % 9)}%",
                                        fontSize = 10.sp,
                                        color = SuccessGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
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
            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF5F3FF))
                    .clickable { }
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.List, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Your planned list: ${AppState.shoppingList.sumOf { it.plannedQuantity }} items", color = Primary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
            Column(Modifier.background(Background).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CartTRow("Subtotal", subtotal.asTenge())
                CartTRow("Tax (12%)", (subtotal * 0.12).asTenge())
                CartTRow("Discount", "-${discount.asTenge()}", SuccessGreen)
                HorizontalDivider(Modifier.padding(vertical = 4.dp), color = BorderStrong)
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(AppState.t().total, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(total.asTenge(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
            Box(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Button(onClick = onCheckout, Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = cart.isNotEmpty()) {
                    Text(AppState.t().checkoutNow, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Rounded.ArrowForward, null, Modifier.size(18.dp))
                }
            }
        }
    }

    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text(stringResource(R.string.set_budget)) },
            text = {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it.filter { ch -> ch.isDigit() } },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    budgetInput.toDoubleOrNull()?.let { AppState.budgetTenge = it }
                    showBudgetDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CartTRow(label: String, value: String, valueColor: Color = TextSecondary) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = TextMuted)
        Text(value, fontSize = 12.sp, color = valueColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BudgetTracker(cartTotalTenge: Double, budgetTenge: Double, onSetBudget: (Double) -> Unit) {
    val progress = (cartTotalTenge / budgetTenge).toFloat().coerceIn(0f, 1.5f)
    val barColor = when {
        progress < 0.7f -> SuccessGreen
        progress < 0.9f -> AccentOrange
        else -> ErrorRed
    }
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Budget", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text("${budgetTenge.toInt()}₸", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
        }
        LinearProgressIndicator(
            progress = { progress.coerceAtMost(1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = Gray100
        )
        OutlinedButton(
            onClick = { onSetBudget(budgetTenge) },
            border = BorderStroke(1.dp, Primary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(36.dp)
        ) {
            Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.set_budget), color = Primary, fontSize = 13.sp)
        }
    }
}
