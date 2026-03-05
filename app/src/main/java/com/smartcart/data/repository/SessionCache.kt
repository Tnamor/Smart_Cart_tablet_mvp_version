package com.smartcart.data.repository

import com.google.gson.Gson
import com.smartcart.data.local.LastSessionDao
import com.smartcart.data.local.LastSessionEntity
import com.smartcart.data.model.CartSession
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class SessionCache @Inject constructor(
    private val dao: LastSessionDao,
    private val gson: Gson,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun saveAsync(session: CartSession) {
        scope.launch {
            save(session)
        }
    }

    suspend fun save(session: CartSession) {
        withContext(Dispatchers.IO) {
            val json = gson.toJson(session)
            dao.upsert(
                LastSessionEntity(
                    id = 0,
                    json = json,
                    updatedAt = System.currentTimeMillis(),
                )
            )
        }
    }

    suspend fun loadLastSession(): CartSession? =
        withContext(Dispatchers.IO) {
            val entity = dao.getLastSession() ?: return@withContext null
            runCatching { gson.fromJson(entity.json, CartSession::class.java) }.getOrNull()
        }
}

