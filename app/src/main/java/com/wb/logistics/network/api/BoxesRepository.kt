package com.wb.logistics.network.api

import com.wb.logistics.ui.reception.domain.ReceptionBoxEntity
import io.reactivex.Observable

interface BoxesRepository {

    fun saveBoxCode(code: String, address : String)

    fun changeBoxes(): Observable<List<ReceptionBoxEntity>>

    fun removeBoxes(checkedBoxes: List<Boolean>)

    fun removeBoxes()

}