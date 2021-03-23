package com.wb.logistics.ui.flights.delegates.items

import com.wb.logistics.mvvm.model.base.BaseItem

class FlightProgressItem : BaseItem {

    private var _idView: Int = 0

    override var idView: Int
        get() = _idView
        set(value) {
            _idView = value
        }

}
