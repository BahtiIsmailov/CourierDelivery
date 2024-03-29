package ru.wb.go.ui.couriermap.domain


import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
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


    private val prolongHideSubject = Channel<Unit>()

    private var coroutineScope:CoroutineScope? = null

    init {
        startVisibilityManagerTimer1()
    }

    override fun subscribeMapState(): Flow<CourierMapState> {
        return courierMapRepository.observeMapState()
            .onEach {
                when (it) {
                    CourierMapState.ShowManagerBar -> prolongHideTimerManager() // если клик по карте то отображается плюс и минус справа
                    is CourierMapState.UpdateMarkers -> hideManagerBar()// вызывается каждый раз когда ты нажимаешь на варихаус
                    else -> { }
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
        prolongHideSubject.trySend(Unit)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startVisibilityManagerTimer1() {
        if (coroutineScope == null) {
            coroutineScope = CoroutineScope(SupervisorJob())
            // ссылка на слушателя если запущен то ничего ен делаем а если
            prolongHideSubject.receiveAsFlow()
                .mapLatest {
                    delay(HIDE_DELAY)
                }
                .onEach {
                    hideManagerBar()
                }
                .flowOn(Dispatchers.IO)
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