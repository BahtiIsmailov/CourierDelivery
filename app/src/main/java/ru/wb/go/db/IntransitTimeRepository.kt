package ru.wb.go.db

import kotlinx.coroutines.flow.Flow

interface IntransitTimeRepository {

    fun startTimer() : Flow<Long>
    fun stopTimer()

}