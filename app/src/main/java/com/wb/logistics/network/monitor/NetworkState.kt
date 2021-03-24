package com.wb.logistics.network.monitor


import io.reactivex.subjects.BehaviorSubject
import kotlin.properties.Delegates

object NetworkState {

    var isNetworkConnected: Boolean by Delegates.observable(
        false,
        { _, _, newValue -> connect.onNext(newValue) })

    var connect = BehaviorSubject.create<Boolean>()

}