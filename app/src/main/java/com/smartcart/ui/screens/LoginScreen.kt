package com.smartcart.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.smartcart.data.model.CartSession
import com.smartcart.data.model.MockData
import com.smartcart.data.model.User
import com.smartcart.data.repository.AppState
import com.smartcart.ui.components.LanguageSwitcher
import com.smartcart.ui.components.rememberQrBitmap
import com.smartcart.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

enum class ScanStatus { READY, SCANNING, AUTHENTICATED }

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val scope       = rememberCoroutineScope()
    val t           = AppState.t()

    // Unique session ID — QR encodes this
    val cartId = remember { "cart_001" }
    val qrContent = cartId
    val qrBitmap: ImageBitmap = rememberQrBitmap(qrContent)

    var status      by remember { mutableStateOf(ScanStatus.READY) }
    var showAlt     by remember { mutableStateOf(false) }
    var altCode     by remember { mutableStateOf("") }

    val db = FirebaseFirestore.getInstance()
    var connectedUserName by remember { mutableStateOf<String?>(null) }
    var listenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }

    LaunchedEffect(cartId) {
        listenerRegistration?.remove()

        listenerRegistration = db.collection("carts")
            .document(cartId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    android.util.Log.e("QR_LOGIN", "Firestore listener error", error)
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    android.util.Log.d("QR_LOGIN", "snapshot null or not exists")
                    return@addSnapshotListener
                }

                android.util.Log.d("QR_LOGIN", "data=${snapshot.data}")

                val statusValue = snapshot.getString("status")?.trim()?.lowercase() ?: "available"
                val userName = snapshot.getString("connectedUserName") ?: ""
                val userEmail = snapshot.getString("connectedUserEmail") ?: ""
                val userId = snapshot.getString("connectedUserId") ?: ""

                android.util.Log.d("QR_LOGIN", "status=$statusValue userId=$userId userName=$userName email=$userEmail")

                if (statusValue == "connected") {
                    connectedUserName = if (userName.isNotBlank()) userName else userEmail
                    status = ScanStatus.AUTHENTICATED

                    AppState.currentUser = User(
                        id = userId.ifBlank { "user_remote" },
                        name = connectedUserName ?: "User",
                        email = userEmail
                    )

                    scope.launch {
                        delay(500)
                        android.util.Log.d("QR_LOGIN", "calling onLoginSuccess()")
                        onLoginSuccess()
                    }
                } else {
                    connectedUserName = null
                    status = ScanStatus.READY
                }
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            listenerRegistration?.remove()
        }
    }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val contents = result.contents ?: return@rememberLauncherForActivityResult
        scope.launch {
            status = ScanStatus.AUTHENTICATED
            delay(400)
            AppState.currentUser = User(id = "user_scanned", name = "Anna K.", email = "anna@example.com")
            onLoginSuccess()
        }
    }

    fun manualLogin() {
        if (altCode.isBlank()) return
        scope.launch {
            status = ScanStatus.AUTHENTICATED
            delay(600)
            AppState.currentUser = User(id = "user_manual", name = "Manual User", email = "manual@example.com")
            onLoginSuccess()
        }
    }

    fun startDemoMode() {
        scope.launch {
            status = ScanStatus.SCANNING

            val demoSession = CartSession(
                sessionId = "demo_session_001",
                userId = "user_demo",
                cartId = "cart_001",
                items = emptyList(),
                shoppingList = MockData.shoppingList,
                startTime = System.currentTimeMillis(),
                userName = "Алия Бекова",
            )

            AppState.currentUser = User(
                id = demoSession.userId,
                name = demoSession.userName,
                email = "demo@snappan.app",
                sessionToken = demoSession.sessionId,
            )
            AppState.cart.clear()
            AppState.shoppingList.clear()
            AppState.shoppingList.addAll(demoSession.shoppingList)

            status = ScanStatus.AUTHENTICATED
            delay(400)
            onLoginSuccess()
        }
    }

    // Background gradient: from-primary-light via-white to-pink-100
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFEDE9FE), Color(0xFFFAF8FF),
                        Color(0xFFFFF0F5), Color(0xFFFCE7F3)
                    )
                )
            )
    ) {
        // Language switcher top-right
        Box(Modifier.align(Alignment.TopEnd).padding(24.dp)) {
            LanguageSwitcher()
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo + Title Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF9B6DFF), Primary)),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.ShoppingCart, null, tint = White, modifier = Modifier.size(26.dp))
                }
                Text(t.loginTitle, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Spacer(Modifier.height(32.dp))

            // White card (matches w-[480px] from React)
            Surface(
                modifier = Modifier.width(480.dp),
                shape = RoundedCornerShape(28.dp),
                color = White,
                shadowElevation = 20.dp,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // QR Code container with corner brackets
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .background(White, RoundedCornerShape(16.dp))
                            .border(2.dp, Border, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        // QR image
                        androidx.compose.foundation.Image(
                            bitmap = qrBitmap,
                            contentDescription = "QR Code",
                            modifier = Modifier.size(200.dp)
                        )
                        // Corner brackets (top-left, top-right, bottom-left, bottom-right)
                        QrCornerBrackets()
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(t.scanToLogin, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text(t.scanInstruction, fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))

                    // Status badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        StatusBadge(
                            status = status,
                            t_ready = t.readyToScan,
                            t_scanning = t.scanning,
                            t_auth = t.authenticated
                        )
                    }

                    if (connectedUserName != null) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${t.authenticated}: $connectedUserName",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = SuccessGreen,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Demo Mode button — directly under scanner/status, outlined style
                    Spacer(Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = { startDemoMode() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderStrong),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(t.demoMode, fontWeight = FontWeight.Medium)
                    }

                    // Divider and Alternative Login exactly as in reference design
                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(color = Border)
                    Spacer(Modifier.height(16.dp))

                    // Alternative Login
                    if (!showAlt) {
                        OutlinedButton(
                            onClick = { showAlt = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderStrong),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) {
                            Icon(Icons.Rounded.Keyboard, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(t.altLogin, fontWeight = FontWeight.Medium)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = altCode,
                                onValueChange = { altCode = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Enter session token or JSON", color = TextMuted) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { manualLogin() }),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = BorderStrong
                                )
                            )
                            FilledIconButton(
                                onClick = ::manualLogin,
                                modifier = Modifier.size(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Primary)
                            ) {
                                Icon(Icons.Rounded.ArrowForward, null, tint = White)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { showAlt = false; altCode = "" }) {
                            Text("Hide manual login", fontSize = 12.sp, color = TextMuted)
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text(t.needHelp, fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// QR corner brackets — matches React absolute positioned divs
@Composable
private fun BoxScope.QrCornerBrackets() {
    val color = Primary
    val len = 20.dp
    val thick = 3.5.dp

    // Top-left
    Box(Modifier.size(len).align(Alignment.TopStart).offset((-2).dp, (-2).dp)) {
        Box(Modifier.fillMaxWidth().height(thick).background(color, RoundedCornerShape(thick / 2)))
        Box(Modifier.width(thick).fillMaxHeight().background(color, RoundedCornerShape(thick / 2)))
    }
    // Top-right
    Box(Modifier.size(len).align(Alignment.TopEnd).offset(2.dp, (-2).dp)) {
        Box(Modifier.fillMaxWidth().height(thick).background(color, RoundedCornerShape(thick / 2)))
        Box(Modifier.width(thick).fillMaxHeight().align(Alignment.CenterEnd).background(color, RoundedCornerShape(thick / 2)))
    }
    // Bottom-left
    Box(Modifier.size(len).align(Alignment.BottomStart).offset((-2).dp, 2.dp)) {
        Box(Modifier.fillMaxWidth().height(thick).align(Alignment.BottomCenter).background(color, RoundedCornerShape(thick / 2)))
        Box(Modifier.width(thick).fillMaxHeight().background(color, RoundedCornerShape(thick / 2)))
    }
    // Bottom-right
    Box(Modifier.size(len).align(Alignment.BottomEnd).offset(2.dp, 2.dp)) {
        Box(Modifier.fillMaxWidth().height(thick).align(Alignment.BottomCenter).background(color, RoundedCornerShape(thick / 2)))
        Box(Modifier.width(thick).fillMaxHeight().align(Alignment.CenterEnd).background(color, RoundedCornerShape(thick / 2)))
    }
}

@Composable
private fun StatusBadge(
    status: ScanStatus,
    t_ready: String, t_scanning: String, t_auth: String
) {
    val pingAnim = rememberInfiniteTransition(label = "ping")
    val alpha by pingAnim.animateFloat(
        0.2f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "a"
    )

    val bgColor = when (status) {
        ScanStatus.AUTHENTICATED -> SuccessGreen.copy(alpha = 0.1f)
        else -> PrimaryLight
    }
    val textColor = when (status) {
        ScanStatus.AUTHENTICATED -> SuccessGreen
        else -> PrimaryDark
    }

    Surface(shape = CircleShape, color = bgColor) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (status) {
                ScanStatus.READY -> Box(Modifier.size(8.dp).background(PrimaryDark, CircleShape))
                ScanStatus.SCANNING -> Box(Modifier.size(8.dp).background(AccentOrange.copy(alpha = alpha), CircleShape))
                ScanStatus.AUTHENTICATED -> Box(
                    modifier = Modifier.size(20.dp).background(SuccessGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = White, modifier = Modifier.size(12.dp))
                }
            }
            Text(
                text = when (status) {
                    ScanStatus.READY -> t_ready
                    ScanStatus.SCANNING -> t_scanning
                    ScanStatus.AUTHENTICATED -> t_auth
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1920dp,height=1200dp,dpi=160")
@Composable
private fun Preview() {
    SmartCartTheme { LoginScreen {} }
}
