package ru.wb.perevozka.ui.courierintransit.domain

import io.reactivex.Flowable
import ru.wb.perevozka.db.entity.courierboxes.CourierIntransitGroupByOfficeEntity

interface CourierIntransitInteractor {

    fun observeBoxesGroupByOrder(): Flowable<List<CourierIntransitGroupByOfficeEntity>>

}