package com.wb.logistics.network.api.app

import com.wb.logistics.network.api.app.response.FlightResponse
import com.wb.logistics.network.api.app.response.FlightStatuses
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Single

class AppRepositoryImpl(
    private val appApi: AppApi,
    private val rxSchedulerFactory: RxSchedulerFactory
) : AppRepository {

    override fun flightStatuses(): Single<FlightStatuses> {
        return appApi.flightStatuses().compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun flight(): Single<FlightResponse> {
        return appApi.flight().compose(rxSchedulerFactory.applySingleSchedulers())
    }

}