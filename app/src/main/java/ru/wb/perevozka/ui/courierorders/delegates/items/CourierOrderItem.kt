package ru.wb.perevozka.ui.courierorders.delegates.items

import ru.wb.perevozka.mvvm.model.base.BaseItem

data class CourierOrderItem(
    val order: String,
    val volume: String,
    val pvzCount: String,
    val coast: String,
) : BaseItem {

    private var _idView: Int = 0

    override var idView: Int
        get() = _idView
        set(value) {
            _idView = value
        }

}
