package ru.wb.go.ui.couriermap

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val _clearMap = Channel<ClearMap>(
        Channel.UNLIMITED
    )
    val clearMap = _clearMap.receiveAsFlow()

    data class ZoomToBoundingBoxOffsetY(
        val boundingBox: BoundingBox,
        val animate: Boolean,
        val offsetY: Int
    )

    private val _zoomToBoundingBoxOffsetY = Channel<ZoomToBoundingBoxOffsetY>()
    val zoomToBoundingBoxOffsetY = _zoomToBoundingBoxOffsetY.receiveAsFlow()

    data class UpdateMarkersWithAnimateToPositions(
        val pointsHide: List<CourierMapMarker>,
        val pointFrom: CourierMapMarker,
        val pointsTo: List<CourierMapMarker>,
        val animateTo: BoundingBox,
        val offsetY: Int
    )

    private val _updateMarkersWithAnimateToPositions =
        Channel<UpdateMarkersWithAnimateToPositions>(
            Channel.UNLIMITED
        )
    val updateMarkersWithAnimateToPositions = _updateMarkersWithAnimateToPositions.receiveAsFlow()

    data class UpdateMarkersWithAnimateToPosition(
        val pointsShow: List<CourierMapMarker>,
        val pointsFrom: List<CourierMapMarker>,
        val pointTo: CourierMapMarker,
        val animateTo: BoundingBox,
        val offsetY: Int
    )

    private val _updateMarkersWithAnimateToPosition =
        Channel<UpdateMarkersWithAnimateToPosition>(
            Channel.UNLIMITED
        )
    val updateMarkersWithAnimateToPosition = _updateMarkersWithAnimateToPosition.receiveAsFlow()

    data class NavigateToPoint(val point: CoordinatePoint)

    private val _navigateToPoint = Channel<NavigateToPoint>(
        Channel.UNLIMITED
    )
    val navigateToPoint = _navigateToPoint.receiveAsFlow()

    data class NavigateToMarker(val id: String)

    private val _navigateToMarker = Channel<NavigateToMarker>(
        Channel.UNLIMITED
    )
    val navigateToMarker = _navigateToMarker.receiveAsFlow()

    data class NavigateToPointZoom(val point: CoordinatePoint)

    private val _navigateToPointZoom = Channel<NavigateToPointZoom>(
        Channel.UNLIMITED
    )
    val navigateToPointZoom = _navigateToPointZoom.receiveAsFlow()

    object NavigateToMyLocation

    private val _navigateToMyLocation = Channel<NavigateToMyLocation>(
        Channel.UNLIMITED
    )
    val navigateToMyLocation = _navigateToMyLocation.receiveAsFlow()

    data class UpdateMarkers(val points: List<CourierMapMarker>)

    private val _updateMarkers = Channel<UpdateMarkers>(
        Channel.UNLIMITED
    )
    val updateMarkers = _updateMarkers.receiveAsFlow()

    data class UpdateMarkersWithIndex(val points: List<CourierMapMarker>)

    private val _updateMarkersWithIndex = Channel<UpdateMarkersWithIndex>(
        Channel.UNLIMITED
    )
    val updateMarkersWithIndex = _updateMarkersWithIndex.receiveAsFlow()

    object UpdateMyLocation

    private val _updateMyLocation = Channel<UpdateMyLocation>(
        Channel.UNLIMITED
    )
    val updateMyLocation = _updateMyLocation.receiveAsFlow()

    data class UpdateMyLocationPoint(val point: CoordinatePoint)

    private val _updateMyLocationPoint = Channel<UpdateMyLocationPoint>(
        Channel.UNLIMITED
    )
    val updateMyLocationPoint = _updateMyLocationPoint.receiveAsFlow()

    data class ZoomToBoundingBox(val boundingBox: BoundingBox, val animate: Boolean)

    private val _zoomToBoundingBox = Channel<ZoomToBoundingBox>(
        Channel.UNLIMITED
    )
    val zoomToBoundingBox = _zoomToBoundingBox.receiveAsFlow()

    private val _visibleManagerBar = Channel<CourierVisibilityManagerBar>(
        Channel.UNLIMITED
    )
    val visibleManagerBar = _visibleManagerBar.receiveAsFlow()


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
        viewModelScope.launch {
            when (it) {
                is CourierMapState.NavigateToPoint -> _navigateToPoint.send(NavigateToPoint(it.point))


                is CourierMapState.UpdateMarkersWithAnimateToPositions -> _updateMarkersWithAnimateToPositions.send(
                    UpdateMarkersWithAnimateToPositions(
                        it.pointsHide,
                        it.pointFrom,
                        it.pointsTo,
                        it.animateTo,
                        it.offsetY
                    )
                )


                is CourierMapState.ZoomToBoundingBoxOffsetY ->{
                    _zoomToBoundingBoxOffsetY.send(
                        ZoomToBoundingBoxOffsetY(it.boundingBox, it.animate, it.offsetY)
                    )
                }


                is CourierMapState.NavigateToMarker -> _navigateToMarker.send(NavigateToMarker(it.id))


                is CourierMapState.NavigateToPointZoom -> _navigateToPointZoom.send(
                    NavigateToPointZoom(it.point)
                )

                CourierMapState.NavigateToMyLocation -> _navigateToMyLocation.send(
                    NavigateToMyLocation
                )


                is CourierMapState.UpdateMarkers -> _updateMarkers.send(UpdateMarkers(it.points.toList()))


                is CourierMapState.UpdateMarkersWithIndex -> _updateMarkersWithIndex.send(
                    UpdateMarkersWithIndex(it.points)
                )


                CourierMapState.UpdateMyLocation -> _updateMyLocation.send(UpdateMyLocation)


                is CourierMapState.UpdateMyLocationPoint -> _updateMyLocationPoint.send(
                    UpdateMyLocationPoint(it.point)
                )


                is CourierMapState.ZoomToBoundingBox -> _zoomToBoundingBox.send(
                    ZoomToBoundingBox(
                        it.boundingBox,
                        it.animate
                    )
                )


                is CourierMapState.UpdateMarkersWithAnimateToPosition -> _updateMarkersWithAnimateToPosition.send(
                    UpdateMarkersWithAnimateToPosition(
                        it.pointsShow,
                        it.pointsFrom,
                        it.pointTo,
                        it.animateTo,
                        it.offsetY
                    )
                )
                CourierMapState.ClearMap -> _clearMap.send(ClearMap)
                CourierMapState.ShowManagerBar -> _visibleManagerBar.send(
                    CourierVisibilityManagerBar.Visible
                )
                CourierMapState.HideManagerBar -> _visibleManagerBar.send(
                    CourierVisibilityManagerBar.Hide
                )
            }
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


