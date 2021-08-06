package ru.wb.perevozka.network.token

import ru.wb.perevozka.app.AppPreffsKeys
import ru.wb.perevozka.app.VERSION_API
import ru.wb.perevozka.network.api.auth.entity.TokenEntity
import ru.wb.perevozka.utils.prefs.SharedWorker

class TokenManagerImpl(private val worker: SharedWorker) : TokenManager {

    override fun apiVersion(): String {
        return VERSION_API
    }

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
        readTokenEntity() ?: TokenEntity("", 0, "")

    private fun readTokenEntity(): TokenEntity? {
        return worker.load(AppPreffsKeys.TOKEN_KEY, TokenEntity::class.java)
    }

    private fun tokenResource(): TokenResource {
        return decodeToken(token().accessToken)
    }

    override fun clear() {
        worker.delete(AppPreffsKeys.TOKEN_KEY)
    }

    override fun isContains(): Boolean {
        return readTokenEntity() != null
    }

}