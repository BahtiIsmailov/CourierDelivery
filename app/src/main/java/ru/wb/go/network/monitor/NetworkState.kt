package ru.wb.go.network.monitor


import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.properties.Delegates

open class NetworkState {

    var isNetworkConnected: Boolean by Delegates.observable(
        false
    ) { _, _, newValue -> if (newValue) Complete else Failed }

    object Failed : NetworkState()

    object Complete : NetworkState()

    companion object {
        val connect = MutableSharedFlow<NetworkState>()
    }

}