package com.smartcart.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class LoginViewModel : ViewModel() {

    private val _qrContent = MutableStateFlow(generateQrContent())
    val qrContent: StateFlow<String> = _qrContent.asStateFlow()

    private val _countdownSeconds = MutableStateFlow(30)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

    init {
        viewModelScope.launch {
            countdownLoop()
        }
    }

    private fun generateQrContent(): String {
        val uuid = UUID.randomUUID().toString().replace("-", "").take(12)
        val timestamp = System.currentTimeMillis()
        return "session://smartcart/$uuid/$timestamp"
    }

    private suspend fun countdownLoop() {
        while (true) {
            delay(1000L)
            val current = _countdownSeconds.value
            if (current <= 0) {
                _qrContent.value = generateQrContent()
                _countdownSeconds.value = 30
            } else {
                _countdownSeconds.value = current - 1
            }
        }
    }
}
