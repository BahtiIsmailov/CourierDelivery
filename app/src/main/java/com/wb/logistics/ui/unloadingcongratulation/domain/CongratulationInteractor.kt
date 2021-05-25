package com.wb.logistics.ui.unloadingcongratulation.domain

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxResultEntity
import io.reactivex.Single

interface CongratulationInteractor {

    fun groupAttachedBox(): Single<AttachedBoxResultEntity>

}