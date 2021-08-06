package ru.wb.perevozka.ui.flightdeliveriesdetails.domain

import io.reactivex.Single

interface FlightDeliveriesDetailsInteractor {

    fun getUnloadedAndReturnBoxesGroupByOffice(currentOfficeId: Int) : Single<UnloadedAndReturnBoxesGroupByOffice>

}