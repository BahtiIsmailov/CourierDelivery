package ru.wb.go.network.token

import ru.wb.go.app.AppPreffsKeys
import ru.wb.go.app.VERSION_API
import ru.wb.go.network.api.auth.entity.TokenEntity
import ru.wb.go.utils.prefs.SharedWorker

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
        return tokenResource().sub ?: ""
    }

    // TODO: 09.12.2021 заменить на реальный токен
    override fun userInn(): String {
        return "928371627364"
    }

    override fun userCompany(): String {
        return tokenResource().extra.company ?: ""
    }

    override fun userCompanyId(): String {
        return tokenResource().extra.companyID.toString() ?: ""
    }

    override fun userPhone(): String {
        return tokenResource().extra.phone
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

    override fun resources(): List<String> {
        return tokenResource().extra.resources ?: mutableListOf()
    }

}