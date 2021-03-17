package com.wb.logistics.ui.auth.domain

import com.jakewharton.rxbinding3.InitialValueObservable
import io.reactivex.Completable
import io.reactivex.Observable

interface CreatePasswordInteractor {
    fun remindPasswordChanges(observable: InitialValueObservable<CharSequence>): Observable<Boolean>
    fun saveAndAuthByPassword(phone: String, password: String, tmpPassword: String): Completable
}