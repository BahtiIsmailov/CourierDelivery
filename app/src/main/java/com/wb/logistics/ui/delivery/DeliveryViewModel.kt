package com.wb.logistics.ui.delivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wb.logistics.mvp.model.base.BaseItem
import com.wb.logistics.ui.delivery.data.DeliveryRepository
import com.wb.logistics.ui.delivery.delegates.items.RouteEmptyItem
import com.wb.logistics.ui.delivery.delegates.items.RouteItem
import com.wb.logistics.ui.res.ResourceProvider

class DeliveryViewModel(
    private val deliveryRepository: DeliveryRepository,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _flights = MutableLiveData<List<BaseItem>>()
    val flights: LiveData<List<BaseItem>>
        get() = _flights

    fun fetchFlights() {
        _flights.value = getMockEmptyFlights()
    }

    private fun getMockEmptyFlights(): List<BaseItem> {
        val data = mutableListOf<BaseItem>()
        data.add(RouteEmptyItem(resourceProvider.getEmptyFlight()))
        return data
    }

    private fun getMockFlights(): List<BaseItem> {
        val data = mutableListOf<BaseItem>()
        (0..3).forEach { _ ->
            val address = mutableListOf<String>()
            (0..5).forEach { _ -> address.add("ПВЗ Москва, ул. Карамазова, 32/3") }
            data.add(
                RouteItem(
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

    fun updateScreenClick() {
        _flights.value = getMockFlights()
    }

}