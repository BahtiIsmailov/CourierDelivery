package com.wb.logistics.network.rx

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface ErrorResolutionStrategy {
    fun apply(call: Observable<*>): Observable<*>
    fun apply(call: Single<*>): Single<*>
    fun apply(call: Completable): Completable
}