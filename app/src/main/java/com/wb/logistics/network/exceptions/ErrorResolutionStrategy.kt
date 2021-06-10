package com.wb.logistics.network.exceptions

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

interface ErrorResolutionStrategy {
    fun apply(call: Observable<*>): Observable<*>
    fun apply(call: Single<*>): Single<*>
    fun apply(call: Maybe<*>): Maybe<*>
    fun apply(call: Completable): Completable
}