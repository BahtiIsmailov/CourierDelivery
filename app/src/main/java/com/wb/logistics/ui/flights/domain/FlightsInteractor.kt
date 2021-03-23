package com.wb.logistics.ui.flights.domain

import com.wb.logistics.mvvm.model.base.BaseItem
import io.reactivex.Single

interface FlightsInteractor {
    fun flight(): Single<List<BaseItem>>
}