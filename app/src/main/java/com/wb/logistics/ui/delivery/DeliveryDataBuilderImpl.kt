package com.wb.logistics.ui.delivery

import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.delivery.res.DeliveryResourceProvider

class DeliveryDataBuilderImpl(private val resourceProvider: DeliveryResourceProvider) :
    DeliveryDataBuilder {
    override fun buildFlights(): List<BaseItem> {
        return ArrayList()
    }
}