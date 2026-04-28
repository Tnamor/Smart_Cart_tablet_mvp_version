package com.smartcart.ui.components

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter

fun generateQrBitmap(content: String, sizePx: Int = 600): Bitmap {
    val hints = mapOf(EncodeHintType.MARGIN to 1)
    val matrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
    val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
    for (x in 0 until sizePx)
        for (y in 0 until sizePx)
            bmp.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.rgb(26, 26, 46) else android.graphics.Color.WHITE)
    return bmp
}

@Composable
fun rememberQrBitmap(content: String): ImageBitmap =
    remember(content) { generateQrBitmap(content).asImageBitmap() }
