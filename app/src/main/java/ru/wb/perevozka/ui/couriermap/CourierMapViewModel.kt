package ru.wb.perevozka.ui.couriermap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.couriermap.domain.CourierMapInteractor
import ru.wb.perevozka.utils.LogUtils
import ru.wb.perevozka.utils.map.MapCircle
import ru.wb.perevozka.utils.map.MapPoint

class CourierMapViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierMapInteractor,
    private val resourceProvider: CourierMapResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    class Point(val x: Double, val y: Double)

    private val points = listOf(
        Point(0.01, 15.5),
        Point(0.05, 12.5),
        Point(0.1, 12.0),
        Point(0.5, 9.0),
        Point(1.0, 8.0),
        Point(1.5, 7.0),
        Point(2.0, 6.8),
        Point(3.0, 6.3),
        Point(6.0, 5.4),
        Point(80.0, 1.8)
    )

    private val _mapState = MutableLiveData<CourierMapState>()
    val mapState: LiveData<CourierMapState>
        get() = _mapState

    init {
        subscribeMapState()
    }

    private fun subscribeMapState() {
        addSubscription(
            interactor.subscribeMapState().subscribe(
                {
                    when (it) {
                        is CourierMapState.NavigateToMarker -> _mapState.value = it
                        is CourierMapState.NavigateToPoint -> _mapState.value = it
                        is CourierMapState.UpdateMapMarkers -> _mapState.value = it
                        is CourierMapState.ZoomAllMarkers -> {
                            val zoomRadius = calculateZoom(it.startNavigation.radius)
                            val startNavigation: MapCircle =
                                it.startNavigation.copy(radius = zoomRadius)
                            _mapState.value = CourierMapState.ZoomAllMarkers(startNavigation)
                        }
                    }
                }, {})
        )
    }

    private fun calculateZoom(fx: Double): Double {
        var p1: Point = points.first()
        var p2: Point = points.last()
        var lastPoint = Point(0.0, 16.0)
        points.sortedBy { it.x }.forEach { point ->
            if (fx >= lastPoint.x && fx <= point.x) {
                LogUtils { logDebugApp("points find forEach fx " + fx + " / " + lastPoint.x.toString() + " / " + point.x.toString()) }
                p1 = lastPoint
                p2 = point
                return zoom(p1, p2, fx)
            }
            lastPoint = point
        }
        return zoom(p1, p2, fx)
    }

    private fun zoom(
        p1: Point,
        p2: Point,
        fx: Double
    ): Double {
        LogUtils { logDebugApp("points find " + p1.x.toString() + " " + p1.y.toString() + " " + p2.x.toString() + " " + p2.y.toString()) }
        val x1 = p1.x
        val y1 = p1.y
        val x2 = p2.x
        val y2 = p2.y
        return y1 + (y2 - y1) * (fx - x1) / (x2 - x1)
    }

    fun onInitPermission() {
        interactor.onInitPermission()
    }

    fun onItemClick(index: String) {
        interactor.onItemClick(index)
    }

}

fun moscowMapPoint() = MapPoint("0", 55.751244, 37.618423)
//fun moscowMapPoint() = MapPoint("0", 0.01, 37.618423)

//fun testMapPoint() = MapPoint("1", 55.755244, 37.618423)
//fun testMapPoint() = MapPoint("1", -0.01, 37.618423)