package com.wb.logistics.network.headers

import com.wb.logistics.app.AppPreffsKeys
import com.wb.logistics.utils.prefs.SharedWorker

class TokenManagerImpl(private val worker: SharedWorker) : TokenManager {

    override fun saveApiToken(token: String) {
        worker.save(AppPreffsKeys.TOKEN_KEY, token)
    }

    override val bearerToken: String
        get() = "Bearer $token"

    private val token: String
        get() = worker.load(AppPreffsKeys.TOKEN_KEY, "")

    override fun clear() {
        worker.delete(AppPreffsKeys.TOKEN_KEY)
    }
}