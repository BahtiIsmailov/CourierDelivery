package ru.wb.go.ui.flightdeliveries.delegates.items

import ru.wb.go.mvvm.model.base.BaseItem

class FlightDeliveriesProgressItem : BaseItem {

    private var _idView: Int = 0

    override var idView: Int
        get() = _idView
        set(value) {
            _idView = value
        }

}
