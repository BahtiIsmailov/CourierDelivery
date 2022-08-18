package ru.wb.go.ui.couriermap

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.couriermap.domain.CourierMapInteractor
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapPoint

class CourierMapViewModel(
    private val interactor: CourierMapInteractor,
) : NetworkViewModel() {

    object ClearMap

    private val _clearMap = MutableLiveData<ClearMap>()
    val clearMap = _clearMap

    data class ZoomToBoundingBoxOffsetY(
        val boundingBox: BoundingBox,
        val animate: Boolean,
        val offsetY: Int
    )

    private val _zoomToBoundingBoxOffsetY = MutableLiveData<ZoomToBoundingBoxOffsetY>()
    val zoomToBoundingBoxOffsetY = _zoomToBoundingBoxOffsetY

    data class UpdateMarkersWithAnimateToPositions(
        val pointsHide: List<CourierMapMarker>,
        val pointFrom: CourierMapMarker,
        val pointsTo: List<CourierMapMarker>,
        val animateTo: BoundingBox,
        val offsetY: Int
    )

    private val _updateMarkersWithAnimateToPositions =
        MutableLiveData<UpdateMarkersWithAnimateToPositions>()
    val updateMarkersWithAnimateToPositions: LiveData<UpdateMarkersWithAnimateToPositions> get() = _updateMarkersWithAnimateToPositions

    data class UpdateMarkersWithAnimateToPosition(
        val pointsShow: List<CourierMapMarker>,
        val pointsFrom: List<CourierMapMarker>,
        val pointTo: CourierMapMarker,
        val animateTo: BoundingBox,
        val offsetY: Int
    )

    private val _updateMarkersWithAnimateToPosition =
        MutableLiveData<UpdateMarkersWithAnimateToPosition>()
    val updateMarkersWithAnimateToPosition = _updateMarkersWithAnimateToPosition

    data class NavigateToPoint(val point: CoordinatePoint)

    private val _navigateToPoint = MutableLiveData<NavigateToPoint>()
    val navigateToPoint: LiveData<NavigateToPoint> get() = _navigateToPoint

    data class NavigateToMarker(val id: String)

    private val _navigateToMarker = MutableLiveData<NavigateToMarker>()
    val navigateToMarker = _navigateToMarker

    data class NavigateToPointZoom(val point: CoordinatePoint)

    private val _navigateToPointZoom = MutableLiveData<NavigateToPointZoom>()
    val navigateToPointZoom = _navigateToPointZoom

    object NavigateToMyLocation

    private val _navigateToMyLocation = MutableLiveData<NavigateToMyLocation>()
    val navigateToMyLocation = _navigateToMyLocation

    data class UpdateMarkers(val points: List<CourierMapMarker>)

    private val _updateMarkers = MutableLiveData<UpdateMarkers>()
    val updateMarkers = _updateMarkers

    data class UpdateMarkersWithIndex(val points: List<CourierMapMarker>)

    private val _updateMarkersWithIndex = MutableLiveData<UpdateMarkersWithIndex>()
    val updateMarkersWithIndex = _updateMarkersWithIndex

    object UpdateMyLocation

    private val _updateMyLocation = MutableLiveData<UpdateMyLocation>()
    val updateMyLocation = _updateMyLocation

    data class UpdateMyLocationPoint(val point: CoordinatePoint)

    private val _updateMyLocationPoint = MutableLiveData<UpdateMyLocationPoint>()
    val updateMyLocationPoint = _updateMyLocationPoint

    data class ZoomToBoundingBox(val boundingBox: BoundingBox, val animate: Boolean)

    private val _zoomToBoundingBox = MutableLiveData<ZoomToBoundingBox>()
    val zoomToBoundingBox = _zoomToBoundingBox

    private val _visibleManagerBar = MutableLiveData<CourierVisibilityManagerBar>()
    val visibleManagerBar = _visibleManagerBar


    init {
        subscribeMapState()
    }

    private fun subscribeMapState() {
        interactor.subscribeMapState()
            .onEach { mapState ->
                subscribeMapStateComplete(mapState)
            }
            .catch {
                logException(it, "subscribeMapState")
            }
            .launchIn(viewModelScope)
    }

    private fun subscribeMapStateComplete(it: CourierMapState) {
        when (it) {
            is CourierMapState.NavigateToPoint -> {
                _navigateToPoint.value = NavigateToPoint(it.point)
            }


            is CourierMapState.UpdateMarkersWithAnimateToPositions -> _updateMarkersWithAnimateToPositions.value =
                UpdateMarkersWithAnimateToPositions(
                    it.pointsHide,
                    it.pointFrom,
                    it.pointsTo,
                    it.animateTo,
                    it.offsetY
                )


            is CourierMapState.ZoomToBoundingBoxOffsetY -> {
                _zoomToBoundingBoxOffsetY.value =
                    ZoomToBoundingBoxOffsetY(it.boundingBox, it.animate, it.offsetY)


            }


            is CourierMapState.NavigateToMarker -> _navigateToMarker.value = NavigateToMarker(it.id)


            is CourierMapState.NavigateToPointZoom -> _navigateToPointZoom.value =
                NavigateToPointZoom(it.point)


            CourierMapState.NavigateToMyLocation -> _navigateToMyLocation.value =
                NavigateToMyLocation


            is CourierMapState.UpdateMarkers -> _updateMarkers.value =
                UpdateMarkers(it.points.toList())


            is CourierMapState.UpdateMarkersWithIndex -> _updateMarkersWithIndex.value =
                UpdateMarkersWithIndex(it.points)


            CourierMapState.UpdateMyLocation -> _updateMyLocation.value = UpdateMyLocation


            is CourierMapState.UpdateMyLocationPoint -> _updateMyLocationPoint.value =
                UpdateMyLocationPoint(it.point)


            is CourierMapState.ZoomToBoundingBox -> _zoomToBoundingBox.value =
                ZoomToBoundingBox(
                    it.boundingBox,
                    it.animate
                )


            is CourierMapState.UpdateMarkersWithAnimateToPosition -> _updateMarkersWithAnimateToPosition.value =
                UpdateMarkersWithAnimateToPosition(
                    it.pointsShow,
                    it.pointsFrom,
                    it.pointTo,
                    it.animateTo,
                    it.offsetY
                )

            CourierMapState.ClearMap -> _clearMap.value = ClearMap
            CourierMapState.ShowManagerBar -> _visibleManagerBar.value =
                CourierVisibilityManagerBar.Visible

            CourierMapState.HideManagerBar -> _visibleManagerBar.value =
                CourierVisibilityManagerBar.Hide

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
        interactor.showAll()
        interactor.prolongTimeHideManager()
    }

    fun onAnimateComplete() {
        interactor.animateComplete()
    }

    companion object {
        const val SCREEN_TAG = "CourierMap"
    }

}

fun moscowCoordinatePoint() = CoordinatePoint(55.751244, 37.618423)


