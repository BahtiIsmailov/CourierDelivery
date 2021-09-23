package ru.wb.perevozka.ui.couriermap.domain

import io.reactivex.Observable
import ru.wb.perevozka.ui.couriermap.CourierMapState

interface CourierMapInteractor {

    fun subscribeMapState(): Observable<CourierMapState>

    fun onItemClick(index: String)

}