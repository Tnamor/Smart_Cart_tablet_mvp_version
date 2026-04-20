package com.smartcart.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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

private fun Double.asPrice(): String = com.smartcart.data.CurrencyConfig.format(this)

@Composable
fun SharedSidebar(
    activeRoute: String = "home",
    onNavigate: (String) -> Unit = {}
) {
    val items = listOf(
        Triple("home",  Icons.Outlined.Home,        AppState.t().home),
        Triple("list",  Icons.Outlined.List,        AppState.t().myShoppingList),
        Triple("cats",  Icons.Outlined.GridView,    AppState.t().categories),
        Triple("favs",  Icons.Outlined.FavoriteBorder,    AppState.t().favorites),
        Triple("support",  Icons.Outlined.HeadsetMic,     AppState.t().navSupport),
    )

    Column(
        modifier = Modifier
            .width(72.dp)
            .fillMaxHeight()
            .background(White)
            .border(BorderStroke(1.dp, Border)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.size(42.dp).background(Primary, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.ShoppingCart, null, tint = White, modifier = Modifier.size(22.dp))
        }

        Spacer(Modifier.height(24.dp))

        items.forEach { (route, icon, label) ->
            SideNavItem(
                icon = icon, label = label,
                isActive = activeRoute == route,
                onClick = { onNavigate(route) }
            )
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.weight(1f))
        
        IconButton(onClick = { /* Settings */ }) {
            Icon(Icons.Outlined.Settings, null, tint = TextMuted)
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
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isActive) PrimaryLight else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    label,
                    tint = if (isActive) Primary else TextMuted,
                    modifier = Modifier.size(24.dp)
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
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

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
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Smart Cart", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            
            Spacer(Modifier.width(32.dp))
            
            // Search Bar - Fixed alignment using BasicTextField for perfect centering
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(Gray100, RoundedCornerShape(12.dp)),
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = TextPrimary),
                cursorBrush = SolidColor(Primary),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Search, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(AppState.t().searchPlaceholder, fontSize = 14.sp, color = TextMuted)
                            }
                            innerTextField()
                        }
                    }
                }
            )

            Spacer(Modifier.weight(0.5f))
            
            LanguageSwitcher()

            IconButton(onClick = {}) {
                Icon(Icons.Outlined.Notifications, null, tint = TextMuted, modifier = Modifier.size(20.dp))
            }
            
            Box(
                Modifier
                    .size(36.dp)
                    .background(Color(0xFFF97316), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(AppState.currentUser?.name?.take(1) ?: "A", color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CartPanel(onCheckout: () -> Unit) {
    val t = AppState.t()
    val lang = AppState.language
    val cart = AppState.cart
    val subtotal = AppState.cartTotal
    val tax = AppState.cartTax
    val discount = AppState.cartDiscount
    val total = (subtotal + tax - discount).coerceAtLeast(0.0)
    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf(((AppState.budgetTenge ?: 0.0).toInt()).toString()) }
    val budget = AppState.budgetTenge

    Surface(modifier = Modifier.width(300.dp).fillMaxHeight(), color = White, shadowElevation = 0.dp, border = BorderStroke(1.dp, Border)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text(t.yourCart, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(t.scanItems, fontSize = 12.sp, color = TextSecondary)
                }
                Box(modifier = Modifier.size(32.dp).background(Primary, CircleShape), contentAlignment = Alignment.Center) {
                    Text("${cart.sumOf { it.quantity }}", color = White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            HorizontalDivider(color = Border, thickness = 1.dp)

            Box(Modifier.padding(vertical = 12.dp)) {
                if (budget == null) {
                    OutlinedButton(
                        onClick = { showBudgetDialog = true },
                        border = BorderStroke(1.dp, Primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(40.dp)
                    ) {
                        Icon(Icons.Outlined.AccountBalanceWallet, null, tint = Primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(t.setBudget, color = Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    BudgetTracker(cartTotalTenge = total, budgetTenge = budget, onSetBudget = {
                        showBudgetDialog = true
                    })
                }
            }
            
            HorizontalDivider(color = Border, thickness = 1.dp)

            LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (cart.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Rounded.ShoppingCartCheckout, null, tint = Gray200, modifier = Modifier.size(56.dp))
                                Spacer(Modifier.height(12.dp))
                                Text(t.emptyWishlist, fontSize = 14.sp, color = TextMuted)
                            }
                        }
                    }
                } else {
                    items(cart, key = { it.product.id }) { item ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current).data(item.product.imageUrl).crossfade(true).build(),
                                placeholder = painterResource(R.drawable.product_placeholder),
                                error = painterResource(R.drawable.product_placeholder),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Gray100),
                                contentScale = ContentScale.Crop
                            )
                            Column(Modifier.weight(1f)) {
                                Text(item.product.localizedName(lang), fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${item.quantity} × ", fontSize = 12.sp, color = TextSecondary)
                                    Text(item.product.formattedPrice(), fontSize = 12.sp, color = Primary, fontWeight = FontWeight.Medium)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(item.product.formattedPrice(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Box(Modifier.size(24.dp).clip(CircleShape).clickable { AppState.removeFromCart(item.product.id) }.background(Gray100),
                                    contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.Close, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
            
            HorizontalDivider(color = Border, thickness = 1.dp)

            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryLight.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.List, null, tint = Primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "${t.plannedList}: ${AppState.shoppingList.sumOf { it.plannedQuantity }}", 
                        color = Primary, fontSize = 13.sp, fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(Modifier.background(Gray100.copy(alpha = 0.5f)).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CartTRow(t.subtotal, subtotal.asPrice())
                CartTRow(t.tax, tax.asPrice())
                CartTRow(t.discounts, "-${discount.asPrice()}", SuccessGreen)
                
                Spacer(Modifier.height(4.dp))
                HorizontalDivider(color = BorderStrong)
                Spacer(Modifier.height(4.dp))
                
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text(t.total, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text(total.asPrice(), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
                }
                
                Spacer(Modifier.height(12.dp))
                
                Button(onClick = onCheckout, Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = cart.isNotEmpty()) {
                    Text(t.checkoutNow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Rounded.ArrowForward, null, Modifier.size(20.dp))
                }
            }
        }
    }

    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text(t.setBudget) },
            text = {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Tenge (₸)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(onClick = {
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
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, color = valueColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BudgetTracker(cartTotalTenge: Double, budgetTenge: Double, onSetBudget: () -> Unit) {
    val progress = (cartTotalTenge / budgetTenge).toFloat().coerceIn(0f, 1.5f)
    val barColor = when {
        progress < 0.7f -> SuccessGreen
        progress < 0.9f -> AccentOrange
        else -> ErrorRed
    }
    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(AppState.t().setBudget, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("${cartTotalTenge.toInt()} / ${budgetTenge.toInt()} ₸", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Primary)
        }
        LinearProgressIndicator(
            progress = { progress.coerceAtMost(1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = Gray100
        )
        if (progress > 1f) {
            Text("Budget exceeded!", color = ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
        TextButton(onClick = onSetBudget, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(24.dp)) {
            Text("Edit budget", fontSize = 12.sp, color = Primary)
        }
    }
}
