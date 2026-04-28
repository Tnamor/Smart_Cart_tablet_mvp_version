package com.smartcart.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartIdStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getCartId(): String {
        val existing = prefs.getString(KEY_CART_ID, null)
        if (!existing.isNullOrBlank()) return existing

        val serial = try {
            Build.SERIAL
        } catch (_: Throwable) {
            "UNKNOWN"
        }

        val newId = "cart_${serial}"
        prefs.edit().putString(KEY_CART_ID, newId).apply()
        return newId
    }

    companion object {
        private const val PREFS_NAME = "smartcart_prefs"
        private const val KEY_CART_ID = "cart_id"
    }
}

