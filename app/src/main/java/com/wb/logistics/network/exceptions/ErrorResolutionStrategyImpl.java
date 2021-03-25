package com.wb.logistics.network.exceptions;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.wb.logistics.app.AppConsts;
import com.wb.logistics.utils.LogUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import kotlin.Unit;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

public class ErrorResolutionStrategyImpl implements ErrorResolutionStrategy {

    @NonNull
    private final ErrorResolutionResourceProvider resourceProvider;
//    @NonNull
//    private final AuthRepository authRepository;


    public ErrorResolutionStrategyImpl(@NonNull ErrorResolutionResourceProvider resourceProvider
    ) { //@NonNull AuthRepository authRepository
        this.resourceProvider = resourceProvider;
        // this.authRepository = authRepository;
    }

    @NotNull
    @Override
    public Observable<?> apply(@NonNull Observable<?> call) {
        return call.onErrorResumeNext(throwable -> {
            return Observable.error(convertException(throwable));
        });
    }

    @NotNull
    @Override
    public Single<?> apply(@NonNull Single<?> call) {
        return call.onErrorResumeNext(throwable ->
                Single.error(convertException(throwable))
        );
    }

    @NotNull
    @Override
    public Completable apply(@NonNull Completable call) {
        return call.onErrorResumeNext(throwable ->
                Completable.error(convertException(throwable))
        );
    }

    private Throwable convertException(@NonNull Throwable throwable) {
        new LogUtils(logUtils -> {
            logUtils.logDebugApp(throwable.toString());
            return Unit.INSTANCE;
        });
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
            return getUnknownException(throwable.getMessage());
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
    private Throwable getUnknownException(String message) {
        return new UnknownException(message, resourceProvider.getUnknownError());
    }

    @NonNull
    private Throwable getHttpException(@NonNull HttpException exception) {
        String message = convertMessageException(exception.response());
        int code = exception.code();
        switch (code) {
            case AppConsts.SERVICE_CODE_BAD_REQUEST:
                return new BadRequestException(message);
            case AppConsts.SERVICE_CODE_UNAUTHORIZED:
                return new UnauthorizedException(message);
            case AppConsts.SERVICE_CODE_FORBIDDEN:
                return new ForbiddenException(message);
            case AppConsts.SERVICE_CODE_LOCKED:
                return new LockedException(message);
            default:
                return new UnknownHttpException(resourceProvider.getUnknownHttpError(), exception.message(), code);
        }
    }

    private String convertMessageException(Response<?> response) {
        ApiErrorModel apiErrorModel = new ApiErrorModel(new Error("Unknown error", ""));
        try {
            ResponseBody responseBody = response.errorBody();
            if (responseBody != null) {
                apiErrorModel = new Gson().fromJson(responseBody.string(), ApiErrorModel.class);
            }
        } catch (IOException e) {
            return apiErrorModel.getError().getMessage();
        }
        return apiErrorModel.getError().getMessage();
    }

}
