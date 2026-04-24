package com.smartcart.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.smartcart.data.model.Product
import com.smartcart.data.repository.AppState
import com.smartcart.data.repository.CartSyncRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.Executors
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import com.smartcart.ml.MlRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

private const val TAG = "TabletCameraScreen"

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraScreen(
    cartId: String = "cart_001",
    onBack: () -> Unit
) {


    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }

    var statusMessage by remember { mutableStateOf("Наведи камеру на штрихкод товара") }
    var isProcessing by remember { mutableStateOf(false) }
    var lastBarcode by remember { mutableStateOf<String?>(null) }

    var barcodeFallbackMode by remember { mutableStateOf(false) }
    var lastMlRequestTime by remember { mutableLongStateOf(0L) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasPermission = granted
            if (!granted) {
                statusMessage = "Нет разрешения на камеру"
            }
        }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasPermission) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    previewView.scaleType = PreviewView.ScaleType.FILL_CENTER

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    val cameraExecutor = Executors.newSingleThreadExecutor()
                    val barcodeScanner = BarcodeScanning.getClient()

                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder()
                                .build()
                                .also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                            val analyzer = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also { analysis ->
                                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->

                                        if (!barcodeFallbackMode) {

                                            handleMlMode(
                                                context = context,
                                                imageProxy = imageProxy,
                                                db = db,
                                                scope = scope,
                                                cartId = cartId,
                                                lastMlRequestTime = lastMlRequestTime,
                                                onLastMlRequestTimeChanged = { lastMlRequestTime = it },
                                                onProcessingChange = { isProcessing = it },
                                                onStatusChange = { statusMessage = it },
                                                onEnableBarcodeFallback = { barcodeFallbackMode = true },
                                                onProductAdded = { onBack() }
                                            )

                                        } else {

                                            analyzeBarcodeFrame(
                                                imageProxy = imageProxy,
                                                isProcessing = isProcessing,
                                                onProcessingChange = { isProcessing = it },
                                                onBarcodeDetected = { barcode ->

                                                    if (barcode == lastBarcode) return@analyzeBarcodeFrame

                                                    lastBarcode = barcode
                                                    statusMessage = "Штрихкод найден: $barcode"

                                                    scope.launch {
                                                        val product = findProductEverywhere(db, barcode = barcode)

                                                        if (product != null) {
                                                            addProductToTabletCart(product, cartId)
                                                            statusMessage = "✅ Добавлено: ${product.nameEn}"
                                                            onBack()
                                                        } else {
                                                            statusMessage = "⚠️ Товар не найден"
                                                        }

                                                        isProcessing = false
                                                    }
                                                },
                                                onError = {
                                                    statusMessage = it
                                                    isProcessing = false
                                                }
                                            )
                                        }
                                    }
                                }

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analyzer
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Camera bind error", e)
                            statusMessage = "Ошибка камеры: ${e.message}"
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = statusMessage,
                    color = Color.Black
                )

                if (isProcessing) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun analyzeBarcodeFrame(
    imageProxy: ImageProxy,
    isProcessing: Boolean,
    onProcessingChange: (Boolean) -> Unit,
    onBarcodeDetected: (String) -> Unit,
    onError: (String) -> Unit
) {
    if (isProcessing) {
        imageProxy.close()
        return
    }

    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    onProcessingChange(true)

    val image = InputImage.fromMediaImage(
        mediaImage,
        imageProxy.imageInfo.rotationDegrees
    )

    val scanner = BarcodeScanning.getClient()

    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            val barcode = barcodes
                .mapNotNull { it.rawValue?.trim() }
                .firstOrNull { it.isNotBlank() }

            if (barcode != null) {
                onBarcodeDetected(barcode)
            } else {
                onProcessingChange(false)
            }
        }
        .addOnFailureListener { e ->
            Log.e(TAG, "Barcode scan failed", e)
            onError(e.message ?: "Barcode scan failed")
        }
        .addOnCompleteListener {
            imageProxy.close()
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

    if (localProduct != null) {
        Log.d(TAG, "Found in AppState: ${localProduct.nameEn}")
        return localProduct
    }

    return try {
        val query = when {
            cleanBarcode != null -> {
                Log.d(TAG, "Searching Firestore by barcode=$cleanBarcode")
                db.collection("products").whereEqualTo("barcode", cleanBarcode)
            }

            cleanMlLabel != null -> {
                Log.d(TAG, "Searching Firestore by ml_label=$cleanMlLabel")
                db.collection("products").whereEqualTo("ml_label", cleanMlLabel)
            }

            else -> null
        }

        val result = query?.get()?.await()

        Log.d(TAG, "Firestore result count=${result?.size() ?: 0}")

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
                barcode = doc.getString("barcode") ?: "",
                unit = doc.getString("unit") ?: "шт",
                zoneId = doc.getString("zoneId") ?: doc.id
            )
        } else {
            null
        }
    } catch (e: Exception) {
        Log.e(TAG, "Firestore search failed", e)
        null
    }
}

