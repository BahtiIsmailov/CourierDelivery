package com.wb.logistics.ui.flights.token

import com.wb.logistics.network.token.TokenManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


class TokenAuthorizationInterceptor(private val  tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request().signedRequest()
        )
    }

    private fun Request.signedRequest(): Request {

//        val accessToken: Token
//        runBlocking {
//            accessToken = tokenRepository.getToken() ?: Token("null","null",3)
//        }

        return newBuilder()
            .header("Authorization", tokenManager.bearerToken())
            .build()
    }
}
