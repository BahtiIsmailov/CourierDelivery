package com.wb.logistics.network.rx;

import androidx.annotation.NonNull;

import com.google.gson.JsonSyntaxException;
import com.wb.logistics.app.AppConsts;
import com.wb.logistics.network.exceptions.ApiGeneralException;
import com.wb.logistics.network.exceptions.NoInternetException;
import com.wb.logistics.network.exceptions.TimeoutException;
import com.wb.logistics.network.exceptions.UnauthorizedException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class ErrorResolutionStrategyImpl implements ErrorResolutionStrategy {

    @NonNull
    private final CallAdapterFactoryResourceProvider resourceProvider;

    public ErrorResolutionStrategyImpl(@NonNull CallAdapterFactoryResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    @NotNull
    @Override
    public Observable<?> apply(@NonNull Observable<?> call) {
        return call.doOnNext(o -> processOnNext())
                .onErrorResumeNext(throwable -> {
                    return Observable.error(convertException((Throwable) throwable));
                });
    }

    @NotNull
    @Override
    public Single<?> apply(@NonNull Single<?> call) {
        return call.doOnSuccess(o -> processOnNext())
                .onErrorResumeNext(throwable ->
                        Single.error(convertException((Throwable) throwable))
                );
    }

    @NotNull
    @Override
    public Completable apply(@NonNull Completable call) {
        return call.onErrorResumeNext(throwable ->
                Completable.error(convertException(throwable))
        );
    }

    private void processOnNext() {
        // TODO: 10.03.2021 prolong session
    }

    private Throwable convertException(@NonNull Throwable throwable) {
        if (throwable instanceof UnknownHostException) {
            return getNotInternetException();
        } else if (throwable instanceof ConnectException) {
            return getNotInternetException();
        } else if (throwable instanceof SSLException) {
            return getTimeoutException();
        } else if (throwable instanceof HttpException) {
            return getHttpException((HttpException) throwable);
        } else if (throwable instanceof SocketTimeoutException) {
            return getTimeoutException();
        } else if (throwable instanceof JsonSyntaxException) {
            return getTimeoutException();
        } else {
            return getTimeoutException();
        }
    }

    @NonNull
    private Throwable getNotInternetException() {
        return new NoInternetException(resourceProvider.getNoInternetError());
    }

    @NonNull
    private Throwable getTimeoutException() {
        return new TimeoutException(resourceProvider.getTimeoutServiceError());
    }

    @NonNull
    private Throwable getHttpException(@NonNull HttpException exception) {
        int code = exception.code();
        switch (code) {
            // TODO: 15.03.2019 переработать case после изменения API авторизации
            case AppConsts.SERVICE_CODE_AUTHORIZED:
                return getAuthorizedException(exception);
            case AppConsts.SERVICE_CODE_UNAUTHORIZED:
                String extensionMessage = "";
                try {
                    ResponseBody responseBody = exception.response().errorBody();
                    if (responseBody != null) {
                        JSONObject jObject = new JSONObject(responseBody.string());
                        extensionMessage = jObject.getString("message");
                    }
                } catch (JSONException e) {
                    //LogUtils.logError("ERROR_RES", e.getMessage());
                } catch (IOException e) {
                    //LogUtils.logError("ERROR_RES", e.getMessage());
                }
                return getUnauthorizedException(code, extensionMessage);
            default:
                return getTimeoutException();
        }
    }

    @NonNull
    private Throwable getAuthorizedException(@NonNull HttpException exception) {
        try {
            String logicalErrorMessage = "";
            ResponseBody responseBody = exception.response().errorBody();
            if (responseBody != null) {
                logicalErrorMessage = responseBody.string();
            }
            // TODO: 27.12.2018 заменить responseBody на errorResponse.getApiMessage() после переработки сервера
            //ApiGeneralErrorResponse errorResponse = new Gson().fromJson(responseBody, ApiGeneralErrorResponse.class);
            return new ApiGeneralException(exception.getMessage(), logicalErrorMessage);
        } catch (JsonSyntaxException e) {
            return new ApiGeneralException(exception.getMessage(), resourceProvider.getUnauthorizedError());
        } catch (IOException e) {
            return exception;
        }
    }

    @NonNull
    private Throwable getUnauthorizedException(int code, String extensionMessage) {
        return new UnauthorizedException(resourceProvider.getUnauthorizedError(), code, extensionMessage);
    }

}
