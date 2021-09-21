package ru.wb.perevozka.db

import io.reactivex.Flowable

interface IntransitTimeRepository {

    fun startTimer(): Flowable<Long>
    fun stopTimer()

}