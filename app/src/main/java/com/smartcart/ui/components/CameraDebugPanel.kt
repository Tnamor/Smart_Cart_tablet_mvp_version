package com.smartcart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartcart.BuildConfig
import com.smartcart.data.repository.AppState
import com.smartcart.presentation.camera.CameraSimulationViewModel
import com.smartcart.ui.theme.Background
import com.smartcart.ui.theme.Primary
import com.smartcart.ui.theme.White

@Composable
fun CameraDebugPanel(
    modifier: Modifier = Modifier,
    viewModel: CameraSimulationViewModel = hiltViewModel(),
) {
    if (!BuildConfig.DEBUG) return

    var isVisible by rememberSaveable { mutableStateOf(true) }
    if (!isVisible) return

    val event by viewModel.lastEvent.collectAsState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(Background.copy(alpha = 0.9f)),
        color = White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Simulate cart camera",
                    style = MaterialTheme.typography.labelLarge,
                    color = Primary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "DEBUG ONLY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = { isVisible = false }) {
                        Text("Скрыть", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            event?.let {
                Text(
                    text = it.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(4.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val products = AppState.products
                products.forEach { product ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${product.nameRu} (${product.barcode})",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = { viewModel.simulateProductAdded(product.barcode) }
                            ) {
                                Text("+1")
                            }
                            Button(
                                onClick = { viewModel.simulateProductRemoved(product.barcode) }
                            ) {
                                Text("-1")
                            }
                        }
                    }
                }
            }
        }
    }
}
