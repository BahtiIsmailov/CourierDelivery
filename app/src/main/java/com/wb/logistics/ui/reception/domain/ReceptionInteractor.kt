package com.wb.logistics.ui.reception.domain

import io.reactivex.Observable

interface ReceptionInteractor {

    fun saveBoxCode(code: String, address : String)

    fun changeBoxes(): Observable<List<ReceptionBoxEntity>>

    fun removeBoxes(checkedBoxes: List<Boolean>)

}