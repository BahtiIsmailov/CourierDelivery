package com.wb.logistics.ui.delivery.delegates.items

import com.wb.logistics.mvvm.model.base.BaseItem

data class RouteEmptyItem(
    val message: String
) : BaseItem {

    private var _idView: Int = 0

    override var idView: Int
        get() = _idView
        set(value) {
            _idView = value
        }

}
