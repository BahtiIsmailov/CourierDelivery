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
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.LinearInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
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
import ru.wb.go.utils.hasPermissions
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapPoint
import java.io.File


class CourierMapFragment : Fragment(), GoogleApiClient.ConnectionCallbacks {

    private val viewModel by viewModel<CourierMapViewModel>()

    companion object {
        const val MY_LOCATION_ID = "my_location_id"
        const val WAREHOUSE_ID = ""
        private const val REQUEST_ERROR = 0
        private const val OSMD_BASE_PATH = "osmdroid"
        private const val OSMD_BASE_TILES = "tiles"
        private const val DEFAULT_ZOOM = 12.0
        private const val MIN_ZOOM = 3.5
        private const val MAX_ZOOM = 20.0
        private const val DEFAULT_POINT_ZOOM = 13.0
        private const val SIZE_IN_PIXELS = 100
        private const val DEFAULT_ANIMATION_MS = 300L
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
        config.userAgentValue = context?.packageName //BuildConfig.APPLICATION_ID
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
        binding.map.overlayManager.tilesOverlay.setColorFilter(colorMatrix) //TilesOverlay.INVERT_COLORS
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

    private fun getIcon(idRes: Int) = AppCompatResources.getDrawable(requireContext(), idRes)

    private fun navigateToMarker(id: String) {
        binding.map.overlays.find { it is Marker && it.id == id }?.apply {
            mapController.animateTo((this as Marker).position)
        }
        binding.map.invalidate()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObservable() {
        viewModel.mapState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierMapState.UpdateMarkers -> updateMarkers(it.points)
                is CourierMapState.UpdateMarkersWithIndex -> {
                    updateMarkersWithIndex(it.points)
                }
                is CourierMapState.NavigateToMarker -> navigateToMarker(it.id)
                is CourierMapState.NavigateToPointZoom -> navigateToPointZoom(it.point)
                is CourierMapState.NavigateToPoint -> navigateToPoint(it.point)
                CourierMapState.NavigateToMyLocation -> navigateToMyLocation()
                is CourierMapState.UpdateMyLocationPoint -> updateMyLocationPoint(it.point)
                is CourierMapState.ZoomToBoundingBox -> zoomToCenterBoundingBox(
                    it.boundingBox,
                    it.animate
                )
                is CourierMapState.ZoomToBoundingBoxOffsetY ->
                    checkMapViewAndZoomToBoundingBoxOffsetY(it)
                CourierMapState.UpdateMyLocation -> updateMyLocation()
                is CourierMapState.UpdateMarkersWithAnimatePosition -> updateMarkersWithAnimatePosition(
                    it
                )
            }
        }
    }

    private fun updateMarkersWithAnimatePosition(it: CourierMapState.UpdateMarkersWithAnimatePosition) {
        // binding.map.overlays.clear()

//
//        private fun addMapMarker(
//            id: String,
//            lat: Double,
//            long: Double,
//            icon: Drawable?
//        ) {
//            val markerMap = Marker(binding.map)
//            markerMap.setOnMarkerClickListener(onMarkerClickListener)
//            markerMap.id = id
//            markerMap.icon = icon
//            markerMap.position = GeoPoint(lat, long)
//            markerMap.setAnchor(0.5f, 0.5f)
//            binding.map.overlays.add(markerMap)
//        }

        val markers = mutableListOf<Marker>()

        it.pointsTo.forEach { state ->

            val markerMap = Marker(binding.map)
            markerMap.id = state.point.id
            markerMap.icon = getIcon(state.icon)
            markerMap.position = GeoPoint(it.pointFrom.latitude, it.pointFrom.longitude)
            markerMap.setAnchor(0.5f, 0.5f)
            markers.add(markerMap)


            binding.map.overlays.add(markerMap)

            val handler = Handler()
            val start = SystemClock.uptimeMillis()
            val duration: Long = 500
            val interpolator = LinearInterpolator()
            handler.post(object : Runnable {
                override fun run() {
                    val elapsed: Long = SystemClock.uptimeMillis() - start
                    val t = interpolator.getInterpolation(elapsed.toFloat() / duration)
                    val lng = t * state.point.long + (1 - t) * it.pointFrom.longitude
                    val lat = t * state.point.lat + (1 - t) * it.pointFrom.latitude
                    markerMap.position = GeoPoint(lat, lng)
                    if (t < 1.0) {
                        handler.postDelayed(this, 15)
                    }
                    binding.map.postInvalidate()
                }
            })

//            addMapMarker(
//                state.point.id,
//                it.pointFrom.latitude,
//                it.pointFrom.longitude,
//                getIcon(state.icon)
//            )
        }

        //binding.map.invalidate()

//        with(item) { addMapMarker(point.id, point.lat, point.long, getIcon(icon)) }
//        initMapMarkers(it.)


    }

