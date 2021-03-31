package com.wb.logistics.ui.reception.domain

import com.wb.logistics.network.api.BoxesRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Observable


class ReceptionInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val boxesRepository: BoxesRepository,
) :
    ReceptionInteractor {

    override fun saveBoxCode(code: String, address: String) {
        boxesRepository.saveBoxCode(code, address)
    }

    override fun changeBoxes(): Observable<List<ReceptionBoxEntity>> {
        return boxesRepository.changeBoxes().compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun removeBoxes(checkedBoxes: List<Boolean>) {
        boxesRepository.removeBoxes(checkedBoxes)
    }

}