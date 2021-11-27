package ru.wb.go.ui.couriermap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import ru.wb.go.BuildConfig
import ru.wb.go.R
import ru.wb.go.databinding.MapFragmentBinding
import ru.wb.go.ui.scanner.hasPermissions
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapPoint


class CourierMapFragment : Fragment(), GoogleApiClient.ConnectionCallbacks {

    private val viewModel by viewModel<CourierMapViewModel>()

    companion object {
        private const val REQUEST_ERROR = 0
        private const val MY_LOCATION_ID = "my_location_id"
        private const val MIN_ZOOM = 9.0
        private const val MAX_ZOOM = 20.0
        private const val DEFAULT_POINT_ZOOM = 13.0
        private const val SIZE_IN_PIXELS = 100
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleApiClient =
            GoogleApiClient.Builder(requireActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build()
    }

    override fun onStart() {
        super.onStart()
        googleApiClient.connect()
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
            if (grand) initMapView()
        }

    private fun initPermission() {
        if (hasPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            initMapView()
        } else {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )

        }
    }

    private fun initMapView() {
        Configuration.getInstance().load(
            requireActivity(),
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        binding.map.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        binding.map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapController = binding.map.controller
        mapController.setZoom(12.0)
        binding.map.setBuiltInZoomControls(false)
        binding.map.setMultiTouchControls(true)
        binding.map.minZoomLevel = MIN_ZOOM
        binding.map.maxZoomLevel = MAX_ZOOM
        binding.map.setUseDataConnection(true)
        viewModel.onInitPermission()
    }

    private fun drawMapMarker(id: String, lat: Double, long: Double, icon: Int) {
        val marker = Marker(binding.map)
        marker.setOnMarkerClickListener { marker, _ ->
            viewModel.onItemClick(marker.id)
            true
        }
        marker.id = id
        marker.icon = getIcon(icon)
        val point = GeoPoint(lat, long)
        marker.position = point
        binding.map.overlays.add(marker)
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
                is CourierMapState.NavigateToPoint -> navigateToPoint(it.mapPoint)
                CourierMapState.NavigateToMyLocation -> navigateToMyLocation()
                CourierMapState.UpdateMyLocation -> forcedLocationUpdate()
                is CourierMapState.UpdateAndNavigateToMyLocationPoint -> updateAndNavigateToMyLocationPoint(
                    it.point
                )
                is CourierMapState.ZoomToCenterBoundingBox -> zoomToCenterBoundingBox(it.boundingBox)
            }
        }
    }

    private fun navigateToPoint(it: MapPoint) {
        val point = GeoPoint(it.lat, it.long)
        mapController.setZoom(DEFAULT_POINT_ZOOM)
        mapController.setCenter(point)
        binding.map.invalidate()
    }

    private fun zoomToCenterBoundingBox(boundingBox: BoundingBox) {
        LogUtils { logDebugApp("zoomToCenterBoundingBox(boundingBox: BoundingBox) " + boundingBox.toString()) }
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
        mapPoints.forEach { item ->
            drawMapMarker(
                item.point.id,
                item.point.lat,
                item.point.long,
                item.icon
            )
        }
        //binding.map.postInvalidate()
    }

    private fun initListeners() {
        binding.myLocation.setOnClickListener { navigateToMyLocation() }
    }

    private fun navigateToMyLocation() {
        if (!serviceOnConnected) return //навигироваться по дефолту
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.numUpdates = 1
            locationRequest.interval = 0
            LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient,
                locationRequest
            ) { location ->
                LogUtils { logDebugApp("Location : " + location.latitude + " / " + location.longitude) }
                navigateToPoint(MapPoint(MY_LOCATION_ID, location.latitude, location.longitude))
                drawMapMarker(
                    MY_LOCATION_ID,
                    location.latitude,
                    location.longitude,
                    R.drawable.ic_my_location
                )
            }
        }
    }

    private fun updateAndNavigateToMyLocationPoint(point: CoordinatePoint) {
        binding.map.overlays.find { (it as Marker).id == MY_LOCATION_ID }?.apply {
            binding.map.overlays.remove(this)
        }
        drawMapMarker(
            MY_LOCATION_ID,
            point.latitude,
            point.longitude,
            R.drawable.ic_my_location
        )
    }

    private fun forcedLocationUpdate() {
        if (!serviceOnConnected) {
            viewModel.onForcedLocationUpdateDefault()
            return
        }
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.numUpdates = 1
            locationRequest.interval = 0
            LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient,
                locationRequest
            ) { location ->
                //LogUtils { logDebugApp("Location : " + location.latitude + " / " + location.longitude) }
                viewModel.onForcedLocationUpdate(
                    CoordinatePoint(
                        location.latitude,
                        location.longitude
                    )
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onResume() {
        super.onResume()
        binding.map.onResume()

        val googleApi = GoogleApiAvailability.getInstance()
        val errorCode = googleApi.isGooglePlayServicesAvailable(requireActivity())
        if (errorCode != ConnectionResult.SUCCESS) {
            googleApi.getErrorDialog(
                requireActivity(),
                errorCode,
                REQUEST_ERROR
            ) { requireActivity().finish() }.show()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    private var serviceOnConnected = false

    override fun onConnected(p0: Bundle?) {
        serviceOnConnected = true
        //binding.myLocation.visibility = View.VISIBLE
    }

    override fun onConnectionSuspended(p0: Int) {

    }

}