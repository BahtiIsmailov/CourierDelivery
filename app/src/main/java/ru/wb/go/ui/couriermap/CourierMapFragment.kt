package ru.wb.go.ui.couriermap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.config.IConfigurationProvider
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import ru.wb.go.R
import ru.wb.go.databinding.MapFragmentBinding
import ru.wb.go.utils.CoroutineExtension
import ru.wb.go.utils.hasPermissions
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapPoint
import java.io.File
import java.util.concurrent.TimeUnit


class CourierMapFragment : Fragment(), GoogleApiClient.ConnectionCallbacks {

    private val viewModel by viewModel<CourierMapViewModel>()

    companion object {
        const val MY_LOCATION_ID = "my_location_id"
        const val WAREHOUSE_ID = "warehouse_id"
        const val ADDRESS_MAP_PREFIX = "ADR_"
        private const val OSMD_BASE_PATH = "osmdroid"
        private const val OSMD_BASE_TILES = "tiles"
        private const val DEFAULT_ZOOM = 12.0
        private const val MIN_ZOOM = 3.5
        private const val MAX_ZOOM = 20.0
        private const val DEFAULT_POINT_ZOOM = 13.0
        private const val SIZE_IN_PIXELS = 100
        private const val DEFAULT_ANIMATION_MS = 300L
        private const val DURATION_POINTS_MS = 500L
        private const val DELAY_ANIMATION_MS = 15L
        private const val INTERPOLATOR_ANIMATION_MAX = 1.0
        private const val TEXT_SIZE_INDEX_MARKER = 40f
    }

    private var _binding: MapFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var locationRequest: LocationRequest
    private var lastLocation: Location? = null
    private var isRequestAccessLocation = false

