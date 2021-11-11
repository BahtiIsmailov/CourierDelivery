package ru.wb.go.ui.unloadingcongratulation.domain

import io.reactivex.Single

interface CongratulationInteractor {

    fun getDeliveryBoxesGroupByOffice() : Single<DeliveryResult>

}