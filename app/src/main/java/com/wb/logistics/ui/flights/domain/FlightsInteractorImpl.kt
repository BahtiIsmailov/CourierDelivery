package com.wb.logistics.ui.flights.domain

import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.network.api.app.AppRepository
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.ui.flights.delegates.items.FlightItem
import io.reactivex.Single

class FlightsInteractorImpl(
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val repository: AppRepository
) : FlightsInteractor {


    override fun flight(): Single<List<BaseItem>> {
        return repository.flight()
            .map {
                val list = mutableListOf<BaseItem>()
                if (it.flight != null) {
                    list.addAll(getMockFlights())
                }
                list
            }
    }

    private fun getMockFlights(): List<BaseItem> {
        val data = mutableListOf<BaseItem>()
        (0..3).forEach { _ ->
            val address = mutableListOf<String>()
            (0..5).forEach { _ -> address.add("ПВЗ Москва, ул. Карамазова, 32/3") }
            data.add(
                FlightItem(
                    "Рейс № 31324",
                    "№23",
                    "23.09.20",
                    "10:00",
                    "Маршрут № 4 «Подольск север»",
                    address
                )
            )
        }
        return data
    }

}