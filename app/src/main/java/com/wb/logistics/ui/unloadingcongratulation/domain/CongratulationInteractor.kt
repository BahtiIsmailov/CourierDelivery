package com.wb.logistics.ui.unloadingcongratulation.domain

import com.wb.logistics.db.entity.attachedboxes.DeliveryBoxGroupByOfficeEntity
import io.reactivex.Single

interface CongratulationInteractor {

    fun getDeliveryBoxesGroupByOffice() : Single<List<DeliveryBoxGroupByOfficeEntity>>

}