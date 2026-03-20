package com.smartcart.backend

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

@Serializable
data class UserDto(
    val userId: String,
    val name: String,
    val sessionToken: String,
    val loyaltyPoints: Int,
)

@Serializable
data class LoginQrRequest(val qr: String)

@Serializable
data class CheckoutItemDto(val productId: String, val qty: Int, val priceTenge: Int)

@Serializable
data class CheckoutRequest(
    val userId: String,
    val totalTenge: Int,
    val items: List<CheckoutItemDto>,
)

@Serializable
data class CheckoutResponse(
    val pointsEarned: Int,
    val newBalance: Int,
)

@Serializable
data class ShoppingListItemDto(
    val productId: String,
    val productName: String,
    val plannedQuantity: Int,
    val isInCart: Boolean = false,
    val inCartQuantity: Int = 0,
)

@Serializable
data class PaymentCardDto(
    val id: String,
    val bankName: String,
    val type: String,
    val lastFour: String,
)

@Serializable
data class CartItemDto(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val priceTenge: Int,
)

@Serializable
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

@Serializable
data class QrSessionState(
    val sessionId: String,
    val userId: String,
    val userName: String,
    val cartId: String,
    val shoppingListId: String,
    val createdAt: Long,
)

@Serializable
data class DetectRequest(val barcode: String)

@Serializable
data class QrGenerateRequest(val userId: String)

@Serializable
data class QrConfirmRequest(val sessionId: String, val qrContent: String)

@Serializable
data class QrGenerateResponse(val sessionId: String, val qrContent: String)

@Serializable
data class QrConfirmResponse(val confirmed: Boolean, val sessionToken: String, val userId: String, val userName: String)

object InMemoryStore {
    private val balances = mutableMapOf<String, Int>() // userId -> points
    val shoppingLists = mutableMapOf<String, MutableList<ShoppingListItemDto>>()
    val paymentCards = mutableMapOf<String, MutableList<PaymentCardDto>>()
    val carts = mutableMapOf<String, MutableList<CartItemDto>>()
    val qrSessions = mutableMapOf<String, QrSessionState>()

    val productsByBarcode: Map<String, ProductDto> = mapOf(
        "234567" to ProductDto("2", "Whole Milk", "Молоко цельное", "Толық сүт", 585, "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400", "234567", "Dairy"),
        "345678" to ProductDto("3", "Sourdough Bread", "Хлеб на закваске", "Ашыған нан", 845, "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400", "345678", "Bakery"),
        "123456" to ProductDto("1", "Organic Avocado", "Авокадо органик", "Органикалық Авокадо", 1157, "https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?w=400", "123456", "Fruits"),
        "456789" to ProductDto("4", "Greek Yogurt", "Йогурт греческий", "Грек йогурты", 494, "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400", "456789", "Dairy"),
        "999001" to ProductDto("9", "Fresh Strawberries", "Клубника свежая", "Жаңа құлпынай", 520, "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=400", "999001", "Fruits"),
    ).also {
        paymentCards["user_demo"] = mutableListOf(
            PaymentCardDto("kaspi", "Kaspi Bank", "Visa", "4242"),
            PaymentCardDto("halyk", "Halyk Bank", "Mastercard", "8888"),
        )
    }

    fun getBalance(userId: String): Int = balances[userId] ?: 0

    fun applyCheckout(userId: String, totalTenge: Int): CheckoutResponse {
        val points = (totalTenge / 100).coerceAtLeast(0)
        val newBalance = getBalance(userId) + points
        balances[userId] = newBalance
        return CheckoutResponse(pointsEarned = points, newBalance = newBalance)
    }
}

