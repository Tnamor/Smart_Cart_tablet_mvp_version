package com.smartcart.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcart.data.repository.AppState
import com.smartcart.data.repository.SessionCache
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class CartViewModel @Inject constructor(
    private val sessionCache: SessionCache,
) : ViewModel() {

    fun completePurchase(onCompleted: () -> Unit) {
        viewModelScope.launch {
            val session = AppState.buildCurrentSession()
            if (session != null) {
                sessionCache.save(session)
            }
            onCompleted()
        }
    }
}
