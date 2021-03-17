package com.wb.logistics.ui.auth.domain

import com.jakewharton.rxbinding3.InitialValueObservable
import io.reactivex.Completable
import io.reactivex.Observable

interface InputPasswordInteractor {
    fun remindPasswordChanges(observable: InitialValueObservable<CharSequence>): Observable<Boolean>
    fun authByPassword(phone: String, password: String): Completable
}