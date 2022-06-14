package ru.wb.go.ui.couriermap.domain

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapPoint
import java.util.concurrent.TimeUnit

class CourierMapInteractorImpl(
    private val courierMapRepository: CourierMapRepository,
    private val deviceManager: DeviceManager
) : CourierMapInteractor {


    private val prolongHideSubject = MutableSharedFlow<Action>()

    private var hideSplashDisposable: Disposable? = null

//    init {
//        startVisibilityManagerTimer1()
//    }
    override suspend fun subscribeMapState(): Flow<CourierMapState> {
       return courierMapRepository.observeMapState().onEach { // слушает все события с картой
            when (it) {
                CourierMapState.ShowManagerBar -> prolongHideTimerManager() // если клик по карте то отображается плюс и минус справа
                is CourierMapState.UpdateMarkers -> hideManagerBar()// вызывается каждый раз когда ты нажимаешь на варихаус
                else -> {}
            }
        }



    }

//    override fun subscribeMapState(): Observable<CourierMapState> { // слушает все события с картой
//        return courierMapRepository
//            .observeMapState()
//            .doOnNext {
//                when (it) {
//                    CourierMapState.ShowManagerBar -> prolongHideTimerManager() // если клик по карте то отображается плюс и минус справа
//                    is CourierMapState.UpdateMarkers -> hideManagerBar()// вызывается каждый раз когда ты нажимаешь на варихаус
//                    else -> {}
//                }
//            }
//    }

    override suspend fun markerClick(point: MapPoint) {
        courierMapRepository.mapAction(CourierMapAction.ItemClick(point))
    }

    override suspend fun mapClick() {
        courierMapRepository.mapAction(CourierMapAction.MapClick)
    }

    override suspend fun onForcedLocationUpdate(point: CoordinatePoint) {
        deviceManager.saveLocation("${point.latitude}:${point.longitude}")
        courierMapRepository.mapAction(CourierMapAction.LocationUpdate(point))
    }

    override suspend fun showAll() {
        courierMapRepository.mapAction(CourierMapAction.ShowAll)
    }

    override suspend fun animateComplete() {
        courierMapRepository.mapAction(CourierMapAction.AnimateComplete)
    }

    override suspend fun prolongTimeHideManager() {
        prolongHideTimerManager()
    }

    private suspend fun prolongHideTimerManager() {
        prolongHideSubject.emit(Action{ }) // отправляет акшн в рх как стэйт флоу
    }

    override suspend fun startVisibilityManagerTimer1() {
        if (hideSplashDisposable == null) { // ссылка на слушателя если запущен то ничего ен делаем а если
            prolongHideSubject.collect()
            Observable.timer(HIDE_DELAY, TimeUnit.SECONDS)
            hideManagerBar()
        }
    }

    private suspend fun hideManagerBar() {
        courierMapRepository.mapState(CourierMapState.HideManagerBar)
    }

    companion object {
        const val HIDE_DELAY = 5L
    }

}