private fun addProductToTabletCart(
    product: Product,
    cartId: String
) {
    val index = AppState.cart.indexOfFirst { item ->
        item.product.barcode.isNotBlank() &&
                item.product.barcode == product.barcode
    }

    if (index >= 0) {
        val oldItem = AppState.cart[index]
        AppState.cart[index] = oldItem.copy(
            quantity = oldItem.quantity + 1
        )
    } else {
        AppState.addToCart(
            product = product,
            addedByCamera = true,
            addedManually = false
        )
    }

    CartSyncRepository.syncCartToFirestore(cartId)
}

private fun handleMlMode(
    context: Context,
    imageProxy: ImageProxy,
    db: FirebaseFirestore,
    scope: kotlinx.coroutines.CoroutineScope,
    cartId: String,
    lastMlRequestTime: Long,
    onLastMlRequestTimeChanged: (Long) -> Unit,
    onProcessingChange: (Boolean) -> Unit,
    onStatusChange: (String) -> Unit,
    onEnableBarcodeFallback: () -> Unit,
    onProductAdded: () -> Unit
) {
    val now = System.currentTimeMillis()

    if (now - lastMlRequestTime < 1500) {
        imageProxy.close()
        return
    }

    onLastMlRequestTimeChanged(now)
    onProcessingChange(true)
    onStatusChange("Распознаем товар...")

    val file = try {
        imageProxyToJpegFile(context, imageProxy)
    } catch (e: Exception) {
        onStatusChange("Ошибка подготовки кадра")
        onProcessingChange(false)
        imageProxy.close()
        return
    }

    scope.launch {
        try {
            val mlResult = withContext(Dispatchers.IO) {
                MlRepository.sendFrameToMl(file)
            }

            val bestDetection = mlResult?.detections
                ?.maxByOrNull { it.yolo_confidence ?: 0.0 }

            val yoloClassRaw = bestDetection?.yolo_class?.trim()?.lowercase()
            val yoloConfidence = bestDetection?.yolo_confidence ?: 0.0
            val yoloClass = normalizeYoloLabel(yoloClassRaw)

            Log.d(TAG, "ML result class=$yoloClass confidence=$yoloConfidence")

            if (yoloClass != null && yoloConfidence >= 0.35) {
                onStatusChange("Найдено: $yoloClass")

                val product = findProductEverywhere(
                    db = db,
                    mlLabel = yoloClass
                )

                if (product != null) {
                    addProductToTabletCart(product, cartId)
                    onStatusChange("✅ Добавлено: ${product.nameEn}")
                    onProcessingChange(false)
                    onProductAdded()
                } else {
                    onStatusChange("Товар не найден в базе. Сканируйте штрихкод")
                    onProcessingChange(false)
                    onEnableBarcodeFallback()
                }
            } else {
                onStatusChange("ML не распознал товар. Сканируйте штрихкод")
                onProcessingChange(false)
                onEnableBarcodeFallback()
            }
        } catch (e: Exception) {
            Log.e(TAG, "ML error", e)
            onStatusChange("Ошибка ML: ${e.message}")
            onProcessingChange(false)
            onEnableBarcodeFallback()
        } finally {
            if (file.exists()) file.delete()
            imageProxy.close()
        }
    }
}

private fun imageProxyToJpegFile(context: Context, imageProxy: ImageProxy): File {
    val yBuffer = imageProxy.planes[0].buffer
    val uBuffer = imageProxy.planes[1].buffer
    val vBuffer = imageProxy.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(
        nv21,
        ImageFormat.NV21,
        imageProxy.width,
        imageProxy.height,
        null
    )

    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(
        Rect(0, 0, imageProxy.width, imageProxy.height),
        90,
        out
    )

    val file = File(context.cacheDir, "ml_frame_${System.currentTimeMillis()}.jpg")
    file.writeBytes(out.toByteArray())

    return file
}

private fun normalizeYoloLabel(label: String?): String? {
    if (label == null) return null

    return when (label.lowercase()) {
        "apple" -> "apple"
        "banana" -> "banana"
        "orange" -> "orange"

        "coca-cola_can" -> "coca-cola can"
        "coca-cola_box" -> "coca-cola carton"
        "coca-cola_bottle" -> "coca-cola bottle"

        "fanta_box" -> "fanta carton"
        "fanta_bottle" -> "fanta bottle"

        "fusetea_box" -> "fuse tea carton"
        "fusetea_bottle" -> "fuse tea bottle"

        "sprite_box" -> "sprite carton"
        "sprite_bottle" -> "sprite bottle"

        else -> label.replace("_", " ")
    }
}