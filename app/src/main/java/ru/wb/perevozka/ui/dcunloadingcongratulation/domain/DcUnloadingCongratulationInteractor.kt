package ru.wb.perevozka.ui.dcunloadingcongratulation.domain

import ru.wb.perevozka.db.entity.dcunloadedboxes.DcCongratulationEntity
import io.reactivex.Single

interface DcUnloadingCongratulationInteractor {

    fun congratulation(): Single<DcCongratulationEntity>

}