fun Application.module() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }
        )
    }

    routing {
        // QR login – returns demo user with current loyalty balance
        // When qr contains "demo", return sessionToken "demo_session" so cart key matches
        post("/api/loginByQr") {
            val body = call.receive<LoginQrRequest>()
            val token = if (body.qr.contains("demo")) "demo_session" else body.qr.removePrefix("session://smartcart/")
            val userId = "user_demo"
            val balance = InMemoryStore.getBalance(userId)
            val name = if (body.qr.contains("demo")) "Алия Бекова" else "Demo User"
            call.respond(
                UserDto(
                    userId = userId,
                    name = name,
                    sessionToken = token,
                    loyaltyPoints = balance,
                )
            )
        }

        // Checkout – calculates and applies loyalty points
        post("/api/cart/checkout") {
            val req = call.receive<CheckoutRequest>()
            val resp = InMemoryStore.applyCheckout(req.userId, req.totalTenge)
            call.respond(resp)
        }

        // Simple profile endpoint
        get("/api/user/{id}") {
            val id = call.parameters["id"] ?: return@get call.respondText(
                "Missing id",
                status = io.ktor.http.HttpStatusCode.BadRequest
            )
            val balance = InMemoryStore.getBalance(id)
            call.respond(
                UserDto(
                    userId = id,
                    name = "Demo User",
                    sessionToken = "",
                    loyaltyPoints = balance,
                )
            )
        }

        // Shopping list – exact paths per spec
        post("/api/shopping-list/{userId}") {
            val userId = call.parameters["userId"] ?: return@post call.respondText("Missing userId", status = io.ktor.http.HttpStatusCode.BadRequest)
            val items = call.receive<List<ShoppingListItemDto>>()
            InMemoryStore.shoppingLists[userId] = items.toMutableList()
            call.respond(items)
        }
        get("/api/shopping-list/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText("Missing userId", status = io.ktor.http.HttpStatusCode.BadRequest)
            val list = InMemoryStore.shoppingLists.getOrPut(userId) { mutableListOf() }
            call.respond(list)
        }

        // Payment cards – exact paths per spec
        post("/api/payment-cards/{userId}") {
            val userId = call.parameters["userId"] ?: return@post call.respondText("Missing userId", status = io.ktor.http.HttpStatusCode.BadRequest)
            val cards = call.receive<List<PaymentCardDto>>()
            InMemoryStore.paymentCards[userId] = cards.toMutableList()
            call.respond(cards)
        }
        get("/api/payment-cards/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText("Missing userId", status = io.ktor.http.HttpStatusCode.BadRequest)
            val cards = InMemoryStore.paymentCards[userId] ?: emptyList()
            call.respond(cards)
        }

        // Cart detect – AI camera sends barcode, returns product
        post("/api/cart/detect") {
            val req = call.receive<DetectRequest>()
            val product = InMemoryStore.productsByBarcode[req.barcode]
            if (product != null) call.respond(product)
            else call.respond(io.ktor.http.HttpStatusCode.NotFound)
        }

        // Cart endpoints (keyed by sessionId)
        get("/api/cart/{sessionId}") {
            val sessionId = call.parameters["sessionId"] ?: return@get call.respondText(
                "Missing sessionId",
                status = io.ktor.http.HttpStatusCode.BadRequest
            )
            val cart = InMemoryStore.carts.getOrPut(sessionId) { mutableListOf() }
            call.respond(cart)
        }

        delete("/api/cart/{sessionId}/item/{productId}") {
            val sessionId = call.parameters["sessionId"] ?: return@delete call.respondText(
                "Missing sessionId",
                status = io.ktor.http.HttpStatusCode.BadRequest
            )
            val productId = call.parameters["productId"] ?: return@delete call.respondText(
                "Missing productId",
                status = io.ktor.http.HttpStatusCode.BadRequest
            )
            val cart = InMemoryStore.carts[sessionId]
            if (cart != null) {
                cart.removeAll { it.productId == productId }
                call.respond(cart)
            } else {
                call.respond(emptyList<CartItemDto>())
            }
        }

        // Product by barcode (legacy)
        get("/api/products/byBarcode/{barcode}") {
            val barcode = call.parameters["barcode"] ?: return@get call.respondText(
                "Missing barcode",
                status = io.ktor.http.HttpStatusCode.BadRequest
            )
            val product = InMemoryStore.productsByBarcode[barcode]
            if (product != null) call.respond(product)
            else call.respond(io.ktor.http.HttpStatusCode.NotFound)
        }

        post("/api/barcode/detect") {
            val req = call.receive<DetectRequest>()
            val product = InMemoryStore.productsByBarcode[req.barcode]
            if (product != null) call.respond(product)
            else call.respond(io.ktor.http.HttpStatusCode.NotFound)
        }

        // QR session generate
        post("/api/qr/generate") {
            val req = call.receive<QrGenerateRequest>()
            val sessionId = "qr_${System.currentTimeMillis()}_${(1000..9999).random()}"
            val state = QrSessionState(
                sessionId = sessionId,
                userId = req.userId,
                userName = "Demo User",
                cartId = "cart_$sessionId",
                shoppingListId = "list_$sessionId",
                createdAt = System.currentTimeMillis(),
            )
            InMemoryStore.qrSessions[sessionId] = state
            call.respond(QrGenerateResponse(
                sessionId = sessionId,
                qrContent = "session://smartcart/$sessionId/${state.createdAt}",
            ))
        }

        // QR session confirm
        post("/api/qr/confirm") {
            val req = call.receive<QrConfirmRequest>()
            val state = InMemoryStore.qrSessions[req.sessionId]
            if (state != null) {
                call.respond(QrConfirmResponse(
                    confirmed = true,
                    sessionToken = req.sessionId,
                    userId = state.userId,
                    userName = state.userName,
                ))
            } else {
                call.respond(io.ktor.http.HttpStatusCode.NotFound)
            }
        }

        get("/api/qr/status/{sessionId}") {
            val sessionId = call.parameters["sessionId"] ?: return@get call.respondText(
                "Missing sessionId",
                status = io.ktor.http.HttpStatusCode.BadRequest
            )
            val state = InMemoryStore.qrSessions[sessionId]
            if (state != null) call.respond(state)
            else call.respond(io.ktor.http.HttpStatusCode.NotFound)
        }
    }
}

