package com.smartcart.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.smartcart.BuildConfig
import com.smartcart.data.model.Product
import com.smartcart.data.repository.AppState
import com.smartcart.data.repository.CartSyncRepository
import com.smartcart.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt

@Composable
fun CameraDebugPanel(modifier: Modifier = Modifier, onOpenCamera: () -> Unit) {

    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }
    val cartId = "cart_001"
    if (!BuildConfig.DEBUG) return

    var eventMessage by remember { mutableStateOf<String?>(null) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val barcode = result.contents.trim()
            val format = result.formatName

            eventMessage = "Raw: $barcode ($format)"
            android.util.Log.d("CAMERA_DEBUG", "RAW barcode='$barcode', format='$format'")

            scope.launch {
                val product = findProductEverywhere(
                    db = db,
                    barcode = barcode
                )

                if (product != null) {
                    addToTabletCart(product, cartId)
                    eventMessage = "✅ Scanned: ${product.nameEn}"
                } else {
                    eventMessage = "⚠️ Not found: $barcode ($format)"
                }
            }
        }
    }

    Surface(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .width(280.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.9f),
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DEBUG ONLY",
                    color = AccentOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Panel (Drag to move)",
                    color = White.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            }

            // Real Camera Access
            Button(
                onClick = {
                    onOpenCamera()
                },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Rounded.CameraAlt, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Open Real Camera", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Divider(color = White.copy(alpha = 0.1f))

            Text(
                "Or Simulate detection:",
                color = White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            LazyColumn(
                modifier = Modifier.heightIn(max = 140.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(AppState.products, key = { it.id }) { product ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                addToTabletCart(product, cartId)
                                eventMessage = "Simulated: ${product.nameEn}"
                            },
                        color = White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = product.nameEn,
                            color = White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            eventMessage?.let {
                Text(
                    text = it,
                    color = SuccessGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                LaunchedEffect(it) {
                    kotlinx.coroutines.delay(2000)
                    eventMessage = null
                }
            }
        }
    }
}

private suspend fun findProductEverywhere(
    db: FirebaseFirestore,
    barcode: String? = null,
    mlLabel: String? = null
): Product? {
    val cleanBarcode = barcode?.trim()
    val cleanMlLabel = mlLabel?.trim()?.lowercase()

    val localProduct = AppState.products.find {
        (cleanBarcode != null && it.barcode.trim() == cleanBarcode) ||
                (cleanMlLabel != null && it.nameEn.trim().lowercase() == cleanMlLabel)
    }

    android.util.Log.d(
        "CAMERA_DEBUG",
        "SCANNED barcode='$cleanBarcode', localFound=${localProduct?.nameEn}"
    )

    if (localProduct != null) return localProduct

    return try {
        val query = when {
            cleanBarcode != null -> {
                android.util.Log.d("CAMERA_DEBUG", "Searching Firestore by barcode=$cleanBarcode")
                db.collection("products").whereEqualTo("barcode", cleanBarcode)
            }
            cleanMlLabel != null -> {
                db.collection("products").whereEqualTo("ml_label", cleanMlLabel)
            }
            else -> null
        }

        val result = query?.get()?.await()
        android.util.Log.d("CAMERA_DEBUG", "Firestore result count = ${result?.size() ?: 0}")

        if (result != null && !result.isEmpty) {
            val doc = result.documents[0]
            Product(
                id = doc.id,
                nameEn = doc.getString("name") ?: "",
                nameRu = doc.getString("nameRu") ?: doc.getString("name") ?: "",
                nameKk = doc.getString("nameKk") ?: doc.getString("name") ?: "",
                price = (doc.get("price") as? Number)?.toDouble() ?: 0.0,
                imageUrl = doc.getString("imageUrl") ?: "",
                category = doc.getString("brand") ?: "",
                barcode = doc.getString("barcode") ?: ""
            )
        } else null
    } catch (e: Exception) {
        android.util.Log.e("CAMERA_DEBUG", "Firestore search failed", e)
        null
    }
}
private fun addToTabletCart(
    product: Product,
    cartId: String
) {
    AppState.addToCart(
        product = product,
        addedByCamera = true,
        addedManually = false
    )

    CartSyncRepository.syncCartToFirestore(cartId)
}