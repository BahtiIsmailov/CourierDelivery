package ru.wb.go.ui.dcunloadingcongratulation.domain

import ru.wb.go.db.entity.dcunloadedboxes.DcCongratulationEntity
import io.reactivex.Single

interface DcUnloadingCongratulationInteractor {

    fun congratulation(): Single<DcCongratulationEntity>

}