package ru.wb.go.ui.couriermap

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
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

    object ClearMap

    private val _clearMap = SingleLiveEvent<ClearMap>()
    val clearMap: LiveData<ClearMap>
        get() = _clearMap

    data class ZoomToBoundingBoxOffsetY(
        val boundingBox: BoundingBox,
        val animate: Boolean,
        val offsetY: Int
    )

    private val _zoomToBoundingBoxOffsetY = SingleLiveEvent<ZoomToBoundingBoxOffsetY>()
    val zoomToBoundingBoxOffsetY: LiveData<ZoomToBoundingBoxOffsetY>
        get() = _zoomToBoundingBoxOffsetY

    data class UpdateMarkersWithAnimateToPositions(
        val pointsHide: List<CourierMapMarker>,
        val pointFrom: CourierMapMarker,
        val pointsTo: List<CourierMapMarker>,
        val animateTo: BoundingBox,
        val offsetY: Int
    )

    private val _updateMarkersWithAnimateToPositions =
        SingleLiveEvent<UpdateMarkersWithAnimateToPositions>()
    val updateMarkersWithAnimateToPositions: LiveData<UpdateMarkersWithAnimateToPositions>
        get() = _updateMarkersWithAnimateToPositions

    data class UpdateMarkersWithAnimateToPosition(
        val pointsShow: List<CourierMapMarker>,
        val pointsFrom: List<CourierMapMarker>,
        val pointTo: CourierMapMarker,
        val animateTo: BoundingBox,
        val offsetY: Int
    )

    private val _updateMarkersWithAnimateToPosition =
        SingleLiveEvent<UpdateMarkersWithAnimateToPosition>()
    val updateMarkersWithAnimateToPosition: LiveData<UpdateMarkersWithAnimateToPosition>
        get() = _updateMarkersWithAnimateToPosition

    data class NavigateToPoint(val point: CoordinatePoint)

    private val _navigateToPoint = SingleLiveEvent<NavigateToPoint>()
    val navigateToPoint: LiveData<NavigateToPoint>
        get() = _navigateToPoint

    data class NavigateToMarker(val id: String)

    private val _navigateToMarker = SingleLiveEvent<NavigateToMarker>()
    val navigateToMarker: LiveData<NavigateToMarker>
        get() = _navigateToMarker

    data class NavigateToPointZoom(val point: CoordinatePoint)

    private val _navigateToPointZoom = SingleLiveEvent<NavigateToPointZoom>()
    val navigateToPointZoom: LiveData<NavigateToPointZoom>
        get() = _navigateToPointZoom

    object NavigateToMyLocation

    private val _navigateToMyLocation = SingleLiveEvent<NavigateToMyLocation>()
    val navigateToMyLocation: LiveData<NavigateToMyLocation>
        get() = _navigateToMyLocation

    data class UpdateMarkers(val points: List<CourierMapMarker>)

    private val _updateMarkers = SingleLiveEvent<UpdateMarkers>()
    val updateMarkers: LiveData<UpdateMarkers>
        get() = _updateMarkers

    data class UpdateMarkersWithIndex(val points: List<CourierMapMarker>)

    private val _updateMarkersWithIndex = SingleLiveEvent<UpdateMarkersWithIndex>()
    val updateMarkersWithIndex: LiveData<UpdateMarkersWithIndex>
        get() = _updateMarkersWithIndex

    object UpdateMyLocation

    private val _updateMyLocation = SingleLiveEvent<UpdateMyLocation>()
    val updateMyLocation: LiveData<UpdateMyLocation>
        get() = _updateMyLocation

    data class UpdateMyLocationPoint(val point: CoordinatePoint)

    private val _updateMyLocationPoint = SingleLiveEvent<UpdateMyLocationPoint>()
    val updateMyLocationPoint: LiveData<UpdateMyLocationPoint>
        get() = _updateMyLocationPoint

    data class ZoomToBoundingBox(val boundingBox: BoundingBox, val animate: Boolean)

    private val _zoomToBoundingBox = SingleLiveEvent<ZoomToBoundingBox>()
    val zoomToBoundingBox: LiveData<ZoomToBoundingBox>
        get() = _zoomToBoundingBox

    private val _visibleManagerBar = SingleLiveEvent<CourierVisibilityManagerBar>()
    val visibleManagerBar: LiveData<CourierVisibilityManagerBar>
        get() = _visibleManagerBar

    fun subscribeState() {
        subscribeMapState()
    }

    private fun subscribeMapState() {
        viewModelScope.launch {
            try {
                interactor.subscribeMapState()
                    .onEach {
                        subscribeMapStateComplete(it)
                    }
                    .collect()

            } catch (e: Exception) {
                LogUtils {
                    logDebugApp("subscribeMapState() error $e")
                }
            }
        }
    }

    private fun subscribeMapStateComplete(it: CourierMapState) {
        when (it) {
            is CourierMapState.NavigateToPoint -> _navigateToPoint.postValue(
                NavigateToPoint(it.point))
            is CourierMapState.UpdateMarkersWithAnimateToPositions -> _updateMarkersWithAnimateToPositions.postValue(
                UpdateMarkersWithAnimateToPositions(
                    it.pointsHide,
                    it.pointFrom,
                    it.pointsTo,
                    it.animateTo,
                    it.offsetY
                ))
            is CourierMapState.ZoomToBoundingBoxOffsetY -> _zoomToBoundingBoxOffsetY.postValue(
                ZoomToBoundingBoxOffsetY(it.boundingBox, it.animate, it.offsetY))
            is CourierMapState.NavigateToMarker -> _navigateToMarker.postValue(
                NavigateToMarker(it.id))
            is CourierMapState.NavigateToPointZoom -> _navigateToPointZoom.postValue(
                NavigateToPointZoom(it.point))
            CourierMapState.NavigateToMyLocation -> _navigateToMyLocation.postValue(
                NavigateToMyLocation)
            is CourierMapState.UpdateMarkers -> _updateMarkers.postValue(
                UpdateMarkers(it.points))
            is CourierMapState.UpdateMarkersWithIndex -> _updateMarkersWithIndex.postValue(
                UpdateMarkersWithIndex(it.points))
            CourierMapState.UpdateMyLocation -> _updateMyLocation.postValue(
                UpdateMyLocation)
            is CourierMapState.UpdateMyLocationPoint -> _updateMyLocationPoint.postValue(
                UpdateMyLocationPoint(it.point))
            is CourierMapState.ZoomToBoundingBox -> _zoomToBoundingBox.postValue(
                ZoomToBoundingBox(it.boundingBox, it.animate))
            is CourierMapState.UpdateMarkersWithAnimateToPosition -> _updateMarkersWithAnimateToPosition.postValue(
                UpdateMarkersWithAnimateToPosition(
                    it.pointsShow,
                    it.pointsFrom,
                    it.pointTo,
                    it.animateTo,
                    it.offsetY
                ))
            CourierMapState.ClearMap -> _clearMap.postValue( ClearMap)
            CourierMapState.ShowManagerBar -> _visibleManagerBar.postValue(
                CourierVisibilityManagerBar.Visible
            )
            CourierMapState.HideManagerBar -> _visibleManagerBar.postValue(
                CourierVisibilityManagerBar.Hide
            )
        }
    }

    fun onItemClick(point: MapPoint) {
        viewModelScope.launch {
            interactor.markerClick(point)
        }
    }

    fun onMapClick() {
        viewModelScope.launch {
            interactor.mapClick()
        }

    }

    fun onForcedLocationUpdate(point: CoordinatePoint) {
        viewModelScope.launch {
            interactor.onForcedLocationUpdate(point)
        }

    }

    fun onForcedLocationUpdateDefault() {
        viewModelScope.launch {
            interactor.onForcedLocationUpdate(moscowCoordinatePoint())
        }

    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    fun onZoomClick() {
        viewModelScope.launch {
            interactor.prolongTimeHideManager()
        }

    }

    fun onShowAllClick() {
        viewModelScope.launch {
            interactor.prolongTimeHideManager()
            interactor.showAll()
        }

    }

    fun onAnimateComplete() {
        viewModelScope.launch {
            interactor.animateComplete()
        }
    }

    companion object {
        const val SCREEN_TAG = "CourierMap"
    }

}

