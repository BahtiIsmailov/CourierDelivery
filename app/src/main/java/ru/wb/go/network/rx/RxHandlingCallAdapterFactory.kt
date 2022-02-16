package ru.wb.go.network.rx

import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import ru.wb.go.network.exceptions.ErrorResolutionStrategy
import java.lang.reflect.Type

class RxHandlingCallAdapterFactory private constructor(
    private val callAdapterFactory: CallAdapter.Factory,
    private val errorResolutionStrategy: ErrorResolutionStrategy
) : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        val callAdapter = callAdapterFactory.get(returnType, annotations, retrofit) ?: return null
        return RxWrapperCallAdapter(callAdapter, errorResolutionStrategy)
    }

    companion object {
        fun create(errorResolutionStrategy: ErrorResolutionStrategy): CallAdapter.Factory {
            val callAdapterFactory = RxJava2CallAdapterFactory.create()
            return RxHandlingCallAdapterFactory(callAdapterFactory, errorResolutionStrategy)
        }
    }

}