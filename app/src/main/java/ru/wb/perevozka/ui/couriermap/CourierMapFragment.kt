package ru.wb.perevozka.ui.couriermap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem
import ru.wb.perevozka.BuildConfig
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.MapFragmentBinding
import ru.wb.perevozka.ui.scanner.hasPermissions
import ru.wb.perevozka.utils.LogUtils
import ru.wb.perevozka.utils.map.MapCircle
import ru.wb.perevozka.utils.map.MapPoint

class CourierMapFragment : Fragment(), GoogleApiClient.ConnectionCallbacks {

    private val viewModel by viewModel<CourierMapViewModel>()

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
        viewModel.onInitPermission()
    }

    private fun initMapMarker(id: String, lat: Double, long: Double, icon: Int) {
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
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObservable() {
        viewModel.mapState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierMapState.ZoomAllMarkers -> zoomAllPoint(it.startNavigation)
                is CourierMapState.NavigateToMarker -> navigateToMarker(it.id)
                is CourierMapState.UpdateMapMarkers -> updateMarkers(it.pointsState)
                is CourierMapState.NavigateToPoint -> navigateToPoint(it.mapPoint)
                CourierMapState.NavigateToMyLocation -> navigateToMyLocation()
            }
        }
    }

    private fun navigateToPoint(it: MapPoint) {
        val point = GeoPoint(it.lat, it.long)
        mapController.setZoom(14.0)
        mapController.setCenter(point)
    }

    private fun zoomAllPoint(it: MapCircle) {
        val point = with(it.point) { GeoPoint(x, y) }
        LogUtils { logDebugApp("zoomAllPoint approx " + it.radius) }
        mapController.setZoom(it.radius)
        mapController.setCenter(point)
    }

    private fun updateMarkers(mapPoints: List<CourierMapMarker>) {
        binding.map.overlays.clear()
        initMapMarkers(mapPoints)
    }

    private fun initMapMarkers(mapPoints: List<CourierMapMarker>) {
        mapPoints.forEach { item ->
            initMapMarker(
                item.point.id,
                item.point.lat,
                item.point.long,
                item.icon
            )
        }
    }

    private fun initListeners() {
        binding.myLocation.setOnClickListener { navigateToMyLocation() }
    }

    private fun navigateToMyLocation() {
        if (!serviceOnConnected) return
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
                navigateToPoint(MapPoint(MY_ID, location.latitude, location.longitude))
                initMapMarker(MY_ID, location.latitude, location.longitude, R.drawable.ic_pvz_my)
            }
        }
    }

    companion object {
        private const val REQUEST_ERROR = 0
        private const val MY_ID = "where_i_am"
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