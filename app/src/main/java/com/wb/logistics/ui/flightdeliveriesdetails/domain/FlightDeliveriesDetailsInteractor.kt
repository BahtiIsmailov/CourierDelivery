package com.wb.logistics.ui.flightdeliveriesdetails.domain

import io.reactivex.Single

interface FlightDeliveriesDetailsInteractor {

    fun getUnloadedAndReturnBoxesGroupByOffice(dstOfficeId: Int) : Single<UnloadedAndReturnBoxesGroupByOffice>

}