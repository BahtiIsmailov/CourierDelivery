package com.wb.logistics.ui.flightdeliveriesdetails.domain

import io.reactivex.Single

interface FlightDeliveriesDetailsInteractor {

    fun getUnloadedAndReturnBoxesGroupByOffice(currentOfficeId: Int) : Single<UnloadedAndReturnBoxesGroupByOffice>

}