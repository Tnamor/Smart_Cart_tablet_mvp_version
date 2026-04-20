package com.smartcart.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object PurchaseRepository {

    private val db = FirebaseFirestore.getInstance()

    fun savePurchase(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = AppState.currentUser

        if (currentUser == null) {
            onError("User is not logged in")
            return
        }

        val cartItems = AppState.cart.toList()
        if (cartItems.isEmpty()) {
            onError("Cart is empty")
            return
        }

        val uid = currentUser.id
        val email = currentUser.email
        val userName = currentUser.name

        val receiptId = "RCP_" + UUID.randomUUID()
            .toString()
            .replace("-", "")
            .take(8)
            .uppercase()

        val purchaseTime = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        ).format(Date())

        AppState.cart.forEach { item ->
            Log.d("PRICE_DEBUG", "${item.product.nameEn} -> ${item.product.price}")
        }
        val itemsList = cartItems.map { item ->
            hashMapOf(
                "name" to item.product.localizedName(AppState.language),
                "barcode" to item.product.barcode,
                "price" to (item.product.price).toInt(),
                "quantity" to item.quantity,
                "imageUrl" to item.product.imageUrl,
                "brand" to "",
                "productId" to item.product.id,
                "unit" to item.product.unit
            )
        }

        val totalAmount = cartItems.sumOf { (it.product.price).toInt() * it.quantity }
        val totalItems = cartItems.sumOf { it.quantity }

        val purchaseData = hashMapOf(
            "receiptId" to receiptId,
            "purchaseTime" to purchaseTime,
            "totalAmount" to totalAmount,
            "totalItems" to totalItems,
            "userId" to uid,
            "userEmail" to email,
            "userName" to userName,
            "items" to itemsList
        )

        db.collection("users")
            .document(uid)
            .collection("purchases")
            .document(receiptId)
            .set(purchaseData)
            .addOnSuccessListener {
                onSuccess(receiptId)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to save purchase")
            }
    }
}