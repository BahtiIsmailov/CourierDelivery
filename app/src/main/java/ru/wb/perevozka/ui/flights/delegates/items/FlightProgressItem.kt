package ru.wb.perevozka.ui.flights.delegates.items

import ru.wb.perevozka.mvvm.model.base.BaseItem

class FlightProgressItem : BaseItem {

    private var _idView: Int = 0

    override var idView: Int
        get() = _idView
        set(value) {
            _idView = value
        }

}
