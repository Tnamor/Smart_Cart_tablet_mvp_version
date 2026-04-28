package com.smartcart.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.smartcart.data.model.Product
import com.smartcart.data.model.AppStrings
import com.smartcart.data.repository.AppState
import com.smartcart.ui.theme.Primary
import com.smartcart.ui.theme.SuccessGreen
import com.smartcart.ui.theme.TextPrimary

private data class StoreZone(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val color: Float,
)

@Composable
fun StoreMapOverlayDialog(
    product: Product,
    onDismiss: () -> Unit,
) {
    val db = remember { FirebaseFirestore.getInstance() }
    var zones by remember { mutableStateOf<List<StoreZone>>(emptyList()) }

    val tappedZoneId = product.zoneId
    val t = AppState.t()

    DisposableEffect(Unit) {
        var registration: ListenerRegistration? = null
        registration = db.collection("storeMap")
            .document("zones")
            .addSnapshotListener { snapshot, _ ->
                val data = snapshot?.data
                if (data == null || data.isEmpty()) {
                    zones = emptyList()
                    return@addSnapshotListener
                }

                val parsed = data.entries.mapNotNull { (zoneId, rawZone) ->
                    val rawMap = rawZone as? Map<*, *> ?: return@mapNotNull null
                    val name = rawMap["name"] as? String ?: zoneId
                    val x = (rawMap["x"] as? Number)?.toFloat() ?: return@mapNotNull null
                    val y = (rawMap["y"] as? Number)?.toFloat() ?: return@mapNotNull null
                    val w = (rawMap["width"] as? Number)?.toFloat() ?: return@mapNotNull null
                    val h = (rawMap["height"] as? Number)?.toFloat() ?: return@mapNotNull null
                    val c = (rawMap["color"] as? Number)?.toFloat() ?: 0.5f
                    StoreZone(
                        id = zoneId,
                        name = name,
                        x = x,
                        y = y,
                        width = w,
                        height = h,
                        color = c,
                    )
                }

                zones = parsed
            }

        onDispose {
            registration?.remove()
        }
    }

    val highlightedZone = zones.firstOrNull { it.id == tappedZoneId }

    Dialog(onDismissRequest = onDismiss) {
        StoreMapOverlayContent(
            product = product,
            zones = zones,
            highlightedZone = highlightedZone,
            t = t,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun StoreMapOverlayContent(
    product: Product,
    zones: List<StoreZone>,
    highlightedZone: StoreZone?,
    t: AppStrings,
    onDismiss: () -> Unit,
) {
    val density = LocalDensity.current
    val borderStrokePx = with(density) { 3.dp.toPx() }
    val normalStrokePx = with(density) { 1.dp.toPx() }
    val pinBaseRadiusPx = with(density) { 10.dp.toPx() }

    val transition = rememberInfiniteTransition(label = "pin_pulse")
    val scale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pin_scale",
    )
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pin_alpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
    ) {
        // Consume clicks inside the content; only outside clicks dismiss.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
                .clickable(enabled = true) { /* consume */ },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = highlightedZone?.name ?: t.storeMap,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close")
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.95f))
                    .padding(8.dp),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasW = size.width
                    val canvasH = size.height

                    fun zoneToRect(z: StoreZone): Rect {
                        val left = z.x * canvasW
                        val top = z.y * canvasH
                        val right = (z.x + z.width) * canvasW
                        val bottom = (z.y + z.height) * canvasH
                        return Rect(left, top, right, bottom)
                    }

                    zones.forEach { z ->
                        val rect = zoneToRect(z)
                        val isHighlighted = z.id == highlightedZone?.id
                        val fill = zoneColor(z.color, isHighlighted)

                        // Fill
                        drawRect(
                            color = fill,
                            topLeft = Offset(rect.left, rect.top),
                            size = androidx.compose.ui.geometry.Size(rect.width, rect.height)
                        )

                        // Border
                        drawRect(
                            color = if (isHighlighted) Primary else fill.copy(alpha = 0.35f),
                            topLeft = Offset(rect.left, rect.top),
                            size = androidx.compose.ui.geometry.Size(rect.width, rect.height),
                            style = Stroke(width = if (isHighlighted) borderStrokePx else normalStrokePx)
                        )
                    }

                    highlightedZone?.let { z ->
                        val rect = zoneToRect(z)
                        val center = Offset(
                            x = rect.left + rect.width / 2f,
                            y = rect.top + rect.height / 2f,
                        )

                        // Pulsing pin
                        drawCircle(
                            color = Primary,
                            radius = pinBaseRadiusPx * scale,
                            center = center,
                            alpha = alpha
                        )
                        drawCircle(
                            color = SuccessGreen,
                            radius = pinBaseRadiusPx * 0.25f,
                            center = center,
                            alpha = 0.95f
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            val aisleShelf = highlightedZone?.name
                ?.let { name -> splitAisleShelf(name) }
                ?: Pair(null, null)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Primary.copy(alpha = 0.92f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = aisleShelf.first?.let { "${t.aisle}: $it" } ?: "${t.aisle}: -",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = aisleShelf.second?.let { "${t.shelf}: $it" } ?: "${t.shelf}: -",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun zoneColor(color: Float, highlighted: Boolean): Color {
    // Firestore might store color in 0..1; if so, treat it as an intensity.
    val intensity = color.coerceIn(0f, 1f)
    val alpha = if (highlighted) 0.25f + intensity * 0.2f else 0.12f + intensity * 0.15f
    // Use the app primary purple as the base tone.
    return Primary.copy(alpha = alpha)
}

private fun splitAisleShelf(zoneName: String): Pair<String?, String?> {
    // Best-effort parsing: support common delimiters.
    val normalized = zoneName.trim()
    val parts = when {
        normalized.contains("-") -> normalized.split("-", limit = 2)
        normalized.contains("—") -> normalized.split("—", limit = 2)
        normalized.contains(":") -> normalized.split(":", limit = 2)
        normalized.contains("/") -> normalized.split("/", limit = 2)
        else -> emptyList()
    }
    return if (parts.size == 2) parts[0].trim() to parts[1].trim() else null to null
}
