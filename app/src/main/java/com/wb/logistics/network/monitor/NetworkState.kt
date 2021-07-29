package com.wb.logistics.network.monitor


import io.reactivex.subjects.BehaviorSubject
import kotlin.properties.Delegates

open class NetworkState {

    var isNetworkConnected: Boolean by Delegates.observable(
        false,
        { _, _, newValue -> connect.onNext(if (newValue) Complete else Failed) })

    object Failed : NetworkState()

    object Complete : NetworkState()

    companion object {
        val connect = BehaviorSubject.create<NetworkState>()
    }

}