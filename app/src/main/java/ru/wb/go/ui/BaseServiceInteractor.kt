package ru.wb.go.ui

import io.reactivex.Observable
import ru.wb.go.network.monitor.NetworkState

interface BaseServiceInteractor {

    suspend fun observeNetworkConnected():  NetworkState

    fun versionApp(): String

}