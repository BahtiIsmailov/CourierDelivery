package com.wb.logistics.ui.unloadingcongratulation.domain

import io.reactivex.Single

interface CongratulationInteractor {

    fun getDeliveryBoxesGroupByOffice() : Single<DeliveryResult>

}