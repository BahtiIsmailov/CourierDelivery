package ru.wb.go.network.exceptions;

import static ru.wb.go.app.AppConsts.HTTP_OBJECT_NOT_FOUND;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Objects;

import javax.net.ssl.SSLException;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import kotlin.Unit;
import kotlin.io.TextStreamsKt;
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
        Error error = convertMessageException(Objects.requireNonNull(exception.response()));
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
            case AppConsts.HTTP_PAGE_NOT_FOUND:
                if (error.getCode().equals(HTTP_OBJECT_NOT_FOUND)) {
                    return new HttpPageNotFound(error.getMessage());
                } else
                    return new HttpObjectNotFoundException(error.getMessage(), error.getCode());
            default:
                return new UnknownHttpException(exception.toString());
        }
    }

    private Error convertMessageException(Response<?> response) {
        ApiErrorModel apiErrorModel;
        String body = TextStreamsKt.readText(Objects.requireNonNull(response.errorBody()).charStream());

        if (isJSONValid(body)) {
            apiErrorModel = new Gson().fromJson(body, ApiErrorModel.class);
        } else {
            if (response.code() == 404) {
                body = body + "\n" + response.raw().request().url();
            }
            apiErrorModel = new ApiErrorModel(new Error(body, "E" + response.code(), new Data(0)));
        }

        return apiErrorModel.getError();
    }

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

}
