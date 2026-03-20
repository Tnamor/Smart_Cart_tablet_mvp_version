package com.smartcart.data.repository


import com.google.firebase.firestore.FirebaseFirestore
import com.smartcart.data.repository.AppState

object CartSyncRepository {

    private val db = FirebaseFirestore.getInstance()

    fun syncCartToFirestore(
        cartId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val itemsList = AppState.cart.map { item ->
            hashMapOf(
                "name" to item.product.nameEn,
                "barcode" to item.product.barcode,
                "price" to (item.product.price * 130).toInt(),
                "quantity" to item.quantity,
                "imageUrl" to item.product.imageUrl,
                "brand" to ""
            )
        }

        val totalAmount = AppState.cart.sumOf { (it.product.price * 130).toInt() * it.quantity }

        val updateData = mapOf(
            "items" to itemsList,
            "totalAmount" to totalAmount
        )

        db.collection("carts")
            .document(cartId)
            .update(updateData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to sync cart")
            }
    }

    fun clearCartInFirestore(
        cartId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val clearData = mapOf(
            "items" to emptyList<Map<String, Any>>(),
            "totalAmount" to 0
        )

        db.collection("carts")
            .document(cartId)
            .update(clearData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to clear cart")
            }
    }

    fun setCartSessionAvailable(cartId: String) {
        db.collection("carts").document(cartId)
            .update(mapOf(
                "status" to "available",
                "connectedUserId" to "",
                "connectedUserName" to "",
                "connectedUserEmail" to "",
                "items" to emptyList<Any>(),
                "totalAmount" to 0
            ))
    }
}