package com.wb.logistics.ui.delivery

import com.wb.logistics.mvvm.model.base.BaseItem

interface DeliveryDataBuilder {
    fun buildFlights(): List<BaseItem?>
}