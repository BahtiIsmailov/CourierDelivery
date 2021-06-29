package com.wb.logistics.ui.dcunloadingcongratulation.domain

import com.wb.logistics.db.entity.dcunloadedboxes.DcCongratulationEntity
import io.reactivex.Single

interface DcUnloadingCongratulationInteractor {

    fun congratulation(): Single<DcCongratulationEntity>

}