    private lateinit var mapController: IMapController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = MapFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservable()
        initListeners()
        initMapView()
        viewModel.subscribeMapState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createLocationRequest()
        buildGoogleApiClient()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.numUpdates = 1
        locationRequest.interval = 0
    }

    private fun buildGoogleApiClient() {
        googleApiClient =
            GoogleApiClient.Builder(requireActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build()
        googleApiClient.connect()
    }

    override fun onStart() {
        super.onStart()
        startGoogleApiClient()
    }

    private fun startGoogleApiClient() {
        if (!googleApiClient.isConnected) googleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()
        stopGoogleApiClient()
    }

    private fun stopGoogleApiClient() {
        googleApiClient.disconnect()
    }

    private fun initAccessLocationPermissions() {
        if (hasPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) || isRequestAccessLocation
        ) updateLocation()
        else launchPermissionsRequest()
    }

    private fun launchPermissionsRequest() {
        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )
        isRequestAccessLocation = true
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var grand = true
            permissions.entries.forEach {
                if (!it.value) grand = false
            }
            if (grand) {
                stopGoogleApiClient()
                startGoogleApiClient()
            }
            updateLocation()
        }

    private fun updateLocation() {
        if (lastLocation == null) {
            viewModel.onForcedLocationUpdateDefault()
        } else {
            viewModel.onForcedLocationUpdate(
                CoordinatePoint(lastLocation!!.latitude, lastLocation!!.longitude)
            )
        }
    }

    private fun initMapView() {
        val config: IConfigurationProvider = Configuration.getInstance()
        config.osmdroidBasePath = createOsmdroidBasePath()
        config.osmdroidTileCache = createOsmdroidTilePath(config.osmdroidBasePath)
        // FIXME: ??? 
        config.userAgentValue = context?.packageName
        config.load(
            requireActivity(),
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        )

        binding.map.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        binding.map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapController = binding.map.controller
        mapController.setZoom(DEFAULT_ZOOM)
        with(moscowCoordinatePoint()) {
            mapController.setCenter(GeoPoint(latitude, longitude))
        }
        binding.map.setBuiltInZoomControls(false)
        binding.map.setMultiTouchControls(true)
        binding.map.minZoomLevel = MIN_ZOOM
        binding.map.maxZoomLevel = MAX_ZOOM
        binding.map.setUseDataConnection(true)

        val colorMatrix = updateScaleMatrix(0.9f, 0.9f, 0.9f, 0.9f)
        binding.map.overlayManager.tilesOverlay.setColorFilter(colorMatrix)
    }

    private fun updateScaleMatrix(
        rScale: Float, gScale: Float, bScale: Float, aScale: Float
    ): ColorMatrixColorFilter {
        val colorMatrix = ColorMatrix()
        colorMatrix.setScale(rScale, gScale, bScale, aScale)
        return ColorMatrixColorFilter(colorMatrix)
    }

    private fun createOsmdroidTilePath(osmdroidBasePath: File): File {
        val osmdroidTilePath = File(osmdroidBasePath, OSMD_BASE_TILES)
        osmdroidTilePath.mkdirs()
        return osmdroidTilePath
    }

    private fun createOsmdroidBasePath(): File {
        val path: File = requireActivity().filesDir
        val osmdroidBasePath = File(path, OSMD_BASE_PATH)
        osmdroidBasePath.mkdirs()
        return osmdroidBasePath
    }

    private val onMarkerClickListener = { marker: Marker, _: MapView ->
        viewModel.onItemClick(with(marker) { MapPoint(id, position.latitude, position.longitude) })
        true
    }

    private val onSkipMarkerClickListener = { _: Marker, _: MapView -> true }

    private fun getIcon(idRes: Int) = AppCompatResources.getDrawable(requireContext(), idRes)

    private fun navigateToMarker(id: String) {
        binding.map.overlays.find { it is Marker && it.id == id }?.apply {
            mapController.animateTo((this as Marker).position)
        }
        binding.map.invalidate()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObservable() {
        viewModel.clearMap.observe(viewLifecycleOwner) {
            clearMap()
        }

        viewModel.zoomToBoundingBoxOffsetY.observe(viewLifecycleOwner) {
            checkMapViewAndZoomToBoundingBoxOffsetY(it)
        }

        viewModel.updateMarkersWithAnimateToPositions.observe(viewLifecycleOwner) {
            updateMarkersWithAnimateToPositions(it)
        }

        viewModel.updateMarkersWithAnimateToPosition.observe(viewLifecycleOwner) {
            updateMarkersWithAnimateToPosition(it)
        }

        viewModel.navigateToPoint.observe(viewLifecycleOwner) {
            navigateToPoint(it.point)
        }

        viewModel.updateMarkers.observe(viewLifecycleOwner) {
            updateMarkers(it.points)
        }

        viewModel.updateMarkersWithIndex.observe(viewLifecycleOwner) {
            updateMarkersWithIndex(it.points)
        }
        viewModel.navigateToMarker.observe(viewLifecycleOwner) {
            navigateToMarker(it.id)
        }

        viewModel.navigateToPointZoom.observe(viewLifecycleOwner) {
            navigateToPointZoom(it.point)
        }

        viewModel.navigateToMyLocation.observe(viewLifecycleOwner) {
            navigateToMyLocation()
        }

        viewModel.updateMyLocationPoint.observe(viewLifecycleOwner) {
            updateMyLocationPoint(it.point)
        }

        viewModel.zoomToBoundingBox.observe(viewLifecycleOwner) {
            zoomToCenterBoundingBox(it.boundingBox, it.animate)
        }

        viewModel.updateMyLocation.observe(viewLifecycleOwner) {
            updateMyLocation()
        }

        viewModel.visibleManagerBar.observe(viewLifecycleOwner) {
            visibleManagerBar(it)
        }

    }

    private fun generateFilterMarker(findId: String): (Marker) -> Boolean = { it.id == findId }

    private fun <T> List<T>.filterAll(filters: List<(T) -> Boolean>) =
        filter { item ->
            var isFind = false
            run find@{
                filters.forEach { filter ->
                    if (filter(item)) {
                        isFind = true
                        return@find
                    }
                }
            }
            isFind
        }

    private inner class BoundingBoxAnimator(
        from: BoundingBox,
        val to: BoundingBox,
        val offsetY: Int
    ) {

        val maxLatFrom = from.latNorth
        val maxLongFrom = from.lonEast
        val minLatFrom = from.latSouth
        val minLongFrom = from.lonWest

        val maxLatTo = to.latNorth
        val maxLongTo = to.lonEast
        val minLatTo = to.latSouth
        val minLongTo = to.lonWest

        var latMax: Double = 0.0
        var lngMax: Double = 0.0
        var latMin: Double = 0.0
        var lngMin: Double = 0.0

        fun next(interpolation: Float, offsetAnimation: Float) {
            if (interpolation < INTERPOLATOR_ANIMATION_MAX) {
                latMax = interpolation * maxLatTo + offsetAnimation * maxLatFrom
                lngMax = interpolation * maxLongTo + offsetAnimation * maxLongFrom
                latMin = interpolation * minLatTo + offsetAnimation * minLatFrom
                lngMin = interpolation * minLongTo + offsetAnimation * minLongFrom
            } else {
                latMax = maxLatTo
                lngMax = maxLongTo
                latMin = minLatTo
                lngMin = minLongTo
            }
            to.set(latMax, lngMax, latMin, lngMin)
            checkMapViewAndZoomToBoundingBoxOffsetY(
                CourierMapViewModel.ZoomToBoundingBoxOffsetY(to, false, offsetY)
            )
        }
    }

    private fun updateMarkersWithAnimateToPosition(withAnimateToPosition: CourierMapViewModel.UpdateMarkersWithAnimateToPosition) {

        val boundingBoxAnimator = BoundingBoxAnimator(
            binding.map.boundingBox,
            withAnimateToPosition.animateTo,
            withAnimateToPosition.offsetY
        )

        val showMarkers = findMapMarkersByFilterId(withAnimateToPosition.pointsShow)
        showMarkers.forEach { it.setOnMarkerClickListener(onMarkerClickListener) }
        val animateMarkersTo = findMapMarkersByFilterId(withAnimateToPosition.pointsFrom)

        val fromGeoPoints = mutableListOf<GeoPoint>()
        animateMarkersTo.forEach {
            with(it.position) {
                fromGeoPoints.add(GeoPoint(latitude, longitude))
            }
        }

        val pointTo = withAnimateToPosition.pointTo.point
        val handler = Handler(Looper.getMainLooper())
        val start = SystemClock.uptimeMillis()
        val interpolator = LinearInterpolator()

        handler.post(object : Runnable {

            var lat: Double = 0.0
            var lng: Double = 0.0

            override fun run() {
                val elapsed = (SystemClock.uptimeMillis() - start).toFloat()
                val interpolation = interpolator.getInterpolation(elapsed / DURATION_POINTS_MS)
                val offsetAnimation = 1 - interpolation

                val alpha = 1 - offsetAnimation

                animateMarkersTo.forEachIndexed { index, marker ->
                    val pointFrom = fromGeoPoints[index]
                    if (interpolation < INTERPOLATOR_ANIMATION_MAX) {
                        lat = interpolation * pointTo.lat + offsetAnimation * pointFrom.latitude
                        lng = interpolation * pointTo.long + offsetAnimation * pointFrom.longitude
                    } else removeMarkerById(marker.id)
                    marker.position = GeoPoint(lat, lng)
                }

                boundingBoxAnimator.next(interpolation, offsetAnimation)

                showMarkers.forEach { marker ->
                    if (interpolation < 1.0) marker.alpha = alpha
                    else marker.alpha = 1f
                }

                if (interpolation < INTERPOLATOR_ANIMATION_MAX)
                    handler.postDelayed(this, DELAY_ANIMATION_MS)
                else viewModel.onAnimateComplete()

                binding.map.postInvalidate()
            }
        })
    }

    private fun findMapMarkersByFilterId(showPoints: List<CourierMapMarker>): MutableList<Marker> {
        val markerFilters = mutableListOf<(Marker) -> Boolean>()
        showPoints.forEach { markerFilters.add(generateFilterMarker(it.point.id)) }
        val findMapMarkers = mutableListOf<Marker>()
        binding.map.overlays
            .filterIsInstance<Marker>()
            .filterAll(markerFilters)
            .apply { findMapMarkers.addAll(this) }
        return findMapMarkers
    }

    private fun clearMap() {
        binding.map.overlays.clear()
    }

    private fun checkMapViewAndZoomToBoundingBoxOffsetY(zoomToBoundingBoxOffsetY: CourierMapViewModel.ZoomToBoundingBoxOffsetY) {
        with(binding.map) {
            if (height > 0 && width > 0) {
                zoomToCenterBoundingBoxOffsetY(
                    zoomToBoundingBoxOffsetY.boundingBox,
                    zoomToBoundingBoxOffsetY.animate,
                    zoomToBoundingBoxOffsetY.offsetY
                )
            } else {
                viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        zoomToCenterBoundingBoxOffsetY(
                            zoomToBoundingBoxOffsetY.boundingBox,
                            zoomToBoundingBoxOffsetY.animate,
                            zoomToBoundingBoxOffsetY.offsetY
                        )
                    }
                })
            }
        }
    }

    private fun updateMarkersWithAnimateToPositions(
        withAnimateToPositions: CourierMapViewModel.UpdateMarkersWithAnimateToPositions
    ) {

        val boundingBoxAnimator = BoundingBoxAnimator(
            binding.map.boundingBox,
            withAnimateToPositions.animateTo,
            withAnimateToPositions.offsetY
        )
        binding.map.overlays.remove(overlayBackground)

        with(withAnimateToPositions) {

            pointsTo.forEach { removeMarkerById(it.point.id) }
            val markersRestore = mutableListOf<Marker>()
            binding.map.overlays
                .filterIsInstance<Marker>()
                .apply { markersRestore.addAll(this) }

            binding.map.overlays.clear()
            pointsTo.forEach(updateMapMarker)
            val markersTo = findMapMarkersByFilterId(pointsTo)
            binding.map.overlays.addAll(markersRestore)

            val markersHide = findMapMarkersByFilterId(pointsHide)
            markersHide.forEach { it.setOnMarkerClickListener(onSkipMarkerClickListener) }

            val handler = Handler(Looper.getMainLooper())
            val start = SystemClock.uptimeMillis()
            val interpolator = LinearInterpolator()

            handler.post(object : Runnable {

                var lat: Double = 0.0
                var lng: Double = 0.0

                override fun run() {
                    val elapsed = (SystemClock.uptimeMillis() - start).toFloat()
                    val interpolation = interpolator.getInterpolation(elapsed / DURATION_POINTS_MS)
                    val offsetAnimation = 1 - interpolation
                    val alpha = offsetAnimation * 1.0f

                    markersTo.forEachIndexed { index, marker ->
                        val pointTo = pointsTo[index].point
                        if (interpolation < INTERPOLATOR_ANIMATION_MAX) {
                            lat =
                                interpolation * pointTo.lat + offsetAnimation * pointFrom.point.lat
                            lng =
                                interpolation * pointTo.long + offsetAnimation * pointFrom.point.long

                        } else {
                            lat = pointTo.lat
                            lng = pointTo.long
                            //marker.setOnMarkerClickListener(onMarkerClickListener)
                        }
                        marker.position = GeoPoint(lat, lng)
                    }

                    markersHide.forEach { marker ->
                        if (interpolation < 1.0) marker.alpha = alpha
                        else marker.alpha = 0f
                    }

                    boundingBoxAnimator.next(interpolation, offsetAnimation)

                    if (interpolation < INTERPOLATOR_ANIMATION_MAX) {
                        handler.postDelayed(this, DELAY_ANIMATION_MS)
                    } else {
                        binding.map.overlays.clear()
                        addOverlayBackground()
                        binding.map.overlays.addAll(markersRestore)
                        binding.map.overlays.addAll(markersTo)
                    }

                    binding.map.postInvalidate()
                }
            })
        }
    }

    private fun navigateToPointZoom(it: CoordinatePoint) {
        val point = GeoPoint(it.latitude, it.longitude)
        mapController.setZoom(DEFAULT_POINT_ZOOM)
        mapController.setCenter(point)
        binding.map.invalidate()
    }

    private fun navigateToPoint(it: CoordinatePoint) {
        navigateToGeoPoint(it.latitude, it.longitude)
    }

    private fun navigateToGeoPoint(latitude: Double, longitude: Double) {
        val point = GeoPoint(latitude, longitude)
        mapController.animateTo(point, null, DEFAULT_ANIMATION_MS)
        binding.map.invalidate()
    }

    private fun zoomToCenterBoundingBoxOffsetY(
        boundingBox: BoundingBox,
        animate: Boolean,
        offsetY: Int
    ) {
        val dimenOffset = R.dimen.map_offset
        val offsetBoundingBox = boundingBox.withOffset(
            binding.map,
            offsetY,
            dimenOffset,
            dimenOffset,
            dimenOffset,
            dimenOffset
        )
        zoomToCenterBoundingBox(offsetBoundingBox, animate)
    }

    private fun updateMarkers(mapPoints: List<CourierMapMarker>) {
        updateMapMarkers(mapPoints)
    }

    private fun zoomToCenterBoundingBox(boundingBox: BoundingBox, animate: Boolean) {
        with(binding.map) {
            if (height > 0) {
                mapController.setCenter(boundingBox.centerWithDateLine)
                zoomToBoundingBox(boundingBox, animate, SIZE_IN_PIXELS)
            } else {
                viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        mapController.setCenter(boundingBox.centerWithDateLine)
                        zoomToBoundingBox(boundingBox, animate, SIZE_IN_PIXELS)
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
            }
        }
    }

    private fun updateMarkersWithIndex(mapPoints: List<CourierMapMarker>) {
        updateMapMarkersWithIndex(mapPoints)
    }

    private fun updateMapMarkersWithIndex(mapPoints: List<CourierMapMarker>) {
        mapPoints.forEachIndexed(updateMapMarkerWithIndex)
        binding.map.invalidate()
    }

    private fun updateMapMarkers(mapPoints: List<CourierMapMarker>) {
        addOverlayBackground()
        mapPoints.forEach(updateMapMarker)
        binding.map.invalidate()
    }

    private val overlayBackground = OverlayBackground(
        object : OverlayBackground.OnBackgroundClickListener {
            override fun onBackgroundClick() {
                viewModel.onMapClick()
            }
        })

    private fun addOverlayBackground() {
        val overlayBackground = binding.map.overlays.find { it.equals(overlayBackground) }
        if (overlayBackground == null) binding.map.overlays.add(this.overlayBackground)
    }

    private val updateMapMarker = { item: CourierMapMarker ->
        with(item) {
            val findPoint = findMapPointById(point.id)
            if (findPoint == null)
                addMapMarker(point.id, point.lat, point.long, getIcon(item.icon))
            else updateMapMarker(findPoint, point.id, point.lat, point.long, getIcon(item.icon))
        }
    }

    private val updateMapMarkerWithIndex = { index: Int, item: CourierMapMarker ->
        with(item) {
            addMapMarker(
                point.id,
                point.lat,
                point.long,
                BitmapDrawable(resources, getBitmapIndexMarker(point.id, icon))
            )
        }
    }

    private fun updateMapMarker(
        mapMarker: Marker,
        id: String,
        lat: Double,
        long: Double,
        icon: Drawable?
    ) {
        mapMarker.apply {
            this.id = id
            this.icon = icon
            this.position = GeoPoint(lat, long)
            this.setAnchor(0.5f, 0.5f)
        }

    }

    private fun addMapMarker(
        id: String,
        lat: Double,
        long: Double,
        icon: Drawable?
    ) {
        val markerMap = Marker(binding.map)
        markerMap.setOnMarkerClickListener(onMarkerClickListener)

        markerMap.id = id
        markerMap.icon = icon
        markerMap.position = GeoPoint(lat, long)
        markerMap.setAnchor(0.5f, 0.5f)
        binding.map.overlays.add(markerMap)
    }

     private fun findMapPointById(id: String) =
        binding.map.overlays
            .filterIsInstance<Marker>()
            .find { it.id == id }

    private fun visibleManagerBar(courierVisibilityManagerBar: CourierVisibilityManagerBar) {
        when (courierVisibilityManagerBar) {
            is CourierVisibilityManagerBar.Hide -> {
                if (binding.managerLayout.visibility == View.GONE) return
                val animation = AnimationUtils.loadAnimation(context, R.anim.slide_out_right)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        binding.managerLayout.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                })
                binding.managerLayout.startAnimation(animation)
            }
            is CourierVisibilityManagerBar.Visible -> {
                if (binding.managerLayout.visibility == View.VISIBLE) return
                binding.managerLayout.visibility = View.VISIBLE
                val animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_right) // анимация с скрыванием кнопок боковых
                binding.managerLayout.startAnimation(animation)

            }
        }
    }

    private fun initListeners() {
        binding.zoomIn.setOnClickListener {
            binding.map.controller.zoomIn()
            viewModel.onZoomClick()
        }
        binding.zoomOut.setOnClickListener {
            binding.map.controller.zoomOut()
            viewModel.onZoomClick()
        }
        binding.showAll.setOnClickListener {
            viewModel.onShowAllClick()
        }
    }

    private fun navigateToMyLocation() {
        removeMarkerById(MY_LOCATION_ID)
        if (lastLocation == null) {
            navigateToDefault()
        } else {
            with(lastLocation!!) {
                navigateToGeoPoint(latitude, longitude)
                addMyLocationPoint(latitude, longitude)
            }
        }
    }

    private fun navigateToDefault() {
        initMapView()
        with(moscowCoordinatePoint()) {
            navigateToGeoPoint(latitude, longitude)
            addMyLocationPoint(latitude, longitude)
        }
    }

    private fun updateMyLocationPoint(point: CoordinatePoint) {
        removeMarkerById(MY_LOCATION_ID)
        addMyLocationPoint(point.latitude, point.longitude)
    }

    private fun removeMarkerById(id: String) {
        with(binding.map.overlays) {
            filterIsInstance<Marker>()
                .find { it.id == id }
                ?.apply { remove(this) }
        }
    }

    private fun addMyLocationPoint(latitude: Double, longitude: Double) {
        val point = MapPoint(MY_LOCATION_ID, latitude, longitude)
        val mapMarker = Empty(point, R.drawable.ic_warehouse_my_location)
        updateMapMarker(mapMarker)
        binding.map.invalidate()
    }

    private fun updateMyLocation() {
        initAccessLocationPermissions()
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onDestroyView() {
        viewModel.clearSubscription()
        super.onDestroyView()
        _binding = null
    }

    override fun onConnected(p0: Bundle?) {
        if (isLocationPermissionGranted()) {
            updateLastLocation()
        }
        if (lastLocation == null) {
            startLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLastLocation() {
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
        updateLocation()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (isLocationPermissionGranted()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, locationListener()
            )
        }
    }

    private fun isLocationPermissionGranted() = ActivityCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun locationListener(): (Location) -> Unit =
        { viewModel.onForcedLocationUpdate(CoordinatePoint(it.latitude, it.longitude)) }

    override fun onConnectionSuspended(p0: Int) {}

    private fun getBitmapIndexMarker(index: String, @DrawableRes res: Int): Bitmap? {
        val bitmap = convertDrawableToBitmap(res)
        bitmap?.let {
            val canvas = Canvas(bitmap)
            val paint = Paint()
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL

            paint.textAlign = Paint.Align.CENTER
            paint.color = ResourcesCompat.getColor(resources,
                R.color.button_app_primary_pressed, null)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = TEXT_SIZE_INDEX_MARKER

            val xPos = (canvas.width / 2).toFloat()
            val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2)
            canvas.drawText(index, xPos, yPos, paint)
            return bitmap
        }
        return null
    }

    private fun convertDrawableToBitmap(@DrawableRes res: Int): Bitmap? {
        val d = ContextCompat.getDrawable(requireContext(), res)
        d?.let {
            val bitmap =
                Bitmap.createBitmap(d.intrinsicWidth, d.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            d.setBounds(0, 0, canvas.width, canvas.height)
            d.draw(canvas)
            return bitmap
        }
        return null
    }

    class OverlayBackground(private val listener: OnBackgroundClickListener) : Overlay() {

        interface OnBackgroundClickListener {
            fun onBackgroundClick()
        }

        override fun onSingleTapConfirmed(event: MotionEvent, mapView: MapView): Boolean {
            listener.onBackgroundClick()
            return false
        }

    }

    private fun BoundingBox.withOffset(
        mapView: MapView,
        offsetY: Int,
        @DimenRes top: Int,
        @DimenRes bottom: Int,
        @DimenRes left: Int,
        @DimenRes right: Int
    ): BoundingBox {
        val offsetBottom = offsetY * -2 // отступ снизу
        val topPx = mapView.context.resources.getDimensionPixelSize(top)
        val bottomPx = mapView.context.resources.getDimensionPixelSize(bottom) + offsetBottom
        val leftPx = mapView.context.resources.getDimensionPixelSize(left)
        val rightPx = mapView.context.resources.getDimensionPixelSize(right)

        val width = mapView.width
        val height = mapView.height
        val pScreenWidth = width - (leftPx + rightPx)
        val pScreenHeight = (height - (topPx + bottomPx)).coerceAtLeast(2) // не даст значению стать меньше 2
        val nextZoom = MapView.getTileSystem()
            .getBoundingBoxZoom(this, pScreenWidth, pScreenHeight)

        val centerPoint = GeoPoint(centerLatitude, centerLongitude)

        val projection = Projection(
            nextZoom, width, height,
            centerPoint,
            mapView.mapOrientation,
            mapView.isHorizontalMapRepetitionEnabled,
            mapView.isVerticalMapRepetitionEnabled,
            mapView.mapCenterOffsetX,
            mapView.mapCenterOffsetY
        //TODO(надо разобраться с багом крайней южной точкой )
        )


        val northWest = projection.fromPixels(0, 0)
        val southEast = projection.fromPixels(width, height)
        val lonPerPx = (southEast.longitude - northWest.longitude) / width
        val latPerPx = (southEast.latitude - northWest.latitude) / height

        return BoundingBox(
            latNorth - topPx * latPerPx,
            lonEast + rightPx * lonPerPx,
            latSouth + bottomPx * latPerPx,
            lonWest - leftPx * lonPerPx
        )

    }

}