    private fun checkMapViewAndZoomToBoundingBoxOffsetY(zoomToBoundingBoxOffsetY: CourierMapState.ZoomToBoundingBoxOffsetY) {
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
                        zoomToCenterBoundingBoxOffsetY(
                            zoomToBoundingBoxOffsetY.boundingBox,
                            zoomToBoundingBoxOffsetY.animate,
                            zoomToBoundingBoxOffsetY.offsetY
                        )
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
            }
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

    private fun updateMarkers(mapPoints: List<CourierMapMarker>) {
        binding.map.overlays.clear()
        initMapMarkers(mapPoints)
    }

    private fun updateMarkersWithIndex(mapPoints: List<CourierMapMarker>) {
        binding.map.overlays.clear()
        initMapMarkersWithIndex(mapPoints)
    }

    private fun initMapMarkersWithIndex(mapPoints: List<CourierMapMarker>) {
        addOverlayBackground()
        mapPoints.forEachIndexed(addMapMarkerWithIndex)
        binding.map.invalidate()
    }

    private fun initMapMarkers(mapPoints: List<CourierMapMarker>) {
        addOverlayBackground()
        mapPoints.forEach(addMapMarker)
        binding.map.invalidate()
    }

    private fun addOverlayBackground() {
        val overlayBackground = OverlayBackground(
            object : OverlayBackground.OnBackgroundClickListener {
                override fun onBackgroundClick() {
                    viewModel.onMapClick()
                }
            })
        binding.map.overlays.add(overlayBackground)
    }

    private val addMapMarker = { item: CourierMapMarker ->
        with(item) { addMapMarker(point.id, point.lat, point.long, getIcon(icon)) }
    }

    private val addMapMarkerWithIndex = { index: Int, item: CourierMapMarker ->
        with(item) {
            addMapMarker(
                point.id,
                point.lat,
                point.long,
                BitmapDrawable(resources, getBitmapIndexMarker(point.id, icon))
            )
        }
    }

    private fun initListeners() {
        binding.myLocation.setOnClickListener { navigateToMyLocation() }
    }

    private fun navigateToMyLocation() {
        removeMyLocationPoint()
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
        removeMyLocationPoint()
        addMyLocationPoint(point.latitude, point.longitude)
    }

    private fun removeMyLocationPoint() {
        binding.map.overlays
            .filterIsInstance<Marker>()
            .find { it.id == MY_LOCATION_ID }
            ?.apply { binding.map.overlays.remove(this) }
    }

    private fun addMyLocationPoint(latitude: Double, longitude: Double) {
        val point = MapPoint(MY_LOCATION_ID, latitude, longitude)
        val mapMarker = Empty(point, R.drawable.ic_warehouse_my_location)
        addMapMarker(mapMarker)
        binding.map.invalidate()
    }

    private fun updateMyLocation() {
        initAccessLocationPermissions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
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
            paint.color = ResourcesCompat.getColor(resources, R.color.lvl_1, null)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 40f

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
        val offsetBottom = offsetY * -2
        val topPx = mapView.context.resources.getDimensionPixelSize(top)
        val bottomPx = mapView.context.resources.getDimensionPixelSize(bottom) + offsetBottom
        val leftPx = mapView.context.resources.getDimensionPixelSize(left)
        val rightPx = mapView.context.resources.getDimensionPixelSize(right)

        val width = mapView.width
        val height = mapView.height
        val pScreenWidth = width - (leftPx + rightPx)
        val pScreenHeight = height - (topPx + bottomPx)
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