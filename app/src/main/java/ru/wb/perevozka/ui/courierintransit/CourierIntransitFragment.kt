package ru.wb.perevozka.ui.courierintransit

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_UP
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Completable
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.perevozka.R
import ru.wb.perevozka.adapters.DefaultAdapterDelegate
import ru.wb.perevozka.databinding.CourierIntransitFragmentBinding
import ru.wb.perevozka.mvvm.model.base.BaseItem
import ru.wb.perevozka.ui.couriercompletedelivery.CourierCompleteDeliveryParameters
import ru.wb.perevozka.ui.courierintransit.delegates.*
import ru.wb.perevozka.ui.courierunloading.CourierUnloadingScanParameters
import ru.wb.perevozka.ui.splash.NavDrawerListener
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.views.ProgressButtonMode
import ru.wb.perevozka.views.ProgressImageButtonMode


class CourierIntransitFragment : Fragment() {

    private val viewModel by viewModel<CourierIntransitViewModel>()

    private var _binding: CourierIntransitFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DefaultAdapterDelegate
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: RecyclerView.SmoothScroller
    private val itemCallback = object : OnCourierIntransitCallback {
        override fun onPickToPointClick(idItem: Int) {
            viewModel.onItemClick(idItem)
        }
    }

    private lateinit var progressDialog: AlertDialog
    private var shortAnimationDuration: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierIntransitFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecyclerView()
        initAdapter()
        initObservable()
        initListeners()
        initProgressDialog()
        shortAnimationDuration = resources.getInteger(android.R.integer.config_longAnimTime)
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lock()
        binding.toolbarLayout.back.visibility = INVISIBLE
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObservable() {

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarTitle.text = it.label
        }

        viewModel.beepEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierIntransitScanOfficeBeepState.Office -> scanOfficeAccepted()
                CourierIntransitScanOfficeBeepState.UnknownOffice -> scanOfficeFailed()
            }
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
                    displayItems(it.items)
                }
                is CourierIntransitItemState.Empty -> {
                    binding.emptyList.visibility = VISIBLE
                    binding.routes.visibility = GONE
                }
                is CourierIntransitItemState.UpdateItems -> {
                    displayItems(it.items)
                    binding.routes.scrollToPosition(it.position)
                }
                CourierIntransitItemState.CompleteDelivery -> {
                    binding.scanQrPvz.visibility = INVISIBLE
                    binding.scanQrPvzComplete.visibility = VISIBLE
                    binding.completeDelivery.visibility = VISIBLE
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
                    binding.scanQrPvzComplete.setState(ProgressImageButtonMode.ENABLED)
                    binding.completeDelivery.setState(ProgressButtonMode.ENABLE)
                }
                CourierIntransitNavigationState.NavigateToScanner -> {
                    crossFade(binding.scannerLayout, binding.mapLayout)
                    binding.scanQrPvz.setState(ProgressButtonMode.DISABLE)
                    binding.scanQrPvzComplete.setState(ProgressImageButtonMode.DISABLED)
                    binding.completeDelivery.setState(ProgressButtonMode.DISABLE)
                }
                is CourierIntransitNavigationState.NavigateToUnloadingScanner -> {
                    findNavController().navigate(
                        CourierIntransitFragmentDirections.actionCourierIntransitFragmentToCourierUnloadingScanFragment(
                            CourierUnloadingScanParameters(it.officeId)
                        )
                    )
                }
                is CourierIntransitNavigationState.NavigateToCompleteDelivery -> {
                    findNavController().navigate(
                        CourierIntransitFragmentDirections.actionCourierIntransitFragmentToCourierCompleteDeliveryFragment(
                            CourierCompleteDeliveryParameters(
                                it.amount,
                                it.unloadedCount,
                                it.fromCount
                            )
                        )
                    )
                }
            }
        }

    }

    private fun displayItems(items: List<BaseItem>) {
        adapter.clear()
        adapter.addItems(items)
        adapter.notifyDataSetChanged()
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
        binding.closeScannerLayout.setOnClickListener { viewModel.closeScannerClick() }
        binding.scanQrPvz.setOnClickListener { viewModel.scanQrPvzClick() }
        binding.scanQrPvzComplete.setOnClickListener { viewModel.scanQrPvzClick() }
        binding.completeDelivery.setOnClickListener { viewModel.completeDeliveryClick() }
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

    private fun initAdapter() {
        adapter = with(DefaultAdapterDelegate()) {
            addDelegate(CourierIntransitEmptyDelegate(requireContext(), itemCallback))
            addDelegate(CourierIntransitCompleteDelegate(requireContext(), itemCallback))
            addDelegate(CourierIntransitFaildDelegate(requireContext(), itemCallback))
            addDelegate(CourierIntransitIsUnloadedDelegate(requireContext(), itemCallback))
        }
        binding.routes.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        //MediaPlayer.create(context, resId)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStartScanner()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStopScanner()
    }

    private fun scanOfficeAccepted() {
        play(R.raw.qr_office_accepted)
    }

    private fun scanOfficeFailed() {
        play(R.raw.qr_office_failed)
    }

    private fun play(resId: Int) {
        Completable.create { MediaPlayer.create(context, resId).start() }.subscribe()
    }

}