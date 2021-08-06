package ru.wb.perevozka.ui.unloadingcongratulation.domain

import io.reactivex.Single

interface CongratulationInteractor {

    fun getDeliveryBoxesGroupByOffice() : Single<DeliveryResult>

}