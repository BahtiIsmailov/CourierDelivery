package ru.wb.go.network.token

import ru.wb.go.app.*
import ru.wb.go.network.api.auth.entity.TokenEntity
import ru.wb.go.utils.prefs.SharedWorker

class TokenManagerImpl(private val worker: SharedWorker) : TokenManager {

    override fun apiVersion(): String {
        return VERSION_API
    }

    override fun apiDemoVersion(): String {
        return VERSION_DEMO_API
    }

    override fun apiVersion3(): String {
        return VERSION_API3
    }

    override fun wbUserID(): String {
        return tokenResource().extra?.wbUserID.toString()
    }

    override fun saveToken(token: TokenEntity) {
        worker.save(AppPreffsKeys.TOKEN_KEY, token)
    }

    override fun bearerToken(): String {
        return "Bearer ${token().accessToken}"
    }

    override fun refreshToken(): String {
        return token().refreshToken
    }

    override fun userName(): String {
        return tokenResource().sub ?: ""
    }

    override fun userInn(): String {
        return worker.load(AppPreffsKeys.USER_INN_KEY, "")
    }

    override fun userCompany(): String {
        //TODO нет компании у курьеров!
        return ""
    }

    override fun userCompanyId(): String {
        return tokenResource().extra?.companyID.toString()
    }

    override fun userPhone(): String {
        return tokenResource().extra?.phone ?: ""
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
        worker.delete(AppPreffsKeys.USER_INN_KEY)
    }

    override fun isContains(): Boolean {
        return readTokenEntity() != null
    }

    override fun isDemo(): Boolean {
        return !isContains() && !worker.load(AppPreffsKeys.DEMO_OFF_KEY, false)
    }

    override fun turnOffDemo() {
        worker.save(AppPreffsKeys.DEMO_OFF_KEY, true)
    }

    override fun resources(): List<String> {
        return tokenResource().extra?.resources ?: mutableListOf()
    }

    override fun isUserCourier(): Boolean {
        return (readTokenEntity() != null) && (userCompanyId() == COURIER_COMPANY_ID
                || resources().contains(COURIER_ROLE))

    }
}