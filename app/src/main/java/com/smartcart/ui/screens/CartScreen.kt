package com.smartcart.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import coil3.compose.AsyncImage
import com.smartcart.data.model.AppStrings
import com.smartcart.data.model.CartItem
import com.smartcart.ui.components.rememberQrBitmap
import com.smartcart.data.repository.AppState
import com.smartcart.ui.theme.*
import kotlin.math.absoluteValue
import kotlinx.coroutines.delay

private enum class PaymentType { CARD, KASPI }

private data class SavedCard(val id: String, val bankName: String, val type: String, val lastFour: String)

@Composable
fun CartScreen(
    onNavigateToList: () -> Unit,
    onNavigateToReceipt: () -> Unit,
) {
    val cart = AppState.cart.toList()
    // Базовые расчеты (в "USD"), конвертация будет внутри asPrice() -> format(Double)
    val subtotal = AppState.cartTotal
    val vat = subtotal * 0.12
    val discount = AppState.cartDiscount
    val total = (subtotal + vat - discount).coerceAtLeast(0.0)
    
    val savedCards = remember {
        listOf(
            SavedCard("kaspi", "Kaspi Bank", "Visa", "4242"),
            SavedCard("halyk", "Halyk Bank", "Mastercard", "8888"),
        )
    }
    var selectedPayment by remember { mutableStateOf(PaymentType.KASPI) }
    var selectedCard by remember { mutableStateOf(savedCards.first().id) }

    Row(modifier = Modifier.fillMaxSize().background(Background)) {
        // Left Panel: Order Summary
        Column(modifier = Modifier.weight(0.55f).fillMaxSize().padding(32.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateToList) { Icon(Icons.Rounded.ArrowBack, null, tint = TextPrimary) }
                Spacer(Modifier.width(8.dp))
                Text(AppState.t().checkout, fontWeight = FontWeight.Black, fontSize = 28.sp, color = TextPrimary)
            }
            
            Spacer(Modifier.height(24.dp))
            Text(AppState.t().orderSummary, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = White,
                border = BorderStroke(1.dp, Border)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(cart) { item -> CheckoutItemRow(item) }
                }
            }

            Spacer(Modifier.height(24.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PriceRow(AppState.t().subtotal, subtotal.asPrice())
                PriceRow(AppState.t().vat, vat.asPrice())
                PriceRow(AppState.t().discounts, "-${discount.asPrice()}", valueColor = SuccessGreen)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderStrong)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(AppState.t().total, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = TextPrimary)
                    Spacer(Modifier.weight(1f))
                    Text(total.asPrice(), fontWeight = FontWeight.Black, fontSize = 32.sp, color = Primary)
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFFFF7ED)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Star, null, tint = AccentOrange, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Бонусы за покупку", fontSize = 13.sp, color = TextSecondary)
                        Text("27 Loyalty Points", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AccentOrange)
                    }
                }
            }
        }

        // Right Panel: Payment Method
        Column(modifier = Modifier.weight(0.45f).fillMaxSize().background(White).padding(32.dp)) {
            Text(AppState.t().paymentMethod, fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextPrimary)
            Spacer(Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Gray100).padding(4.dp)
            ) {
                PaymentTab(Icons.Outlined.QrCode, "Kaspi QR", selectedPayment == PaymentType.KASPI) { selectedPayment = PaymentType.KASPI }
                PaymentTab(Icons.Outlined.CreditCard, AppState.t().creditCard, selectedPayment == PaymentType.CARD) { selectedPayment = PaymentType.CARD }
            }
            
            Spacer(Modifier.height(32.dp))
            
            when (selectedPayment) {
                PaymentType.KASPI -> KaspiQrSection(total = total, t = AppState.t())
                PaymentType.CARD -> {
                    Text("Ваши карты", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(16.dp))
                    savedCards.forEach { card ->
                        SavedCardRow(card = card, isSelected = selectedCard == card.id, onSelect = { selectedCard = card.id })
                    }
                }
            }
            
            Spacer(Modifier.weight(1f))
            
            Button(
                onClick = onNavigateToReceipt,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = cart.isNotEmpty()
            ) {
                Text(AppState.t().completePurchase, color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.width(10.dp))
                Icon(Icons.Rounded.ArrowForward, null, tint = White)
            }
            
            Spacer(Modifier.height(12.dp))
            Text(
                AppState.t().terms,
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.SupportAgent, null, tint = Primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(AppState.t().callAssistant, color = Primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CheckoutItemRow(item: CartItem) {
    val confidence = 90 + (item.product.id.hashCode().absoluteValue % 9)
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.product.imageUrl,
                contentDescription = item.product.nameEn,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(Gray100),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.localizedName(AppState.language), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text("Кол-во: ${item.quantity} × ${item.product.formattedPrice()}", fontSize = 13.sp, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(item.lineTotal.asPrice(), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextPrimary)
                Surface(color = SuccessGreen.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                    Text("AI: $confidence%", fontSize = 10.sp, color = SuccessGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
        HorizontalDivider(color = Gray100)
    }
}

@Composable
private fun KaspiQrSection(total: Double, t: AppStrings) {
    var secondsLeft by remember { mutableIntStateOf(30) }
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    // Генерируем данные для QR в тенге (целые числа для Kaspi)
    val totalTenge = (total * 130).toInt()
    val amountTiyn = totalTenge * 100
    val qrContent = "kaspi://pay?amount=$amountTiyn&currency=KZT&ref=smartcart&ts=$timestamp"
    val qrBitmap = rememberQrBitmap(qrContent)

    LaunchedEffect(timestamp) {
        secondsLeft = 30
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        timestamp = System.currentTimeMillis() // Обновляем QR
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(White)
                .border(1.dp, Border, RoundedCornerShape(20.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    bitmap = qrBitmap,
                    contentDescription = "Kaspi QR",
                    modifier = Modifier.size(180.dp)
                )
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { secondsLeft / 30f },
                    modifier = Modifier.width(120.dp).height(4.dp).clip(CircleShape),
                    color = Primary,
                    trackColor = Gray100
                )
            }
        }
        
        Spacer(Modifier.height(20.dp))
        Text(t.kaspiQrScan, fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(total.asPrice(), fontWeight = FontWeight.Black, fontSize = 24.sp, color = Primary)
            Spacer(Modifier.width(12.dp))
            Icon(Icons.Rounded.Refresh, null, tint = TextMuted, modifier = Modifier.size(18.dp))
            Text("Обновление через ${secondsLeft}с", fontSize = 12.sp, color = TextMuted)
        }
    }
}

@Composable
private fun RowScope.PaymentTab(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.weight(1f).height(48.dp).clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Primary else Color.Transparent,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = if (isSelected) White else TextSecondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) White else TextSecondary)
        }
    }
}

@Composable
private fun SavedCardRow(card: SavedCard, isSelected: Boolean, onSelect: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onSelect() },
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) PrimaryLight.copy(alpha = 0.4f) else White,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Primary else Border)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(Gray100, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.CreditCard, null, tint = Primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(card.bankName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text("${card.type} •••• ${card.lastFour}", fontSize = 13.sp, color = TextSecondary)
            }
            if (isSelected) Icon(Icons.Rounded.CheckCircle, null, tint = Primary, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String, valueColor: Color = TextPrimary, labelColor: Color = TextSecondary) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = labelColor, fontSize = 15.sp)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

private fun Double.asPrice(): String = com.smartcart.data.CurrencyConfig.format(this)

@Preview(showBackground = true, device = "spec:width=1920dp,height=1104dp,dpi=160")
@Composable
private fun Preview() {
    CartScreen(onNavigateToList = {}, onNavigateToReceipt = {})
}
