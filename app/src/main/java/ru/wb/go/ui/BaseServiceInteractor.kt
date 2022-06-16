package ru.wb.go.ui

import ru.wb.go.network.monitor.NetworkState

interface BaseServiceInteractor {

    suspend fun observeNetworkConnected():  NetworkState

    fun versionApp(): String

}