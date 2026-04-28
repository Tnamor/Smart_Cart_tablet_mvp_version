package com.smartcart.ml

import android.util.Log
import com.smartcart.data.api.RetrofitClient
import com.smartcart.data.api.MlResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import okhttp3.RequestBody.Companion.toRequestBody

object MlRepository {

    private const val TAG = "ML_REPO"

    suspend fun sendFrameToMl(
        file: File,
        barcodeProduct: String = "x"
    ): MlResponse? {
        return try {
            Log.d(TAG, "Sending file=${file.absolutePath}, size=${file.length()}")

            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())

            val body = MultipartBody.Part.createFormData(
                "image",
                file.name,
                requestFile
            )

            val barcodePart = barcodeProduct.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = RetrofitClient.api.detectProductMl(
                body,
                barcodePart
            )

            Log.d(TAG, "ML response = $response")
            response

        } catch (e: Exception) {
            Log.e(TAG, "ML error: ${e.message}", e)
            null
        }
    }
}