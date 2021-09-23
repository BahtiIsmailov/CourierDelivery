package ru.wb.perevozka.ui.couriermap.domain

import io.reactivex.Observable
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.ui.couriermap.CourierMapState
import ru.wb.perevozka.ui.scanner.domain.ScannerState

class CourierMapInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val courierMapRepository: CourierMapRepository,
) : CourierMapInteractor {

    override fun subscribeMapState(): Observable<CourierMapState> {
        return courierMapRepository.observeMapState()
    }

    override fun onItemClick(index: String) {
        courierMapRepository.mapAction(index)
    }

}