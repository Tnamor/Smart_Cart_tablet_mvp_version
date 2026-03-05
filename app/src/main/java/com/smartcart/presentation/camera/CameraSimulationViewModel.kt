package com.smartcart.presentation.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcart.data.model.Product
import com.smartcart.data.repository.AppState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CameraSimulationEvent(
    val message: String,
    val product: Product?,
)

@HiltViewModel
class CameraSimulationViewModel @Inject constructor() : ViewModel() {

    private val _lastEvent = MutableStateFlow<CameraSimulationEvent?>(null)
    val lastEvent: StateFlow<CameraSimulationEvent?> = _lastEvent.asStateFlow()

    fun simulateProductAdded(productBarcode: String) {
        viewModelScope.launch {
            val product = AppState.products.find { it.barcode == productBarcode }
            if (product != null) {
                AppState.addToCart(product, addedByCamera = true, addedManually = false)
                _lastEvent.value = CameraSimulationEvent(
                    message = "Product detected: ${product.nameRu}",
                    product = product
                )
            }
        }
    }

    fun simulateProductRemoved(productBarcode: String) {
        viewModelScope.launch {
            val product = AppState.products.find { it.barcode == productBarcode }
            if (product != null) {
                AppState.updateCartQty(product.id, -1)
                _lastEvent.value = CameraSimulationEvent(
                    message = "Product removed: ${product.nameRu}",
                    product = product
                )
            }
        }
    }
}

