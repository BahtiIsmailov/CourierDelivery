package ru.wb.go.db

interface IntransitTimeRepository {

    fun startTimer() :Long
    fun stopTimer()

}