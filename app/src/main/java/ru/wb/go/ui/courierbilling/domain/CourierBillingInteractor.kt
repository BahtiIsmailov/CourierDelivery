package ru.wb.go.ui.courierbilling.domain

import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.network.api.app.entity.BillingCommonEntity
import ru.wb.go.network.monitor.NetworkState

interface CourierBillingInteractor {

    fun billing(): Single<BillingCommonEntity>



    fun observeNetworkConnected(): Observable<NetworkState>

}