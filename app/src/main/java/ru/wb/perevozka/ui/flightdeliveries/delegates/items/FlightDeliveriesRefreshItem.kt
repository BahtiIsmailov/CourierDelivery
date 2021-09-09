package ru.wb.perevozka.ui.flightdeliveries.delegates.items

import ru.wb.perevozka.mvvm.model.base.BaseItem

data class FlightDeliveriesRefreshItem(
    val message: String
) : BaseItem {

    private var _idView: Int = 0

    override var idView: Int
        get() = _idView
        set(value) {
            _idView = value
        }

}