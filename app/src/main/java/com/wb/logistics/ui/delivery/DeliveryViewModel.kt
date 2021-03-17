package com.wb.logistics.ui.delivery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.delivery.delegates.items.RouteEmptyItem
import com.wb.logistics.ui.delivery.delegates.items.RouteItem
import com.wb.logistics.ui.res.AppResourceProvider

class DeliveryViewModel(
    private val resourceProvider: AppResourceProvider //private val deliveryRepository: DeliveryRepository,
) : ViewModel() {

    private val _flights = MutableLiveData<List<BaseItem>>()
    val flights: LiveData<List<BaseItem>>
        get() = _flights

    private val _visibleStartAddingBoxes = MutableLiveData<Boolean>()
    val visibleStartAddingBoxes: LiveData<Boolean>
        get() = _visibleStartAddingBoxes

    fun fetchFlights() {
        _flights.value = getMockEmptyFlights()
        _visibleStartAddingBoxes.value = false
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
        val flights = getMockFlights()
        if (flights.isEmpty()) {
            _visibleStartAddingBoxes.value = false
        } else {
            _visibleStartAddingBoxes.value = true
            _flights.value = flights
        }
    }

}