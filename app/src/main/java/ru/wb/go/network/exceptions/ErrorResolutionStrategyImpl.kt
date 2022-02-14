package ru.wb.go.network.exceptions

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.reactivex.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import ru.wb.go.app.AppConsts.HTTP_OBJECT_NOT_FOUND
import ru.wb.go.app.AppConsts.HTTP_PAGE_NOT_FOUND
import ru.wb.go.app.AppConsts.REFRESH_TOKEN_INVALID
import ru.wb.go.app.AppConsts.SERVICE_CODE_BAD_REQUEST
import ru.wb.go.app.AppConsts.SERVICE_CODE_FORBIDDEN
import ru.wb.go.app.AppConsts.SERVICE_CODE_LOCKED
import ru.wb.go.app.AppConsts.SERVICE_CODE_UNAUTHORIZED
import ru.wb.go.ui.app.domain.AppNavRepository
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class ErrorResolutionStrategyImpl(
    private val resourceProvider: ErrorResolutionResourceProvider,
    private val appNavRepository: AppNavRepository
) :
    ErrorResolutionStrategy {

    override fun apply(call: Observable<*>): Observable<*> {
        return call.retryWhen { throwableObservable: Observable<Throwable> ->
            retryWhenUnauthorized(throwableObservable)
        }
    }

    private fun retryWhenUnauthorized(throwableObservable: Observable<Throwable>): Observable<Completable> {
        return throwableObservable
            .flatMap {
                if (isUnauthorized(it)) Observable.just(Completable.complete())
                else Observable.error(convertException(it))
            }
            .take(NUMBER_ATTEMPTS_ON_ERROR.toLong())
    }

    private fun isUnauthorized(it: Throwable) =
        it is HttpException && it.code() == SERVICE_CODE_UNAUTHORIZED

    override fun apply(call: Single<*>): Single<*> {
        return call.retryWhen { throwableFlowable: Flowable<Throwable> ->
            retryWhenUnauthorized(throwableFlowable)
        }
    }

    override fun apply(call: Completable): Completable {
        return call.retryWhen { throwableFlowable: Flowable<Throwable> ->
            retryWhenUnauthorized(throwableFlowable)
        }
    }

    override fun apply(call: Maybe<*>): Maybe<*> {
        return call.retryWhen { throwableFlowable: Flowable<Throwable> ->
            retryWhenUnauthorized(throwableFlowable)
        }
    }

    private fun retryWhenUnauthorized(throwableFlowable: Flowable<Throwable>): Flowable<Completable> {
        return throwableFlowable
            .flatMap { makeError(it) }
            .take(NUMBER_ATTEMPTS_ON_ERROR.toLong())
    }

    private fun makeError(throwable: Throwable) =
        when {
            isUnauthorized(throwable) -> {
                Flowable.just(Completable.complete())
            }
            throwable is RefreshAccessTokenException -> {
                Flowable.error(refreshTokenException(throwable))
            }
            else -> {
                Flowable.error(convertException(throwable))
            }
        }

    private fun refreshTokenException(exceptionAccess: RefreshAccessTokenException): Throwable {
        val error = convertMessageException(exceptionAccess.message)
        return if (error.code == REFRESH_TOKEN_INVALID) {
            appNavRepository.navigate("to_auth")
            UnauthorizedException(resourceProvider.unauthorizedError)
        } else {
            convertException(exceptionAccess)
        }
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

    private fun convertMessageException(message: String): Error {
        return if (isJSONValid(message))
            Gson().fromJson(message, ApiErrorModel::class.java).error
        else {
            Error(
                message.ifEmpty { resourceProvider.bodyIsEmpty },
                "E$SERVICE_CODE_UNAUTHORIZED",
                Data(0)
            )
        }
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

    companion object {
        private const val NUMBER_ATTEMPTS_ON_ERROR = 2
    }

}