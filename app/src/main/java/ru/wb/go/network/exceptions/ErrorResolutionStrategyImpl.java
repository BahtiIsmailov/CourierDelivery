package ru.wb.go.network.exceptions;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import kotlin.Unit;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;
import ru.wb.go.app.AppConsts;
import ru.wb.go.utils.LogUtils;

public class ErrorResolutionStrategyImpl implements ErrorResolutionStrategy {

    private final static int NUMBER_ATTEMPTS_ON_ERROR = 2;

    @NonNull
    private final ErrorResolutionResourceProvider resourceProvider;


    public ErrorResolutionStrategyImpl(@NonNull ErrorResolutionResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
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
        return call.retryWhen(this::retryWhenUnauthorized);
    }

    @NotNull
    @Override
    public Completable apply(@NonNull Completable call) {
        return call.retryWhen(this::retryWhenUnauthorized);
    }

    @NotNull
    @Override
    public Maybe<?> apply(@NotNull Maybe<?> call) {
        return call.retryWhen(this::retryWhenUnauthorized);
    }

    private Flowable<Completable> retryWhenUnauthorized(Flowable<Throwable> throwableFlowable) {
        return throwableFlowable.flatMap(throwable -> {
            if (throwable instanceof HttpException) {
                int code = ((HttpException) throwable).code();
                if (code == AppConsts.SERVICE_CODE_UNAUTHORIZED) {
                    return Flowable.just(Completable.complete());
                } else {
                    return Flowable.error(convertException(throwable));
                }
            }
            return Flowable.error(convertException(throwable));
        }).take(NUMBER_ATTEMPTS_ON_ERROR);
    }

    private Throwable convertException(@NonNull Throwable throwable) {
        new LogUtils(logUtils -> {
            logUtils.logDebugApp("Throwable convertException " + throwable.toString());
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
        Error error = convertMessageException(exception.response());
        int code = exception.code();
        switch (code) {
            case AppConsts.SERVICE_CODE_BAD_REQUEST:
                return new BadRequestException(error);
            case AppConsts.SERVICE_CODE_UNAUTHORIZED:
                return new UnauthorizedException(error.getMessage());
            case AppConsts.SERVICE_CODE_FORBIDDEN:
                return new ForbiddenException(error.getMessage());
            case AppConsts.SERVICE_CODE_LOCKED:
                return new LockedException(error.getMessage());
            default:
                return new UnknownHttpException(resourceProvider.getUnknownHttpError(), exception.message(), code);
        }
    }

    private Error convertMessageException(Response<?> response) {
        ApiErrorModel apiErrorModel = new ApiErrorModel(new Error("Unknown error", "", new Data(0)));
        try {
            ResponseBody responseBody = response.errorBody();
            if (responseBody != null) {
                apiErrorModel = new Gson().fromJson(responseBody.string(), ApiErrorModel.class);
            }
        } catch (IOException e) {
            return apiErrorModel.getError();
        }
        return apiErrorModel.getError();
    }

}
