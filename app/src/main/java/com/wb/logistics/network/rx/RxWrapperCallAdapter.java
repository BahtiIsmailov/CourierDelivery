package com.wb.logistics.network.rx;

import androidx.annotation.NonNull;

import com.wb.logistics.network.exceptions.ErrorResolutionStrategy;

import java.lang.reflect.Type;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.CallAdapter;

public class RxWrapperCallAdapter<R> implements CallAdapter<R, Object> {

    @NonNull
    private final CallAdapter<R, Object> wrapped;
    @NonNull
    private final ErrorResolutionStrategy errorResolutionStrategy;

    RxWrapperCallAdapter(@NonNull CallAdapter<R, Object> wrapped,
                         @NonNull ErrorResolutionStrategy errorResolutionStrategy) {
        this.wrapped = wrapped;
        this.errorResolutionStrategy = errorResolutionStrategy;
    }

    @Override
    public Type responseType() {
        return wrapped.responseType();
    }

    @Override
    public Object adapt(@NonNull Call<R> call) {
        Object adaptedCall = wrapped.adapt(call);
        if (adaptedCall instanceof Observable) {
            return errorResolutionStrategy.apply((Observable) adaptedCall);
        } else if (adaptedCall instanceof Single) {
            return errorResolutionStrategy.apply((Single) adaptedCall);
        } else if (adaptedCall instanceof Completable) {
            return errorResolutionStrategy.apply((Completable) adaptedCall);
        }
        throw new UnsupportedOperationException("Only Observable or Single or Completable supported");
    }

}
