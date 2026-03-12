package com.smartcart.data.repository

import androidx.compose.runtime.*
import com.google.gson.Gson
import com.smartcart.data.model.*

object AppState {
    var language  by mutableStateOf(AppLanguage.RU)
    var currentUser by mutableStateOf<User?>(null)
    var budgetTenge by mutableStateOf<Double?>(null)

    val products = mutableStateListOf<Product>().apply { addAll(MockData.products) }
    val cart      = mutableStateListOf<CartItem>()
    val shoppingList = mutableStateListOf<ShoppingListItem>().apply {
        addAll(MockData.shoppingList)
    }
    var wishlistIds by mutableStateOf(MockData.products.take(5).map { it.id }.toSet())

    // Computed
    val cartTotal    get() = cart.sumOf { it.product.price * it.quantity }
    val cartTax      get() = cartTotal * 0.08
    val cartVat      get() = cartTotal * 0.12
    val cartDiscount get() = 2.0  // fixed for demo
    val cartFinal    get() = (cartTotal + cartTax - cartDiscount).coerceAtLeast(0.0)
    val cartCount    get() = cart.sumOf { it.quantity }

    val listTotal    get() = shoppingList.sumOf { it.product.price * it.plannedQuantity }
    val listCount    get() = shoppingList.sumOf { it.plannedQuantity }

    fun t() = language.strings()

    fun addToCart(product: Product, addedByCamera: Boolean = false, addedManually: Boolean = true) {
        val i = cart.indexOfFirst { it.product.id == product.id }
        if (i >= 0) {
            val existing = cart[i]
            cart[i] = existing.copy(
                quantity = existing.quantity + 1,
                addedByCamera = existing.addedByCamera || addedByCamera,
                addedManually = existing.addedManually || addedManually
            )
        } else {
            cart.add(
                CartItem(
                    product = product,
                    quantity = 1,
                    addedByCamera = addedByCamera,
                    addedManually = addedManually
                )
            )
        }

        val sIndex = shoppingList.indexOfFirst { it.product.id == product.id }
        if (sIndex >= 0) {
            val listItem = shoppingList[sIndex]
            val newInCartQty = (listItem.inCartQuantity + 1).coerceAtMost(listItem.plannedQuantity)
            shoppingList[sIndex] = listItem.copy(
                isInCart = newInCartQty > 0,
                inCartQuantity = newInCartQty
            )
        }
        CartSyncRepository.syncCartToFirestore("cart_001")
    }

    fun removeFromCart(id: String) {
        val removed = cart.find { it.product.id == id } ?: run {
            cart.removeAll { it.product.id == id }
            return
        }
        cart.removeAll { it.product.id == id }

        val sIndex = shoppingList.indexOfFirst { it.product.id == id }
        if (sIndex >= 0) {
            val listItem = shoppingList[sIndex]
            val newInCartQty = (listItem.inCartQuantity - removed.quantity).coerceAtLeast(0)
            shoppingList[sIndex] = listItem.copy(
                isInCart = newInCartQty > 0,
                inCartQuantity = newInCartQty
            )
        }
        CartSyncRepository.syncCartToFirestore("cart_001")
    }

    fun updateCartQty(id: String, delta: Int) {
        val i = cart.indexOfFirst { it.product.id == id }
        if (i < 0 && delta > 0) {
            val product = products.find { it.id == id } ?: return
            addToCart(product, addedByCamera = false, addedManually = true)
            return
        } else if (i < 0) {
            return
        }

        val current = cart[i]
        val newQty = (current.quantity + delta).coerceAtLeast(0)
        if (newQty == 0) {
            cart.removeAt(i)
        } else {
            cart[i] = current.copy(quantity = newQty)
        }

        val sIndex = shoppingList.indexOfFirst { it.product.id == id }
        if (sIndex >= 0) {
            val listItem = shoppingList[sIndex]
            val newInCartQty = newQty.coerceAtMost(listItem.plannedQuantity)
            shoppingList[sIndex] = listItem.copy(
                isInCart = newInCartQty > 0,
                inCartQuantity = newInCartQty
            )
        }
        CartSyncRepository.syncCartToFirestore("cart_001")
    }

