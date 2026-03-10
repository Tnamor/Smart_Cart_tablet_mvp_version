package com.smartcart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import coil3.compose.AsyncImage
import com.smartcart.data.model.AppStrings
import com.smartcart.data.model.CartItem
import com.smartcart.ui.components.rememberQrBitmap
import com.smartcart.data.repository.AppState
import com.smartcart.ui.theme.Background
import com.smartcart.ui.theme.Border
import com.smartcart.ui.theme.BorderStrong
import com.smartcart.ui.theme.Primary
import com.smartcart.ui.theme.PrimaryLight
import com.smartcart.ui.theme.SuccessGreen
import com.smartcart.ui.theme.TextMuted
import com.smartcart.ui.theme.TextPrimary
import com.smartcart.ui.theme.TextSecondary
import com.smartcart.ui.theme.White
import kotlin.math.absoluteValue

private enum class PaymentType { CARD, KASPI }

private data class SavedCard(val id: String, val bankName: String, val type: String, val lastFour: String)

@Composable
fun CartScreen(
    onNavigateToList: () -> Unit,
    onNavigateToReceipt: () -> Unit,
) {
    val cart = AppState.cart.toList()
    val subtotal = AppState.cartTotal * 130.0
    val vat = subtotal * 0.12
    val discount = (AppState.cartDiscount * 130.0).coerceAtLeast(500.0)
    val total = (subtotal + vat - discount).coerceAtLeast(0.0)
    val savedCards = remember {
        listOf(
            SavedCard("kaspi", "Kaspi Bank", "Visa", "4242"),
            SavedCard("halyk", "Halyk Bank", "Mastercard", "8888"),
        )
    }
    var selectedPayment by remember { mutableStateOf(PaymentType.CARD) }
    var selectedCard by remember { mutableStateOf(savedCards.first().id) }

    Row(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.weight(0.55f).fillMaxSize().padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateToList) { Icon(Icons.Rounded.ArrowBack, null, tint = TextPrimary) }
                Text("Checkout", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
            Spacer(Modifier.height(20.dp))
            Text(AppState.t().orderSummary, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Spacer(Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cart) { item -> CheckoutItemRow(item) }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Border)
            PriceRow(AppState.t().subtotal, subtotal.asTenge())
            PriceRow(AppState.t().vat, vat.asTenge())
            PriceRow(AppState.t().memberDiscount, "-${discount.asTenge()}", valueColor = Primary, labelColor = Primary)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Border)
            Row {
                Text(AppState.t().total, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.weight(1f))
                Text(total.asTenge(), fontWeight = FontWeight.Bold, fontSize = 28.sp)
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF7ED)).padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("You'll earn", fontSize = 13.sp, color = TextSecondary)
                        Text("27 Loyalty Points", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFF59E0B))
                    }
                }
            }
        }

        Column(modifier = Modifier.weight(0.45f).fillMaxSize().background(White).padding(24.dp)) {
            Text(AppState.t().paymentMethod, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF3F4F6)).padding(4.dp)
            ) {
                PaymentTab(Icons.Outlined.CreditCard, AppState.t().creditCard, selectedPayment == PaymentType.CARD) { selectedPayment = PaymentType.CARD }
                PaymentTab(Icons.Outlined.QrCode, "Kaspi QR", selectedPayment == PaymentType.KASPI) { selectedPayment = PaymentType.KASPI }
            }
            Spacer(Modifier.height(20.dp))
            when (selectedPayment) {
                PaymentType.KASPI -> KaspiQrSection(total = total, t = AppState.t())
                PaymentType.CARD -> {
                    Text("Your saved cards", fontSize = 14.sp, color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    savedCards.forEach { card ->
                        SavedCardRow(card = card, isSelected = selectedCard == card.id, onSelect = { selectedCard = card.id })
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onNavigateToReceipt,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Complete Purchase", color = White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Rounded.ArrowForward, null, tint = White)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "By completing you agree to our Terms of Service",
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.SupportAgent, null, tint = Primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Call Assistant", color = Primary)
            }
        }
    }
}

@Composable
private fun CheckoutItemRow(item: CartItem) {
    val confidence = 90 + (item.product.id.hashCode().absoluteValue % 9)
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = item.product.imageUrl,
            contentDescription = item.product.nameEn,
            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.localizedName(AppState.language), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text("Qty: ${item.quantity} × ${item.product.formattedPrice()}", fontSize = 13.sp, color = TextSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text((item.lineTotal * 130.0).asTenge(), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFFECFDF5)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text("AI: $confidence%", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Medium)
            }
        }
    }
    HorizontalDivider(color = Color(0xFFF3F4F6))
}

@Composable
private fun KaspiQrSection(total: Double, t: AppStrings) {
    val amountTiyn = (total * 100).toInt()
    val qrContent = "kaspi://pay?amount=$amountTiyn&currency=KZT&ref=smartcart"
    val qrBitmap = rememberQrBitmap(qrContent)
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(White).border(1.dp, Border, RoundedCornerShape(12.dp)).padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                bitmap = qrBitmap,
                contentDescription = "Kaspi QR",
                modifier = Modifier.size(200.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(t.kaspiQrScan, fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("${total.toInt()} ₸", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Primary)
        }
    }
}

@Composable
private fun RowScope.PaymentTab(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (isSelected) Primary else Color.Transparent).clickable { onClick() }.padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = if (isSelected) White else TextSecondary, modifier = Modifier.size(16.dp))
            Text(label, fontSize = 12.sp, color = if (isSelected) White else TextSecondary)
        }
    }
}

@Composable
private fun SavedCardRow(card: SavedCard, isSelected: Boolean, onSelect: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(12.dp)).border(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Primary else BorderStrong,
            shape = RoundedCornerShape(12.dp)
        ).background(if (isSelected) Color(0xFFF5F3FF) else White).clickable { onSelect() }.padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CreditCard, null, tint = Primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(card.bankName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text("${card.type} •••• ${card.lastFour}", fontSize = 13.sp, color = TextSecondary)
            }
            if (isSelected) Icon(Icons.Rounded.CheckCircle, null, tint = Primary, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String, valueColor: Color = TextPrimary, labelColor: Color = TextSecondary) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = labelColor)
        Text(value, color = valueColor, fontWeight = FontWeight.SemiBold)
    }
}

private fun Double.asTenge(): String = "${toInt()} ₸"

@Preview(showBackground = true, device = "spec:width=1920dp,height=1104dp,dpi=160")
@Composable
private fun Preview() {
    CartScreen(onNavigateToList = {}, onNavigateToReceipt = {})
}
