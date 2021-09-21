package ru.wb.perevozka.ui.courierintransit

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_UP
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import ru.wb.perevozka.BuildConfig
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.CourierIntransitFragmentBinding
import ru.wb.perevozka.ui.courierunloading.CourierUnloadingScanParameters
import ru.wb.perevozka.ui.scanner.hasPermissions
import ru.wb.perevozka.utils.LogUtils
import ru.wb.perevozka.views.ProgressButtonMode


class CourierIntransitFragment : Fragment() {

    private val viewModel by viewModel<CourierIntransitViewModel>()

    private var _binding: CourierIntransitFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CourierIntransitAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: RecyclerView.SmoothScroller
    private lateinit var progressDialog: AlertDialog
    private var shortAnimationDuration: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierIntransitFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var mapController: IMapController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecyclerView()
        initObservable()
        initListeners()
        initProgressDialog()
        initPermission()
        shortAnimationDuration = resources.getInteger(android.R.integer.config_longAnimTime)
    }

    private fun initView() {
        binding.toolbarLayout.back.visibility = INVISIBLE
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var grang = true
            permissions.entries.forEach {
                if (!it.value) {
                    grang = false
                }
            }
            if (grang) {
                initMapView()
            }
        }

    private fun initPermission() {
        if (hasPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            initMapView()
        } else {
            requestMultiplePermissions.launch(
                arrayOf(
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
        Configuration.getInstance().userAgentValue =
            BuildConfig.APPLICATION_ID
        binding.map.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        binding.map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        binding.map.setMultiTouchControls(true)
        mapController = binding.map.controller
        mapController.setZoom(12.0)
        binding.map.setBuiltInZoomControls(true)
        binding.map.setMultiTouchControls(true)
        //binding.map.setBuiltInZoomControls(zoomControl, OverlayLayoutParams.RIGHT | OverlayLayoutParams.BOTTOM)
    }

    private fun initMapMarker(id: String, lat: Double, long: Double) {
        val marker = Marker(binding.map)
        marker.setOnMarkerClickListener { marker, _ ->
            viewModel.onItemClick(marker.id.toInt())
            true
        }
        marker.id = id
        marker.icon = normalMarkerIcon()
        val point = GeoPoint(lat, long)
        marker.position = point
        binding.map.overlays.add(marker)
    }

    private fun normalMarkerIcon() =
        AppCompatResources.getDrawable(requireContext(), R.drawable.ic_point_pvz)


    private fun setSelectedMarker(id: String, isSelected: Boolean) {
        binding.map.overlays.forEach {
            val marker = it as Marker
            marker.icon = if (marker.id == id && isSelected) selectedMarkerIcon()
            else normalMarkerIcon()
        }
        binding.map.overlays.find { (it as Marker).id == id }?.apply {
            binding.map.overlays.remove(this)
            binding.map.overlays.add(this)
            mapController.animateTo((this as Marker).position)
        }
        mapController.setZoom(16.0)
        binding.map.invalidate()
    }

    private fun selectedMarkerIcon() =
        AppCompatResources.getDrawable(requireContext(), R.drawable.ic_point_pvz_selected)

    @SuppressLint("NotifyDataSetChanged")
    private fun initObservable() {

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarTitle.text = it.label
        }

        viewModel.intransitTime.observe(viewLifecycleOwner) {
            when (it) {
                is CourierIntransitTimeState.Time -> {
                    binding.time.text = it.time
                }
            }
        }

        viewModel.orderDetails.observe(viewLifecycleOwner) {
            when (it) {
                is CourierIntransitItemState.InitItems -> {
                    binding.deliveryTotalCount.text = it.boxTotal
                    binding.emptyList.visibility = GONE
                    binding.routes.visibility = VISIBLE
                    val callback = object : CourierIntransitAdapter.OnItemClickCallBack {
                        override fun onItemClick(index: Int) {
                            viewModel.onItemClick(index)
                        }
                    }
                    adapter = CourierIntransitAdapter(requireContext(), it.items, callback)
                    binding.routes.adapter = adapter
                }
                is CourierIntransitItemState.Empty -> {
                    binding.emptyList.visibility = VISIBLE
                    binding.routes.visibility = GONE
                }
                is CourierIntransitItemState.UpdateItems -> {
                    adapter.setData(it.items)
                    adapter.notifyDataSetChanged()
                    binding.routes.scrollToPosition(it.position)
                }
            }
        }

        viewModel.mapPoint.observe(viewLifecycleOwner) {
            when (it) {
                is CourierIntransitMapPoint.InitMapPoint -> {
                    it.points.forEach { item ->
                        initMapMarker(
                            item.id,
                            item.lat,
                            item.long
                        )
                    }
                    val point =
                        GeoPoint(it.startNavigation.point.x, it.startNavigation.point.y)
                    mapController.setZoom(it.startNavigation.radius * 6)
                    mapController.animateTo(point)
                    binding.map.invalidate()
                }
                is CourierIntransitMapPoint.NavigateToPoint -> {
                    setSelectedMarker(it.id, it.isSelected)
                }
            }
        }

        viewModel.progressState.observe(viewLifecycleOwner) {
            when (it) {
                CourierIntransitProgressState.Progress -> showProgressDialog()
                CourierIntransitProgressState.ProgressComplete -> closeProgressDialog()
            }
        }

        viewModel.navigationState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierIntransitNavigationState.NavigateToDialogConfirm ->
                    showConfirmDialog(it.title, it.message)
                is CourierIntransitNavigationState.NavigateToDialogInfo ->
                    showEmptyOrderDialog(it.title, it.message, it.button)
                CourierIntransitNavigationState.NavigateToMap -> {
                    crossFade(binding.mapLayout, binding.scannerLayout)
                    binding.scanQrPvz.setState(ProgressButtonMode.ENABLE)
                }
                CourierIntransitNavigationState.NavigateToScanner -> {
                    crossFade(binding.scannerLayout, binding.mapLayout)
                    binding.scanQrPvz.setState(ProgressButtonMode.DISABLE)
                }
                is CourierIntransitNavigationState.NavigateToUnloadingScanner -> {
                    findNavController().navigate(CourierIntransitFragmentDirections.actionCourierIntransitFragmentToCourierUnloadingScanFragment(
                        CourierUnloadingScanParameters(it.officeId)
                    ))
                }
            }
        }

    }

    private fun crossFade(showView: View, hideView: View) {
        showView.apply {
            alpha = 0f
            visibility = VISIBLE
            animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }
        hideView.animate()
            .alpha(0f)
            .setDuration(shortAnimationDuration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    hideView.visibility = GONE
                }
            })
    }

    private fun initListeners() {
        binding.toolbarLayout.back.setOnClickListener { findNavController().popBackStack() }
        binding.scanQrPvz.setOnClickListener { viewModel.scanQrPvzClick() }
        binding.closeScannerLayout.setOnClickListener { viewModel.closeScannerClick() }
    }

    // TODO: 20.08.2021 переработать
    private fun initProgressDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomProgressAlertDialog)
        val viewGroup: ViewGroup = binding.routes
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.custom_progress_layout_dialog, viewGroup, false)
        builder.setView(dialogView)
        progressDialog = builder.create()
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == ACTION_UP) {
                progressDialog.dismiss()
                viewModel.onCancelLoadClick()
            }
            true
        }
    }

    private fun closeProgressDialog() {
        if (progressDialog.isShowing) progressDialog.dismiss()
    }

    private fun showProgressDialog() {
        progressDialog.show()
    }

    // TODO: 27.08.2021 переработать
    private fun showEmptyOrderDialog(title: String, message: String, button: String) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val viewGroup: ViewGroup = binding.layout
        val dialogView: View =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.custom_layout_dialog_info_result, viewGroup, false)
        val titleText: TextView = dialogView.findViewById(R.id.title)
        val messageText: TextView = dialogView.findViewById(R.id.message)
        val positive: Button = dialogView.findViewById(R.id.positive)

        builder.setView(dialogView)
        val alertDialog: AlertDialog = builder.create()
        titleText.text = title
        messageText.text = message
        positive.setOnClickListener {
            alertDialog.dismiss()
            findNavController().popBackStack()
        }
        positive.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        positive.text = button
        alertDialog.show()
    }

    // TODO: 27.08.2021 переработать
    private fun showConfirmDialog(title: String, message: String) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val viewGroup: ViewGroup = binding.layout
        val dialogView: View =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.custom_layout_dialog_result, viewGroup, false)
        val titleText: TextView = dialogView.findViewById(R.id.title)
        val messageText: TextView = dialogView.findViewById(R.id.message)
        val negative: Button = dialogView.findViewById(R.id.negative)
        val positive: Button = dialogView.findViewById(R.id.positive)

        builder.setView(dialogView)
        val alertDialog: AlertDialog = builder.create()
        titleText.text = title
        messageText.text = message
        negative.setOnClickListener { alertDialog.dismiss() }
        negative.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        negative.text = getString(R.string.courier_orders_details_dialog_negative_button)
        positive.setOnClickListener {
            alertDialog.dismiss()
            viewModel.confirmTakeOrderClick()
        }
        positive.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        positive.text = getString(R.string.courier_orders_details_dialog_positive_button)
        alertDialog.show()
    }

    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.routes.layoutManager = layoutManager
        binding.routes.setHasFixedSize(true)
        initSmoothScroller()
    }

    private fun initSmoothScroller() {
        smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
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

    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }


    override fun onStart() {
        super.onStart()
        viewModel.onStartScanner()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStopScanner()
    }

}