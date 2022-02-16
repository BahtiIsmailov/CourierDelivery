package ru.wb.go.network.exceptions

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import ru.wb.go.app.AppConsts.HTTP_OBJECT_NOT_FOUND
import ru.wb.go.app.AppConsts.HTTP_PAGE_NOT_FOUND
import ru.wb.go.app.AppConsts.SERVICE_CODE_BAD_REQUEST
import ru.wb.go.app.AppConsts.SERVICE_CODE_FORBIDDEN
import ru.wb.go.app.AppConsts.SERVICE_CODE_LOCKED
import ru.wb.go.app.AppConsts.SERVICE_CODE_UNAUTHORIZED
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException


class ErrorResolutionStrategyImpl(
    private val resourceProvider: ErrorResolutionResourceProvider
) : ErrorResolutionStrategy {

    override fun apply(call: Observable<*>): Observable<*> {
        return call
            .onErrorResumeNext { throwable: Throwable -> Observable.error(convertException(throwable)) }
    }

    override fun apply(call: Single<*>): Single<*> {
        return call.onErrorResumeNext { Single.error { convertException(it) } }
    }

    override fun apply(call: Completable): Completable {
        return call.onErrorResumeNext { Completable.error { convertException(it) } }
    }

    override fun apply(call: Maybe<*>): Maybe<*> {
        return call.onErrorResumeNext { throwable: Throwable? -> Maybe.error(throwable) }
    }

    private fun convertException(throwable: Throwable): Throwable {
        return when (throwable) {
            is UnknownHostException -> notInternetException()
            is ConnectException -> notInternetException()
            is SSLException -> timeoutException()
            is HttpException -> getHttpException(throwable)
            is SocketTimeoutException -> timeoutException()
            is JsonSyntaxException -> timeoutException()
            else -> getUnknownException(throwable.message)
        }
    }

    private fun notInternetException() = NoInternetException(resourceProvider.noInternetError)

    private fun timeoutException() = TimeoutException(resourceProvider.timeoutServiceError)

    private fun getUnknownException(message: String?): Throwable {
        return UnknownException(
            if (message.isNullOrEmpty()) resourceProvider.bodyIsEmptyOrNull else message,
            resourceProvider.unknownError
        )
    }

    private fun getHttpException(exception: HttpException): Throwable {
        val error = convertMessageException(exception.response())
        return when (exception.code()) {
            SERVICE_CODE_BAD_REQUEST -> BadRequestException(error)
            SERVICE_CODE_UNAUTHORIZED -> UnauthorizedException(error.message)
            SERVICE_CODE_FORBIDDEN -> ForbiddenException(error.message)
            SERVICE_CODE_LOCKED -> LockedException(error.message)
            HTTP_PAGE_NOT_FOUND -> if (error.code == HTTP_OBJECT_NOT_FOUND) {
                HttpPageNotFoundException(error.message)
            } else HttpObjectNotFoundException(error.message, error.code)
            else -> UnknownHttpException(exception.toString())
        }
    }

    private fun convertMessageException(response: Response<*>?): Error {
        var body = response?.errorBody()?.charStream()?.readText()
        return if (isJSONValid(body)) Gson().fromJson(body, ApiErrorModel::class.java)
        else {
            if (response?.code() == HTTP_PAGE_NOT_FOUND) {
                body = """
                    $body
                    ${response.raw().request.url}
                    """.trimIndent()
            }
            ApiErrorModel(
                Error(
                    if (body.isNullOrEmpty()) resourceProvider.bodyIsEmptyOrNull else body,
                    "E" + response?.code(),
                    Data(0)
                )
            )
        }.error
    }

    private fun isJSONValid(body: String?): Boolean {
        if (body == null) return false
        try {
            JSONObject(body)
        } catch (ex: JSONException) {
            try {
                JSONArray(body)
            } catch (ex1: JSONException) {
                return false
            }
        }
        return true
    }

}