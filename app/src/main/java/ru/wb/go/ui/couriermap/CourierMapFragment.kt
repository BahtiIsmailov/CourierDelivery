package ru.wb.go.ui.couriermap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
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
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import ru.wb.go.BuildConfig
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
        private const val REQUEST_ERROR = 0
        private const val OSMD_BASE_PATH = "osmdroid"
        private const val OSMD_BASE_TILES = "tiles"
        private const val MIN_ZOOM = 4.0
        private const val MAX_ZOOM = 20.0
        private const val DEFAULT_POINT_ZOOM = 13.0
        private const val SIZE_IN_PIXELS = 100
        private const val DEFAULT_ANIMATION_MS = 300L
    }

    private var _binding: MapFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = MapFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var mapController: IMapController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservable()
        initListeners()
        initPermission()
    }

    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var locationRequest: LocationRequest
    private var lastLocation: Location? = null

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
        if (!googleApiClient.isConnected) {
            googleApiClient.connect()
        }
    }

    override fun onStop() {
        super.onStop()
        googleApiClient.disconnect()
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var grand = true
            permissions.entries.forEach {
                if (!it.value) grand = false
            }
            if (grand) {
                initMapView()
                viewModel.onInitPermission()
            } else {
                viewModel.onDeniedPermission()
            }
        }

    private fun initPermission() {
        if (hasPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        ) {
            initMapView()
            viewModel.onInitPermission()
        } else {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )
            )

        }
    }

    private fun initMapView() {
        val config: IConfigurationProvider = Configuration.getInstance()
        config.osmdroidBasePath = createOsmdroidBasePath()
        config.osmdroidTileCache = createOsmdroidTilePath(config.osmdroidBasePath)
        config.userAgentValue = BuildConfig.APPLICATION_ID
        config.load(
            requireActivity(),
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        )

        binding.map.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        binding.map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapController = binding.map.controller
        mapController.setZoom(12.0)
        binding.map.setBuiltInZoomControls(false)
        binding.map.setMultiTouchControls(true)
        binding.map.minZoomLevel = MIN_ZOOM
        binding.map.maxZoomLevel = MAX_ZOOM
        binding.map.setUseDataConnection(true)
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

    private fun addMapMarker(id: String, lat: Double, long: Double, icon: Int) {
        val markerMap = Marker(binding.map)
        markerMap.setOnMarkerClickListener(onMarkerClickListener)
        markerMap.id = id
        markerMap.icon = getIcon(icon)
        markerMap.position = GeoPoint(lat, long)
        binding.map.overlays.add(markerMap)
    }

    private fun getIcon(idRes: Int) = AppCompatResources.getDrawable(requireContext(), idRes)

    private fun navigateToMarker(id: String) {
        binding.map.overlays.find { (it as Marker).id == id }?.apply {
            mapController.setCenter((this as Marker).position)
        }
        mapController.setZoom(16.0)
        binding.map.invalidate()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObservable() {
        viewModel.mapState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierMapState.NavigateToMarker -> navigateToMarker(it.id)
                is CourierMapState.UpdateMarkers -> updateMarkers(it.points)
                is CourierMapState.NavigateToPointZoom -> navigateToPointZoom(it.mapPoint)
                CourierMapState.NavigateToMyLocation -> navigateToMyLocation()
                CourierMapState.UpdateMyLocation -> updateMyLocation()
                is CourierMapState.UpdateMyLocationPoint -> updateMyLocationPoint(it.point)
                is CourierMapState.ZoomToBoundingBox -> zoomToCenterBoundingBox(it.boundingBox)
                is CourierMapState.NavigateToPoint -> navigateToPoint(it.mapPoint)
            }
        }
    }

    private fun navigateToPointZoom(it: MapPoint) {
        val point = GeoPoint(it.lat, it.long)
        mapController.setZoom(DEFAULT_POINT_ZOOM)
        mapController.setCenter(point)
        binding.map.invalidate()
    }

    private fun navigateToPoint(it: MapPoint) {
        val point = GeoPoint(it.lat, it.long)
        mapController.animateTo(point, null, DEFAULT_ANIMATION_MS)
        binding.map.invalidate()
    }

    private fun zoomToCenterBoundingBox(boundingBox: BoundingBox) {
        with(binding.map) {
            if (height > 0) {
                zoomToBoundingBox(boundingBox, false, SIZE_IN_PIXELS)
            } else {
                viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        zoomToBoundingBox(boundingBox, false, SIZE_IN_PIXELS)
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
        with(item) { addMapMarker(point.id, point.lat, point.long, icon) }
    }

    private fun initListeners() {
        binding.myLocation.setOnClickListener { navigateToMyLocation() }
    }

    private fun navigateToMyLocation() {
        if (lastLocation == null) {
            navigateToDefault()
        } else {
            navigateToPoint(
                MapPoint(MY_LOCATION_ID, lastLocation!!.latitude, lastLocation!!.longitude)
            )
            addMyLocationPoint(lastLocation!!.latitude, lastLocation!!.longitude)
        }
    }

    private fun navigateToDefault() {
        initMapView()
        val coordinateMoscow = moscowCoordinatePoint()
        navigateToPoint(
            MapPoint(
                MY_LOCATION_ID,
                coordinateMoscow.latitude,
                coordinateMoscow.longitude
            )
        )
        addMyLocationPoint(coordinateMoscow.latitude, coordinateMoscow.longitude)
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
        addMapMarker(MY_LOCATION_ID, latitude, longitude, R.drawable.ic_warehouse_my_location)
        binding.map.invalidate()
    }

    private fun updateMyLocation() {
        if (lastLocation == null) {
            viewModel.onForcedLocationUpdateDefault()
        } else {
            viewModel.onForcedLocationUpdate(
                CoordinatePoint(lastLocation!!.latitude, lastLocation!!.longitude)
            )
        }
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
    }

    private fun isLocationPermissionGranted() = ActivityCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (isLocationPermissionGranted()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, locationListener()
            )
        }
    }

    private fun locationListener(): (Location) -> Unit =
        { viewModel.onForcedLocationUpdate(CoordinatePoint(it.latitude, it.longitude)) }

    override fun onConnectionSuspended(p0: Int) {}

    class OverlayBackground(private val listener: OnBackgroundClickListener) : Overlay() {

        interface OnBackgroundClickListener {
            fun onBackgroundClick()
        }

        override fun onSingleTapConfirmed(event: MotionEvent, mapView: MapView): Boolean {
            listener.onBackgroundClick()
            return false
        }

    }

}