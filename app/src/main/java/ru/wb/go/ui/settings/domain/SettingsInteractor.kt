package ru.wb.go.ui.settings.domain

import io.reactivex.Observable
import ru.wb.go.network.monitor.NetworkState

interface SettingsInteractor {
    suspend fun observeNetworkConnected():  NetworkState
}