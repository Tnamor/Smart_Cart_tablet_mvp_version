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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.smartcart.data.repository.AppState
import com.smartcart.ui.theme.Background
import com.smartcart.ui.theme.Border
import com.smartcart.ui.theme.Primary
import com.smartcart.ui.theme.TextMuted
import com.smartcart.ui.theme.TextPrimary
import com.smartcart.ui.theme.TextSecondary
import com.smartcart.ui.theme.White

@Composable
fun ReceiptScreen(
    receiptId: String,
    onBack: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = AppState.currentUser

    var receiptData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isFinishing by remember { mutableStateOf(false) }

    LaunchedEffect(receiptId) {
        if (currentUser == null) {
            error = "User not logged in"
            isLoading = false
            return@LaunchedEffect
        }

        db.collection("users")
            .document(currentUser.id)
            .collection("purchases")
            .document(receiptId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    receiptData = document.data
                } else {
                    error = "Receipt not found"
                }
                isLoading = false
            }
            .addOnFailureListener { e ->
                error = e.message ?: "Failed to load receipt"
                isLoading = false
            }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        }

        error != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(24.dp)
            ) {
                Column {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = null,
                            tint = TextPrimary
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "Error",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = error ?: "Unknown error",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                }
            }
        }

        receiptData != null -> {
            val receiptIdValue = receiptData?.get("receiptId") as? String ?: ""
            val purchaseTime = receiptData?.get("purchaseTime") as? String ?: ""
            val totalAmount = (receiptData?.get("totalAmount") as? Number)?.toInt() ?: 0
            val totalItems = (receiptData?.get("totalItems") as? Number)?.toInt() ?: 0
            val userName = receiptData?.get("userName") as? String ?: ""
            val userEmail = receiptData?.get("userEmail") as? String ?: ""

            @Suppress("UNCHECKED_CAST")
            val items = receiptData?.get("items") as? List<Map<String, Any>> ?: emptyList()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = null,
                            tint = TextPrimary
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "Receipt",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = TextPrimary
                    )
                }

                Spacer(Modifier.height(20.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = White,
                    tonalElevation = 1.dp,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                    ) {
                        Text(
                            text = "SMARTCART STORE",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Receipt ID: $receiptIdValue",
                            fontSize = 15.sp,
                            color = TextSecondary
                        )

                        Text(
                            text = "Time: $purchaseTime",
                            fontSize = 15.sp,
                            color = TextSecondary
                        )

                        if (userName.isNotBlank()) {
                            Text(
                                text = "Customer: $userName",
                                fontSize = 15.sp,
                                color = TextSecondary
                            )
                        }

                        if (userEmail.isNotBlank()) {
                            Text(
                                text = "Email: $userEmail",
                                fontSize = 15.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(Modifier.height(24.dp))
                        HorizontalDivider(color = Border)
                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = "Items",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )

                        Spacer(Modifier.height(16.dp))

                        items.forEach { item ->
                            val name = item["name"] as? String ?: ""
                            val quantity = (item["quantity"] as? Number)?.toInt() ?: 0
                            val price = (item["price"] as? Number)?.toInt() ?: 0
                            val barcode = item["barcode"] as? String ?: ""
                            val rowTotal = quantity * price

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )

                                        if (barcode.isNotBlank()) {
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = "Barcode: $barcode",
                                                fontSize = 12.sp,
                                                color = TextMuted
                                            )
                                        }

                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "$quantity × ${price.asTenge()}",
                                            fontSize = 14.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    Text(
                                        text = rowTotal.asTenge(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                Spacer(Modifier.height(10.dp))
                                HorizontalDivider(color = Border)
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total items",
                                fontSize = 16.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = totalItems.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total amount",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = totalAmount.asTenge(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Primary
                            )
                        }

                        Spacer(Modifier.height(32.dp))

                        Text(
                            text = "Thank you for shopping with SmartCart!",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp,
                            color = TextSecondary
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                isFinishing = true

                                db.collection("carts")
                                    .document("cart_001")
                                    .update(
                                        mapOf(
                                            "status" to "available",
                                            "totalAmount" to 0,
                                            "items" to emptyList<Map<String, Any>>(),

                                            "connectedUserId" to FieldValue.delete(),
                                            "connectedUserEmail" to FieldValue.delete(),
                                            "connectedUserName" to FieldValue.delete(),
                                            "connectedAt" to FieldValue.delete()
                                        )
                                    )
                                    .addOnSuccessListener {
                                        onBack()
                                    }
                                    .addOnFailureListener {
                                        onBack()
                                    }
                            },
                            enabled = !isFinishing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text(
                                text = if (isFinishing) "Finishing..." else "Done",
                                color = White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Int.asTenge(): String = "$this ₸"