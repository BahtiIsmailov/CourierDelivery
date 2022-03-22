package ru.wb.go.ui.courierdataexpects.domain

import io.reactivex.Completable
import io.reactivex.Single

interface CourierDataExpectsInteractor {

    fun saveRepeatCourierDocuments(): Completable

    fun isRegisteredStatus(): Single<String>

}