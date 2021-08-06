package ru.wb.perevozka.ui.flightdeliveriesdetails

import ru.wb.perevozka.mvvm.model.base.BaseItem
import ru.wb.perevozka.ui.flightdeliveriesdetails.domain.UnloadedAndReturnBoxesGroupByOffice

interface FlightDeliveriesDetailsDataBuilder {
    fun buildItem(value: UnloadedAndReturnBoxesGroupByOffice): List<BaseItem>
}