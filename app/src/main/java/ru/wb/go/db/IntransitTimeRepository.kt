package ru.wb.go.db

import io.reactivex.Flowable

interface IntransitTimeRepository {

    fun startTimer(): Flowable<Long>
    fun stopTimer()

}