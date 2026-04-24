package com.smartcart.ml

import android.util.Log
import com.smartcart.data.api.RetrofitClient
import com.smartcart.data.api.MlResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object MlRepository {

    private const val TAG = "ML_REPO"

    suspend fun sendFrameToMl(file: File): MlResponse? {
        return try {
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())

            val body = MultipartBody.Part.createFormData(
                "file",
                file.name,
                requestFile
            )

            val response = RetrofitClient.api.detectProductMl(body)

            Log.d(TAG, "ML response = $response")

            response
        } catch (e: Exception) {
            Log.e(TAG, "ML error", e)
            null
        }
    }
}