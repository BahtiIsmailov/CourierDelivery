package ru.wb.go.ui.couriermap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.couriermap.domain.CourierMapInteractor
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapPoint

class CourierMapViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierMapInteractor,
    private val resourceProvider: CourierMapResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    class Point(val x: Double, val y: Double)

    private val points = listOf(
        Point(0.01, 14.6),
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

    private fun subscribeMapState() {
        addSubscription(
            interactor.subscribeMapState().subscribe(
                {
                    when (it) {
                        is CourierMapState.NavigateToMarker -> _mapState.value = it
                        is CourierMapState.NavigateToPoint -> _mapState.value = it
                        is CourierMapState.UpdateMarkers -> _mapState.value = it
                        is CourierMapState.NavigateToPointByZoomRadius -> _mapState.value = it
                        CourierMapState.NavigateToMyLocation -> _mapState.value = it
                        is CourierMapState.UpdateAndNavigateToMyLocationPoint -> _mapState.value = it
                        CourierMapState.UpdateMyLocation -> _mapState.value = it
                        is CourierMapState.ZoomToCenterBoundingBox -> _mapState.value = it
                    }
                },
                {
                    LogUtils { logDebugApp("subscribeMapState() error " + it) }
                })
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
        LogUtils { logDebugApp("CourierMapViewModel onInitPermission()") }
        subscribeMapState()
        interactor.onInitPermission()
    }

    fun onItemClick(index: String) {
        interactor.onItemClick(index)
    }

    fun onForcedLocationUpdate(point: CoordinatePoint) {
        interactor.onForcedLocationUpdate(point)
    }

}

fun moscowMapPoint() = MapPoint("0", 55.751244, 37.618423)
//fun moscowMapPoint() = MapPoint("0", 0.01, 37.618423)

//myLocation CoordinatePoint(latitude=55.7618109, longitude=37.7777639)

//fun testMapPoint0() = MapPoint("0", 58.7618109, 37.8777639) //37.313508 58.7617864, 30.313508
fun testMapPoint0() = MapPoint("0", 59.7617864, 37.313508) //37.313508
fun testMapPoint1() = MapPoint("1", 55.7617864, 73.498648) //37.842315
//56.524096, 73.498648

fun myCoordinatePoint() = CoordinatePoint(55.759958, 37.852315)