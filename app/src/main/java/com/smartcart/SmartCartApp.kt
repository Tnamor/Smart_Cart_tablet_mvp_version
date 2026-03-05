package com.smartcart

import android.app.Application
import com.smartcart.data.model.CartSession
import com.smartcart.data.repository.AppState
import com.smartcart.data.repository.SessionCache
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class SmartCartApp : Application() {

    @Inject lateinit var sessionCache: SessionCache

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Restore last cached session if available (offline support)
        scope.launch {
            val session: CartSession? = sessionCache.loadLastSession()
            if (session != null) {
                AppState.restoreFromSession(session)
            }
        }
    }
}
