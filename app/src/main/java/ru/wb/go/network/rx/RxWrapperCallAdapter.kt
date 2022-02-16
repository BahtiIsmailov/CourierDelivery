package ru.wb.go.network.rx

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.CallAdapter
import ru.wb.go.network.exceptions.ErrorResolutionStrategy
import java.lang.reflect.Type

class RxWrapperCallAdapter<R> internal constructor(
    private val callAdapter: CallAdapter<R, *>,
    private val errorResolutionStrategy: ErrorResolutionStrategy
) : CallAdapter<R, Any> {

    override fun responseType(): Type {
        return callAdapter.responseType()
    }

    override fun adapt(call: Call<R>): Any {
        return when (val adaptedCall = callAdapter.adapt(call)) {
            is Observable<*> -> errorResolutionStrategy.apply(adaptedCall)
            is Single<*> -> errorResolutionStrategy.apply(adaptedCall)
            is Completable -> errorResolutionStrategy.apply(adaptedCall)
            is Maybe<*> -> errorResolutionStrategy.apply(adaptedCall)
            else -> throw UnsupportedOperationException("Only Observable or Single or Completable or Maybe supported")
        }
    }

}