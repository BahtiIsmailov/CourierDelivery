package com.wb.logistics.ui.reception.domain

import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


class ReceptionInteractorImpl(private val rxSchedulerFactory: RxSchedulerFactory) :
    ReceptionInteractor {

    private val listBehaviorSubject = BehaviorSubject.create<List<ReceptionBoxEntity>>()
    private val boxes = mutableListOf<ReceptionBoxEntity>()

    override fun saveBoxCode(code: String, address : String) {
        boxes.add(ReceptionBoxEntity(code + boxes.size, address))
        listBehaviorSubject.onNext(boxes)
    }

    override fun changeBoxes(): Observable<List<ReceptionBoxEntity>> {
        return listBehaviorSubject.compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun removeBoxes(checkedBoxes: List<Boolean>) {
        for (i in checkedBoxes.indices.reversed()) {
            if (checkedBoxes[i]) boxes.removeAt(i)
        }
        listBehaviorSubject.onNext(boxes)
    }

}