package com.wb.logistics.network.api

import com.wb.logistics.ui.reception.domain.ReceptionBoxEntity
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class BoxesRepositoryImpl() : BoxesRepository {

    private val listBehaviorSubject = BehaviorSubject.create<List<ReceptionBoxEntity>>()
    private val boxes = mutableListOf<ReceptionBoxEntity>()

    override fun saveBoxCode(code: String, address: String) {
        boxes.add(ReceptionBoxEntity(code + boxes.size, address))
        listBehaviorSubject.onNext(boxes)
    }

    override fun changeBoxes(): Observable<List<ReceptionBoxEntity>> {
        return listBehaviorSubject
    }

    override fun removeBoxes(checkedBoxes: List<Boolean>) {
        for (i in checkedBoxes.indices.reversed()) {
            if (checkedBoxes[i]) boxes.removeAt(i)
        }
        listBehaviorSubject.onNext(boxes)
    }

    override fun removeBoxes() {
        boxes.clear()
        listBehaviorSubject.onNext(boxes)
    }

}