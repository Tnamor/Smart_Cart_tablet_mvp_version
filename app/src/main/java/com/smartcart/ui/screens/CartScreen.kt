package com.smartcart.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.smartcart.data.model.CartItem
import com.smartcart.data.repository.AppState
import com.smartcart.presentation.cart.CartViewModel
import com.smartcart.ui.theme.*

@Composable
fun CartScreen(
    onNavigateToList: () -> Unit,
    onNavigateToReceipt: () -> Unit,
) {
    val t    = AppState.t()
    val lang = AppState.language
    val cart = AppState.cart
    val subtotal = AppState.cartTotal
    val vat      = AppState.cartVat
    val discount = AppState.cartDiscount
    val total    = (subtotal + vat - discount).coerceAtLeast(0.0)

    var payment by remember { mutableStateOf("card") }
    val viewModel: CartViewModel = hiltViewModel()

    Column(Modifier.fillMaxSize().background(Background)) {

        // Top Bar (matches CartScreen.tsx h-16 header)
        Surface(Modifier.fillMaxWidth(), color = White, shadowElevation = 1.dp) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconButton(onClick = onNavigateToList, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = TextSecondary)
                    }
                    Box(Modifier.size(32.dp).background(Primary, RoundedCornerShape(9.dp)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.ShoppingCart, null, tint = White, modifier = Modifier.size(17.dp))
                    }
                    Text(t.autoCart, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    // ● Cart Online
                    Surface(shape = RoundedCornerShape(20.dp), color = SuccessGreen.copy(0.1f),
                        border = BorderStroke(1.dp, SuccessGreen.copy(0.2f))) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(Modifier.size(7.dp).background(SuccessGreen, CircleShape))
                            Text(t.cartOnline, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                color = SuccessGreen, letterSpacing = 0.5.sp)
                        }
                    }
                    // Call Assistant
                    Row(Modifier.clickable {}.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.HeadsetMic, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        Text(t.callAssistant, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                    }
                }
            }
        }

        Row(Modifier.weight(1f).padding(24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {

            // ── Left Panel ────────────────────────────────
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)) {

                // Items card
                Surface(Modifier.weight(1f).fillMaxWidth(), RoundedCornerShape(20.dp), White,
                    shadowElevation = 1.dp, border = BorderStroke(1.dp, Border)) {
                    Column(Modifier.fillMaxSize()) {
                        Row(Modifier.fillMaxWidth().background(Gray50).padding(horizontal = 20.dp, vertical = 14.dp)) {
                            Text("${t.yourItems} (${cart.sumOf { it.quantity }})",
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary,
                                letterSpacing = 0.5.sp)
                        }
                        HorizontalDivider(color = Border)

                        if (cart.isEmpty()) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Rounded.ShoppingCartCheckout, null, tint = Color(0xFFDDE1EA), modifier = Modifier.size(52.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("Cart is empty", fontSize = 15.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                                }
                            }
                        } else {
                            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(8.dp)) {
                                cart.forEachIndexed { i, item ->
                                    CartItemRow(item, lang.let { AppState.language },
                                        onInc    = { AppState.updateCartQty(item.product.id, +1) },
                                        onDec    = { AppState.updateCartQty(item.product.id, -1) },
                                        onRemove = { AppState.removeFromCart(item.product.id) })
                                    if (i < cart.size - 1) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Border)
                                }
                            }
                        }
                    }
                }

                // Payment methods
                Column {
                    Text(t.paymentMethod, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = TextSecondary, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 10.dp, start = 2.dp))
                    Row(Modifier.fillMaxWidth().height(80.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        PayCard(Icons.Rounded.CreditCard, t.creditCard, payment == "card", Modifier.weight(1f)) { payment = "card" }
                        PayCard(Icons.Rounded.Money,      t.cash,       payment == "cash", Modifier.weight(1f)) { payment = "cash" }
                        PayCard(Icons.Rounded.Nfc,        t.tapToPay,   payment == "nfc",  Modifier.weight(1f)) { payment = "nfc" }
                    }
                }
            }

            // ── Right Panel ───────────────────────────────
            Column(Modifier.width(340.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Order Summary card
                Surface(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), White,
                    shadowElevation = 1.dp, border = BorderStroke(1.dp, Border)) {
                    Column(Modifier.padding(24.dp)) {
                        Text(t.orderSummary, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        HorizontalDivider(Modifier.padding(vertical = 14.dp), color = Border)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SRow(t.subtotal, subtotal.asTenge())
                            SRow(t.vat,      vat.asTenge())
                            SRow(t.memberDiscount, "-${discount.asTenge()}", SuccessGreen, SuccessGreen)
                        }
                        HorizontalDivider(Modifier.padding(vertical = 14.dp), color = Border)
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) {
                            Text(t.total, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Text(total.asTenge(), fontSize = 30.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                        }
                    }
                }

                // Points banner
                Surface(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp),
                    PrimaryLight.copy(0.3f), border = BorderStroke(1.dp, Primary.copy(0.2f))) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(32.dp).background(Primary, CircleShape), Alignment.Center) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = White, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = buildAnnotatedString {
                                append("You will earn ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Primary)) { append("24 points") }
                                append(" with this purchase.")
                            },
                            fontSize = 13.sp, color = PrimaryDark
                        )
                    }
                }

                // Complete Purchase
                Button(onClick = {
                    viewModel.completePurchase {
                        onNavigateToReceipt()
                    }
                }, Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = cart.isNotEmpty()) {
                    Text(t.completePurchase, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Rounded.ArrowForward, null, modifier = Modifier.size(18.dp))
                }

                Text(t.terms, fontSize = 11.sp, color = TextMuted, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem, lang: com.smartcart.data.model.AppLanguage,
    onInc: () -> Unit, onDec: () -> Unit, onRemove: () -> Unit
) {
    val name = item.product.localizedName(lang)
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        AsyncImage(model = item.product.imageUrl, contentDescription = name,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(Gray100),
            contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f)) {
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${item.product.formattedPrice()} / ${item.product.category}",
                fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
        }
        // Stepper
        Surface(shape = RoundedCornerShape(9.dp), color = White, border = BorderStroke(1.dp, Border),
            shadowElevation = 1.dp) {
            Row(Modifier.height(36.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(36.dp).fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 9.dp, bottomStart = 9.dp))
                    .clickable { onDec() }
                    .background(Gray50),
                    contentAlignment = Alignment.Center) {
                    Text("−", fontSize = 18.sp, color = TextSecondary)
                }
                Box(Modifier.width(40.dp).fillMaxHeight().background(Gray50.copy(0.5f)),
                    contentAlignment = Alignment.Center) {
                    Text("${item.quantity}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Box(Modifier.width(36.dp).fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 9.dp, bottomEnd = 9.dp))
                    .clickable { onInc() }
                    .background(Gray50),
                    contentAlignment = Alignment.Center) {
                    Text("+", fontSize = 18.sp, color = TextSecondary)
                }
            }
        }
        Text(item.product.formattedPrice(), fontSize = 15.sp, fontWeight = FontWeight.Bold,
            color = TextPrimary, modifier = Modifier.width(70.dp), textAlign = TextAlign.End)
        Box(Modifier.size(30.dp).clip(CircleShape).clickable { onRemove() }
            .background(Color.Transparent), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.DeleteOutline, null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PayCard(icon: ImageVector, label: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier.clickable { onClick() }, RoundedCornerShape(12.dp),
        color = if (active) PrimaryLight.copy(0.3f) else White,
        border = BorderStroke(if (active) 2.dp else 1.dp, if (active) Primary else BorderStrong)
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, label, tint = if (active) Primary else TextMuted, modifier = Modifier.size(24.dp))
                Text(label, fontSize = 12.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (active) Primary else TextSecondary)
            }
            if (active) {
                Box(Modifier.size(20.dp).align(Alignment.TopEnd).offset((-8).dp, 8.dp)
                    .background(Primary, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Check, null, tint = White, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
private fun SRow(label: String, value: String,
    labelColor: Color = TextSecondary, valueColor: Color = TextPrimary) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = labelColor)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

private fun Double.asTenge() = "${(this * 130).toInt()}₸"

@Preview(showBackground = true, device = "spec:width=1920dp,height=1104dp,dpi=160")
@Composable
private fun Preview() {
    SmartCartTheme {
        CartScreen(onNavigateToList = {}, onNavigateToReceipt = {})
    }
}
