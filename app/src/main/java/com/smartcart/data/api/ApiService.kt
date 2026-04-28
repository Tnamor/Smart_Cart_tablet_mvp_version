package com.smartcart.data.api

import okhttp3.MultipartBody
import retrofit2.http.*
import okhttp3.RequestBody


/**
 * Backend API service. BASE_URL = http://10.0.2.2:8080/ (emulator localhost).
 */
interface ApiService {

    @POST("api/loginByQr")
    suspend fun loginByQr(@Body body: LoginQrRequest): LoginResponse

    @GET("api/shopping-list/{userId}")
    suspend fun getShoppingList(@Path("userId") userId: String): List<ShoppingListItemDto>

    @POST("api/shopping-list/{userId}")
    suspend fun saveShoppingList(
        @Path("userId") userId: String,
        @Body items: List<ShoppingListItemDto>,
    ): List<ShoppingListItemDto>

    @GET("api/payment-cards/{userId}")
    suspend fun getPaymentCards(@Path("userId") userId: String): List<PaymentCardDto>

    @POST("api/payment-cards/{userId}")
    suspend fun savePaymentCards(
        @Path("userId") userId: String,
        @Body cards: List<PaymentCardDto>,
    ): List<PaymentCardDto>

    @POST("api/cart/detect")
    suspend fun cartDetect(@Body body: DetectRequest): ProductDto

    @GET("api/cart/{sessionId}")
    suspend fun getCart(@Path("sessionId") sessionId: String): List<CartItemDto>

    @HTTP(method = "DELETE", path = "api/cart/{sessionId}/item/{productId}", hasBody = false)
    suspend fun removeCartItem(
        @Path("sessionId") sessionId: String,
        @Path("productId") productId: String,
    ): List<CartItemDto>

    @POST("api/qr/generate")
    suspend fun qrGenerate(@Body body: QrGenerateRequest): QrGenerateResponse

    @GET("api/qr/status/{sessionId}")
    suspend fun getQrStatus(@Path("sessionId") sessionId: String): QrSessionState

    @POST("api/qr/confirm")
    suspend fun qrConfirm(@Body body: QrConfirmRequest): QrConfirmResponse

    @Multipart
    @POST("predict")
    suspend fun detectProductMl(
        @Part image: MultipartBody.Part,
        @Part("barcode_product") barcodeProduct: RequestBody
    ): MlResponse
}



// DTOs matching backend
data class LoginQrRequest(val qr: String)

data class LoginResponse(
    val userId: String,
    val name: String,
    val sessionToken: String,
    val loyaltyPoints: Int,
)

data class ShoppingListItemDto(
    val productId: String,
    val productName: String,
    val plannedQuantity: Int,
    val isInCart: Boolean = false,
    val inCartQuantity: Int = 0,
)

data class PaymentCardDto(
    val id: String,
    val bankName: String,
    val type: String,
    val lastFour: String,
)

data class CartItemDto(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val priceTenge: Int,
)

data class ProductDto(
    val id: String,
    val nameEn: String,
    val nameRu: String,
    val nameKk: String,
    val priceTenge: Int,
    val imageUrl: String,
    val barcode: String,
    val category: String,
)

data class DetectRequest(val barcode: String)

data class QrGenerateRequest(val userId: String)

data class QrGenerateResponse(val sessionId: String, val qrContent: String)

data class QrSessionState(
    val sessionId: String,
    val userId: String,
    val userName: String,
    val cartId: String,
    val shoppingListId: String,
    val createdAt: Long,
)

data class QrConfirmRequest(val sessionId: String, val qrContent: String)

data class QrConfirmResponse(
    val confirmed: Boolean,
    val sessionToken: String,
    val userId: String,
    val userName: String,
)

data class MlResponse(
    val detections: List<MlDetection>
)

data class MlDetection(
    val yolo_class: String?,
    val yolo_confidence: Double?
)