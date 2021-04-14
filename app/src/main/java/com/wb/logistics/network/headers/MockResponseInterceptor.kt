package com.wb.logistics.network.headers

import android.os.ConditionVariable
import android.text.TextUtils
import com.wb.logistics.network.api.auth.entity.TokenEntity
import com.wb.logistics.network.api.auth.query.RefreshTokenQuery
import com.wb.logistics.network.token.TokenManager
import com.wb.logistics.ui.flights.token.RefreshRepositoryImpl
import com.wb.logistics.utils.LogUtils
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection
import java.util.concurrent.atomic.AtomicBoolean


class MockResponseInterceptor(
    private val headerManager: HeaderManager,
    private val tokenManager: TokenManager,
) : Interceptor {

    private val LOCK = ConditionVariable(true)
    private val mIsRefreshing: AtomicBoolean = AtomicBoolean(false)

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()

        // 1. sign this request
        val builder = request.newBuilder()
        for ((key, value) in headerManager.headerApiMap) {
            LogUtils { logDebugApp("Header" + key + " " + value) }
            builder.addHeader(key, value)
        }
        val newRequest = builder.build()

        // 2. proceed with the request
        var response = chain.proceed(newRequest) //request

        // 3. check the response: have we got a 401?
        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            if (!TextUtils.isEmpty(tokenManager.bearerToken())) {
                /*
                *  Because we send out multiple HTTP requests in parallel, they might all list a 401 at the same time.
                *  Only one of them should refresh the token, because otherwise we'd refresh the same token multiple times
                *  and that is bad. Therefore we have these two static objects, a ConditionVariable and a boolean. The
                *  first thread that gets here closes the ConditionVariable and changes the boolean flag.
                */
                if (mIsRefreshing.compareAndSet(false, true)) {
                    LOCK.close()

                    /* we're the first here. let's refresh this token.
                    *  it looks like our token isn't valid anymore.
                    *  REFRESH the actual token here
                    */

                    val refreshRepository = RefreshRepositoryImpl()
                    val refreshResponse =
                        refreshRepository.refreshAccessTokens(RefreshTokenQuery(tokenManager.refreshToken()))
                            .execute().body()?: throw NullPointerException()
                    val tokenEntity = with(refreshResponse) {
                        TokenEntity(accessToken, expiresIn, refreshToken)
                    }
                    tokenManager.saveToken(tokenEntity)

                    LOCK.open()
                    mIsRefreshing.set(false)
                } else {
                    // Another thread is refreshing the token for us, let's wait for it.
                    val conditionOpened = LOCK.block(15000)

                    // If the next check is false, it means that the timeout expired, that is - the refresh
                    // stuff has failed.
                    if (conditionOpened) {

                        // another thread has refreshed this for us! thanks!
                        // sign the request with the new token and proceed
                        // return the outcome of the newly signed request

                        val newBuilder = newRequest.newBuilder()
                        for ((key, value) in headerManager.headerApiMap) {
                            LogUtils { logDebugApp("Header" + key + " " + value) }
                            newBuilder.addHeader(key, value)
                        }

                        response = chain.proceed(newRequest)
                    }
                }
            }
        }

        // check if still unauthorized (i.e. refresh failed)
        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            //... // clean your access token and prompt for request again.
            tokenManager.clear()
        }

        // returning the response to the original request
        return response;


    }


}