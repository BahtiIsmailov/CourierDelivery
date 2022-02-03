package ru.wb.go.ui.couriermap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.couriermap.domain.CourierMapInteractor
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapPoint

class CourierMapViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierMapInteractor,
    private val resourceProvider: CourierMapResourceProvider,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _mapState = MutableLiveData<CourierMapState>()
    val mapState: LiveData<CourierMapState>
        get() = _mapState

    fun onInitPermission() {
        subscribeMapState()
        interactor.onInitPermission()
    }

    fun onDeniedPermission() {
        subscribeMapState()
        interactor.onDeniedPermission(moscowCoordinatePoint())
    }

    private fun subscribeMapState() {
        addSubscription(
            interactor.subscribeMapState().subscribe(
                { _mapState.value = it },
                { LogUtils { logDebugApp("subscribeMapState() error " + it) } }
            )
        )
    }

    fun onItemClick(point: MapPoint) {
        interactor.onItemClick(point)
    }

    fun onMapClick() {
        interactor.onMapClick()
    }

    fun onForcedLocationUpdate(point: CoordinatePoint) {
        interactor.onForcedLocationUpdate(point)
    }

    fun onForcedLocationUpdateDefault() {
        interactor.onForcedLocationUpdate(moscowCoordinatePoint())
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierMap"
    }

}

fun moscowMapPoint() = MapPoint("0", 55.751244, 37.618423)

fun moscowCoordinatePoint() = CoordinatePoint(55.751244, 37.618423)
//fun moscowMapPoint() = MapPoint("0", 0.01, 37.618423)

//myLocation CoordinatePoint(latitude=55.7618109, longitude=37.7777639)

//fun testMapPoint0() = MapPoint("0", 58.7618109, 37.8777639) //37.313508 58.7617864, 30.313508
fun testMapPoint0() = MapPoint("0", 59.7617864, 37.313508) //37.313508
fun testMapPoint1() = MapPoint("1", 55.7617864, 73.498648) //37.842315
//56.524096, 73.498648

fun myCoordinatePoint() = CoordinatePoint(55.759958, 37.852315)