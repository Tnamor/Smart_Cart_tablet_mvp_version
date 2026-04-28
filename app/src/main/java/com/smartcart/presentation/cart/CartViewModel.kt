package com.smartcart.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcart.data.repository.AppState
import com.smartcart.data.repository.PurchaseRepository
import com.smartcart.data.repository.SessionCache
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CartViewModel @Inject constructor(
    private val sessionCache: SessionCache,
) : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    fun completePurchase(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isProcessing.value = true
            
            // 1. Сохраняем локально для кэша
            val session = AppState.buildCurrentSession()
            if (session != null) {
                sessionCache.save(session)
            }

            // 2. Сохраняем в Firestore
            PurchaseRepository.savePurchase(
                onSuccess = { receiptId ->
                    _isProcessing.value = false
                    onSuccess(receiptId)
                },
                onError = { message ->
                    _isProcessing.value = false
                    onError(message)
                }
            )
        }
    }
}