fun moscowCoordinatePoint() = CoordinatePoint(55.751244, 37.618423)


/*
object ClearMap

    private val _clearMap = SingleLiveEvent<ClearMap>()
    val clearMap: LiveData<ClearMap>
        get() = _clearMap

    data class ZoomToBoundingBoxOffsetY(
        val boundingBox: BoundingBox,
        val animate: Boolean,
        val offsetY: Int
    )

    private val _zoomToBoundingBoxOffsetY = SingleLiveEvent<ZoomToBoundingBoxOffsetY>()
    val zoomToBoundingBoxOffsetY: LiveData<ZoomToBoundingBoxOffsetY>
        get() = _zoomToBoundingBoxOffsetY

    data class UpdateMarkersWithAnimateToPositions(
        val pointsHide: List<CourierMapMarker>,
        val pointFrom: CourierMapMarker,
        val pointsTo: List<CourierMapMarker>,
        val animateTo: BoundingBox,
        val offsetY: Int
    )

    private val _updateMarkersWithAnimateToPositions =
        SingleLiveEvent<UpdateMarkersWithAnimateToPositions>()
    val updateMarkersWithAnimateToPositions: LiveData<UpdateMarkersWithAnimateToPositions>
        get() = _updateMarkersWithAnimateToPositions

    data class UpdateMarkersWithAnimateToPosition(
        val pointsShow: List<CourierMapMarker>,
        val pointsFrom: List<CourierMapMarker>,
        val pointTo: CourierMapMarker,
        val animateTo: BoundingBox,
        val offsetY: Int
    )

    private val _updateMarkersWithAnimateToPosition =
        SingleLiveEvent<UpdateMarkersWithAnimateToPosition>()
    val updateMarkersWithAnimateToPosition: LiveData<UpdateMarkersWithAnimateToPosition>
        get() = _updateMarkersWithAnimateToPosition

    data class NavigateToPoint(val point: CoordinatePoint)

    private val _navigateToPoint = SingleLiveEvent<NavigateToPoint>()
    val navigateToPoint: LiveData<NavigateToPoint>
        get() = _navigateToPoint

    data class NavigateToMarker(val id: String)

    private val _navigateToMarker = SingleLiveEvent<NavigateToMarker>()
    val navigateToMarker: LiveData<NavigateToMarker>
        get() = _navigateToMarker

    data class NavigateToPointZoom(val point: CoordinatePoint)

    private val _navigateToPointZoom = SingleLiveEvent<NavigateToPointZoom>()
    val navigateToPointZoom: LiveData<NavigateToPointZoom>
        get() = _navigateToPointZoom

    object NavigateToMyLocation

    private val _navigateToMyLocation = SingleLiveEvent<NavigateToMyLocation>()
    val navigateToMyLocation: LiveData<NavigateToMyLocation>
        get() = _navigateToMyLocation

    data class UpdateMarkers(val points: List<CourierMapMarker>)

    private val _updateMarkers = SingleLiveEvent<UpdateMarkers>()
    val updateMarkers: LiveData<UpdateMarkers>
        get() = _updateMarkers

    data class UpdateMarkersWithIndex(val points: List<CourierMapMarker>)

    private val _updateMarkersWithIndex = SingleLiveEvent<UpdateMarkersWithIndex>()
    val updateMarkersWithIndex: LiveData<UpdateMarkersWithIndex>
        get() = _updateMarkersWithIndex

    object UpdateMyLocation

    private val _updateMyLocation = SingleLiveEvent<UpdateMyLocation>()
    val updateMyLocation: LiveData<UpdateMyLocation>
        get() = _updateMyLocation

    data class UpdateMyLocationPoint(val point: CoordinatePoint)

    private val _updateMyLocationPoint = SingleLiveEvent<UpdateMyLocationPoint>()
    val updateMyLocationPoint: LiveData<UpdateMyLocationPoint>
        get() = _updateMyLocationPoint

    data class ZoomToBoundingBox(val boundingBox: BoundingBox, val animate: Boolean)

    private val _zoomToBoundingBox = SingleLiveEvent<ZoomToBoundingBox>()
    val zoomToBoundingBox: LiveData<ZoomToBoundingBox>
        get() = _zoomToBoundingBox

    private val _visibleManagerBar = SingleLiveEvent<CourierVisibilityManagerBar>()
    val visibleManagerBar: LiveData<CourierVisibilityManagerBar>
        get() = _visibleManagerBar

    fun subscribeState() {
        subscribeMapState()
    }

    private fun subscribeMapState() {
        addSubscription(
            interactor.subscribeMapState()
                .subscribe(
                    { subscribeMapStateComplete(it) },
                    { LogUtils { logDebugApp("subscribeMapState() error " + it) } }
                )
        )
    }

    private fun subscribeMapStateComplete(it: CourierMapState) {
        when (it) {
            is CourierMapState.NavigateToPoint -> _navigateToPoint.postValue(
                NavigateToPoint(it.point))
            is CourierMapState.UpdateMarkersWithAnimateToPositions -> _updateMarkersWithAnimateToPositions.postValue(
                UpdateMarkersWithAnimateToPositions(
                    it.pointsHide,
                    it.pointFrom,
                    it.pointsTo,
                    it.animateTo,
                    it.offsetY
                ))
            is CourierMapState.ZoomToBoundingBoxOffsetY -> _zoomToBoundingBoxOffsetY.postValue(
                ZoomToBoundingBoxOffsetY(it.boundingBox, it.animate, it.offsetY)
            )
            is CourierMapState.NavigateToMarker -> _navigateToMarker.postValue(
                NavigateToMarker(it.id)
            )
            is CourierMapState.NavigateToPointZoom -> _navigateToPointZoom.postValue(
                NavigateToPointZoom(it.point)
            )
            CourierMapState.NavigateToMyLocation -> _navigateToMyLocation.postValue(
                NavigateToMyLocation
            )
            is CourierMapState.UpdateMarkers -> _updateMarkers.postValue(
                UpdateMarkers(it.points)
            )
            is CourierMapState.UpdateMarkersWithIndex -> _updateMarkersWithIndex.postValue(
                UpdateMarkersWithIndex(it.points)
            )
            CourierMapState.UpdateMyLocation -> _updateMyLocation.postValue(
                UpdateMyLocation
            )
            is CourierMapState.UpdateMyLocationPoint -> _updateMyLocationPoint.postValue(
                UpdateMyLocationPoint(it.point)
            )
            is CourierMapState.ZoomToBoundingBox -> _zoomToBoundingBox.postValue(
                ZoomToBoundingBox(it.boundingBox, it.animate)
            )
            is CourierMapState.UpdateMarkersWithAnimateToPosition -> _updateMarkersWithAnimateToPosition.postValue(
                UpdateMarkersWithAnimateToPosition(
                    it.pointsShow,
                    it.pointsFrom,
                    it.pointTo,
                    it.animateTo,
                    it.offsetY
                )
            )
            CourierMapState.ClearMap -> _clearMap.postValue(ClearMap)
            CourierMapState.ShowManagerBar -> _visibleManagerBar.postValue(
                CourierVisibilityManagerBar.Visible
            )
            CourierMapState.HideManagerBar -> _visibleManagerBar.postValue(
                CourierVisibilityManagerBar.Hide
            )
        }
    }

    fun onItemClick(point: MapPoint) {
        interactor.markerClick(point)
    }

    fun onMapClick() {
        interactor.mapClick()
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

    fun onZoomClick() {
        interactor.prolongTimeHideManager()
    }

    fun onShowAllClick() {
        interactor.prolongTimeHideManager()
        interactor.showAll()
    }

    fun onAnimateComplete() {
        interactor.animateComplete()
    }

    companion object {
        const val SCREEN_TAG = "CourierMap"
    }

}

fun moscowCoordinatePoint() = CoordinatePoint(55.751244, 37.618423)

 */