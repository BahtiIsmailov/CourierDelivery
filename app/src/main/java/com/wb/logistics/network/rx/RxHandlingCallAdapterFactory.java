package com.wb.logistics.network.rx;

import androidx.annotation.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class RxHandlingCallAdapterFactory extends CallAdapter.Factory {

    @NonNull
    private final CallAdapter.Factory callAdapterFactory;
    @NonNull
    private final ErrorResolutionStrategy errorResolutionStrategy;

    private RxHandlingCallAdapterFactory(@NonNull CallAdapter.Factory callAdapterFactory,
                                         @NonNull ErrorResolutionStrategy errorResolutionStrategy) {
        this.callAdapterFactory = callAdapterFactory;
        this.errorResolutionStrategy = errorResolutionStrategy;
    }

    public static CallAdapter.Factory create(@NonNull ErrorResolutionStrategy errorResolutionStrategy) {
        CallAdapter.Factory callAdapterFactory = RxJava2CallAdapterFactory.create();
        return new RxHandlingCallAdapterFactory(callAdapterFactory, errorResolutionStrategy);
    }

    @Override
    public CallAdapter<?, ?> get(@NonNull Type returnType,
                                 @NonNull Annotation[] annotations,
                                 @NonNull Retrofit retrofit) {
        CallAdapter<?, ?> callAdapter = callAdapterFactory.get(returnType, annotations, retrofit);
        if (callAdapter == null) return null;
        return new RxWrapperCallAdapter(callAdapter, errorResolutionStrategy);
    }

}
