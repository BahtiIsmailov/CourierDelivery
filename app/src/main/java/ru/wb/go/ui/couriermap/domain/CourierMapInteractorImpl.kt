package ru.wb.go.ui.couriermap.domain


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapPoint

class CourierMapInteractorImpl(
    private val courierMapRepository: CourierMapRepository,
    private val deviceManager: DeviceManager
) : CourierMapInteractor {


    private val prolongHideSubject = MutableSharedFlow<Unit>(
        extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var coroutineScope:CoroutineScope? = null

    init {
        startVisibilityManagerTimer1()
    }

    override fun subscribeMapState(): Flow<CourierMapState> {
        return courierMapRepository.observeMapState()
            .onEach { // слушает все события с картой
                when (it) {
                    CourierMapState.ShowManagerBar -> prolongHideTimerManager() // если клик по карте то отображается плюс и минус справа
                    is CourierMapState.UpdateMarkers -> hideManagerBar()// вызывается каждый раз когда ты нажимаешь на варихаус
                    else -> {}
                }
            }
    }


    override fun markerClick(point: MapPoint) {
        courierMapRepository.mapAction(CourierMapAction.ItemClick(point))
    }

    override fun mapClick() {
        courierMapRepository.mapAction(CourierMapAction.MapClick)
    }

    override fun onForcedLocationUpdate(point: CoordinatePoint) {
        deviceManager.saveLocation("${point.latitude}:${point.longitude}")
        courierMapRepository.mapAction(CourierMapAction.LocationUpdate(point))
    }

    override fun showAll() {
        courierMapRepository.mapAction(CourierMapAction.ShowAll)
    }


    override fun animateComplete() {
        courierMapRepository.mapAction(CourierMapAction.AnimateComplete)
    }

    override fun prolongTimeHideManager() {
        prolongHideTimerManager()
    }



    private fun prolongHideTimerManager() {
        prolongHideSubject.tryEmit(Unit) // отправляет акшн в рх как стэйт флоу
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startVisibilityManagerTimer1() {
        if (coroutineScope == null) {
            coroutineScope = CoroutineScope(SupervisorJob())
            // ссылка на слушателя если запущен то ничего ен делаем а если
            prolongHideSubject
                .mapLatest {
                    delay(HIDE_DELAY)
                }
                .onEach {
                    hideManagerBar()
                }
                .launchIn(coroutineScope!!)
        }

    }

    private fun hideManagerBar() {
        courierMapRepository.mapState(CourierMapState.HideManagerBar)
    }



    companion object {
        const val HIDE_DELAY = 5000L
    }

}