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
import kotlinx.coroutines.withContext

@HiltAndroidApp
class SmartCartApp : Application() {

    @Inject lateinit var sessionCacheProvider: javax.inject.Provider<SessionCache>

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Restore last cached session; mutate Compose state only on Main.
        scope.launch {
            try {
                val session: CartSession? = sessionCacheProvider.get().loadLastSession()
                if (session != null) {
                    withContext(Dispatchers.Main) {
                        AppState.restoreFromSession(session)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }
}
