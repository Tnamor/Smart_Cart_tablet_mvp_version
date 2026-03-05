package com.smartcart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartcart.data.repository.AppState
import com.smartcart.ui.theme.Background
import com.smartcart.ui.theme.Primary
import com.smartcart.ui.theme.SuccessGreen
import com.smartcart.ui.theme.TextMuted
import com.smartcart.ui.theme.TextPrimary
import com.smartcart.ui.theme.White

@Composable
fun ReceiptScreen(onBackToLogin: () -> Unit) {
    val t = AppState.t()
    val cart = AppState.cart.toList()
    val subtotal = AppState.cartTotal
    val vat = AppState.cartVat
    val discount = AppState.cartDiscount
    val total = AppState.cartFinal

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(520.dp),
            shape = RoundedCornerShape(24.dp),
            color = White,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.ReceiptLong,
                            contentDescription = null,
                            tint = Primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Покупка завершена",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                    }
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen
                    )
                }

                Text(
                    text = "Спасибо за покупку! Вот краткая сводка по вашей корзине.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )

                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cart) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = item.product.localizedName(AppState.language),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${item.quantity} × ${item.product.formattedPrice()}",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                            Text(
                                text = "${(item.lineTotal * 130).toInt()}₸",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SummaryRow(label = t.subtotal, value = "${(subtotal * 130).toInt()}₸")
                    SummaryRow(label = t.vat, value = "${(vat * 130).toInt()}₸")
                    SummaryRow(
                        label = t.memberDiscount,
                        value = "-${(discount * 130).toInt()}₸"
                    )
                    HorizontalDivider()
                    SummaryRow(
                        label = t.total,
                        value = "${(total * 130).toInt()}₸",
                        bold = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onBackToLogin,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Новый покупатель", fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = t.terms,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(260.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = if (bold) 18.sp else 14.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

