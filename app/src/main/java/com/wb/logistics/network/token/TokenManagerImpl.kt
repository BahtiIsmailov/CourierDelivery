package com.wb.logistics.network.token

import com.wb.logistics.app.AppPreffsKeys
import com.wb.logistics.network.api.auth.entity.TokenEntity
import com.wb.logistics.utils.prefs.SharedWorker

class TokenManagerImpl(private val worker: SharedWorker) : TokenManager {

    override fun saveToken(token: TokenEntity) {
        worker.save(AppPreffsKeys.TOKEN_KEY, token)
    }

    override fun bearerToken(): String {
        return "Bearer ${token().accessToken}"
    }

    override fun bearerRefreshToken(): String {
        return "Bearer ${token().refreshToken}"
    }

    override fun refreshToken(): String {
        return token().refreshToken
    }

    override fun userName(): String {
        return tokenResource().sub
    }

    override fun userCompany(): String {
        return tokenResource().extra.company
    }

    private fun token(): TokenEntity =
        worker.load(AppPreffsKeys.TOKEN_KEY, TokenEntity::class.java) ?: TokenEntity("", 0, "")

    private fun tokenResource(): TokenResource {
        return decodeToken(token().accessToken)
    }

    override fun clear() {
        worker.delete(AppPreffsKeys.TOKEN_KEY)
    }

}