    fun moveListToCart() {
        shoppingList.forEachIndexed { index, item ->
            val existingIndex = cart.indexOfFirst { it.product.id == item.product.id }
            if (existingIndex >= 0) {
                val existing = cart[existingIndex]
                val additional = item.plannedQuantity - item.inCartQuantity
                if (additional > 0) {
                    cart[existingIndex] = existing.copy(quantity = existing.quantity + additional)
                }
            } else {
                cart.add(
                    CartItem(
                        product = item.product,
                        quantity = item.plannedQuantity,
                        addedByCamera = false,
                        addedManually = true
                    )
                )
            }

            shoppingList[index] = item.copy(
                isInCart = true,
                inCartQuantity = item.plannedQuantity
            )
        }
        CartSyncRepository.syncCartToFirestore("cart_001")
    }

    fun isInCart(id: String) = cart.any { it.product.id == id }
    fun cartQty(id: String)  = cart.find { it.product.id == id }?.quantity ?: 0

    fun isInWishlist(id: String) = wishlistIds.contains(id)
    fun toggleWishlist(id: String) {
        val current = wishlistIds
        wishlistIds = if (current.contains(id)) current - id else current + id
    }

    fun updateShoppingListPlannedQty(id: String, delta: Int) {
        val index = shoppingList.indexOfFirst { it.product.id == id }
        if (index < 0) return
        val item = shoppingList[index]
        val newPlanned = (item.plannedQuantity + delta).coerceAtLeast(0)
        if (newPlanned == 0) {
            shoppingList.removeAt(index)
        } else {
            val newInCart = item.inCartQuantity.coerceAtMost(newPlanned)
            shoppingList[index] = item.copy(
                plannedQuantity = newPlanned,
                inCartQuantity = newInCart,
                isInCart = newInCart > 0
            )
        }
    }

    private val gson = Gson()

    suspend fun loginWithQR(sessionCode: String): Boolean {
        kotlinx.coroutines.delay(300)

        val payload: QrSessionPayload? = try {
            if (sessionCode.trim().startsWith("{")) {
                gson.fromJson(sessionCode, QrSessionPayload::class.java)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }

        currentUser = if (payload != null) {
            User(
                id = payload.userId,
                name = payload.userName,
                email = "${payload.userId}@snappan.app",
                sessionToken = payload.sessionToken,
            )
        } else {
            User(
                id = "demo_user",
                name = "Demo User",
                email = "demo@snappan.app",
                sessionToken = sessionCode,
            )
        }

        cart.clear()
        shoppingList.clear()
        shoppingList.addAll(MockData.shoppingList)

        return true
    }

    fun logout() {
        currentUser = null
        cart.clear()
        shoppingList.clear()
        shoppingList.addAll(MockData.shoppingList)
    }

    fun buildCurrentSession(): CartSession? {
        val user = currentUser ?: return null
        return CartSession(
            sessionId = user.sessionToken.ifBlank { "local_${System.currentTimeMillis()}" },
            userId = user.id,
            cartId = "cart_1",
            items = cart.toList(),
            shoppingList = shoppingList.toList(),
            startTime = System.currentTimeMillis(),
            userName = user.name,
        )
    }

    fun restoreFromSession(session: CartSession) {
        val restoredUser = User(
            id = session.userId,
            name = session.userName.ifBlank { currentUser?.name ?: "Previous shopper" },
            email = currentUser?.email ?: "${session.userId}@snappan.app",
            sessionToken = session.sessionId,
        )
        currentUser = restoredUser

        cart.clear()
        cart.addAll(session.items)

        shoppingList.clear()
        shoppingList.addAll(session.shoppingList)
    }
}
