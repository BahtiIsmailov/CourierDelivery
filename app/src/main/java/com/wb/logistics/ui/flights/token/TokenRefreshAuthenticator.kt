package com.wb.logistics.ui.flights.token

import com.wb.logistics.network.api.auth.query.RefreshTokenQuery
import com.wb.logistics.network.token.TokenManager
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenRefreshAuthenticator(
    // private val tokenRepository: LogisticsTokenRepository,
    private val tokenManager: TokenManager,
    private val loginRepository: RefreshRepository,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request {


//        var currentToken: Token
//        runBlocking {
//            currentToken = tokenRepository.getToken() ?: throw NullPointerException()
//        }
//
//        val newAccessToken: Token =
//            loginRepository.refreshAccessTokens(
//                currentToken.accessToken,
//                RefreshRequest(currentToken.refreshToken)
//            ).execute().body()
//                ?: throw NullPointerException()
//
//        runBlocking {
//            tokenRepository.eraseCurrentToken()
//            tokenRepository.eraseCurrentTokenResource()
//            tokenRepository.insertToken(newAccessToken)
//            tokenRepository.insertTokenResource(decodeToken(newAccessToken.accessToken))
//        }
//
//
//        sessionRepository.refreshToken()


        val newAccessToken = loginRepository.refreshAccessTokens(RefreshTokenQuery(tokenManager.bearerRefreshToken()))
            .execute().body() ?: throw NullPointerException()

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${newAccessToken.accessToken}")
            .build()
    }


}
