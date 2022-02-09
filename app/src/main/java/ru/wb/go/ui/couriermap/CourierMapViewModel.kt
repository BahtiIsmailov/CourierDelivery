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
) : NetworkViewModel(compositeDisposable, metric) {

    private val _mapState = MutableLiveData<CourierMapState>()
    val mapState: LiveData<CourierMapState>
        get() = _mapState

    init {
        subscribeMapState()
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

fun moscowCoordinatePoint() = CoordinatePoint(55.751244, 37.618423)