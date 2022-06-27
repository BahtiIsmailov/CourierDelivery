package ru.wb.go.ui.courierintransit

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.*
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.adapters.DefaultAdapterDelegate
import ru.wb.go.databinding.CourierIntransitFragmentBinding
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.BaseServiceFragment
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.couriercompletedelivery.CourierCompleteDeliveryParameters
import ru.wb.go.ui.courierintransit.delegates.*
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.dialogs.ProgressDialogFragment
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.managers.ErrorDialogData


class CourierIntransitFragment :
    BaseServiceFragment<CourierIntransitViewModel, CourierIntransitFragmentBinding>(
        CourierIntransitFragmentBinding::inflate
    ) {

    override val viewModel by viewModel<CourierIntransitViewModel>()

    private lateinit var adapter: DefaultAdapterDelegate
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: RecyclerView.SmoothScroller
    private val itemCallback = object : OnCourierIntransitCallback {
        override fun onPickToPointClick(idItem: Int) {
            viewModel.onItemOfficeClick(idItem)
        }
    }
//    var schedul = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecyclerView()
        initAdapter()
        initObservable()
        initListeners()
        initReturnDialogResult()
        viewModel.update()
    }

    private fun initReturnDialogResult() {
        setFragmentResultListener(DIALOG_INFO_TAG) { _, bundle ->
            if (bundle.containsKey(DialogInfoFragment.DIALOG_INFO_BACK_KEY)) {
                viewModel.onErrorDialogConfirmClick()
            }
        }
//        setFragmentResultListener(ADDRESS_DETAIL_SCHEDULE) { _, bundle ->
//              schedul = bundle.getString(ADDRESS_DETAIL_SCHEDULE_FOR_INTRANSIT).toString()
//        }

    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lockNavDrawer()
    }

    private fun setColorNavigatorTint(@ColorRes colorRes: Int) {
        binding.navigatorButton.setColorFilter(
            ContextCompat.getColor(requireContext(), colorRes),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObservable() {

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.currentOrderId.text = it.label
        }

        viewModel.navigateToErrorDialog.observe(viewLifecycleOwner) {
            showDialogInfo(it)
        }

        viewModel.navigatorState.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierIntransitNavigatorUIState.Disable -> {
                    binding.navigatorButton.isEnabled = false
                    binding.addressDetailSchedule.isVisible = false
                    binding.navigatorButton.setImageResource(R.drawable.ic_navigator)
                }
                is CourierIntransitNavigatorUIState.Enable -> {
                    binding.navigatorButton.isEnabled = true
                    binding.timeWorkDetail.text = state.scheduleOrder
                    binding.addressDetailSchedule.isVisible = true
                    binding.navigatorButton.setImageResource(R.drawable.ic_bottom_navigator_color)
                }
            }
        }

        viewModel.beepEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierIntransitScanOfficeBeepState.Office -> scanOfficeAccepted()
                CourierIntransitScanOfficeBeepState.UnknownQrOffice -> scanOfficeFailed()
                CourierIntransitScanOfficeBeepState.WrongOffice -> scanWrongOffice()
            }
        }

        viewModel.intransitTime.observe(viewLifecycleOwner) {
            when (it) {
                is CourierIntransitTimeState.Time -> {
                    binding.mapTimer.text = it.time
                }
            }
        }

        viewModel.isEnableBottomState.observe(viewLifecycleOwner) { state ->
            binding.scanQrPvzCompleteButton.isEnabled = state
            binding.completeDeliveryButton.isEnabled = state
        }

        viewModel.intransitOrders.observe(viewLifecycleOwner) {
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
                is CourierIntransitItemState.UpdateItems -> displayItems(it.items)
                is CourierIntransitItemState.CompleteDelivery -> {
                    binding.navigatorButton.visibility = INVISIBLE
                    binding.scanQrPvzButton.visibility = INVISIBLE
                    binding.scanQrPvzCompleteButton.visibility = VISIBLE
                    binding.completeDeliveryButton.visibility = VISIBLE
                }
                is CourierIntransitItemState.ScrollTo -> binding.routes.scrollToPosition(it.position)

            }
        }

        viewModel.waitLoader.observe(viewLifecycleOwner) {
            when (it) {
                WaitLoader.Wait -> showProgressDialog()
                WaitLoader.Complete -> closeProgressDialog()
            }
        }

        viewModel.navigateToDialogConfirmInfo.observe(viewLifecycleOwner) {
            showDialogConfirmInfo(it.type, it.title, it.message, it.positive, it.negative)
        }

        viewModel.navigationState.observe(viewLifecycleOwner) {
            when (it) {
                CourierIntransitNavigationState.NavigateToScanner -> {
                    binding.scanQrPvzButton.isEnabled = false
                    binding.navigatorButton.isEnabled = false
                    binding.scanQrPvzCompleteButton.isEnabled = false
                    binding.completeDeliveryButton.isEnabled = false

                    binding.holdList.visibility = VISIBLE
                    findNavController().navigate(
                        CourierIntransitFragmentDirections.actionCourierIntransitFragmentToCourierIntransitOfficeScannerFragment()
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
                is CourierIntransitNavigationState.NavigateToNavigator -> {
                    val geoLocation = Uri.parse("geo:${it.latitude},${it.longitude}")
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = geoLocation
                    startActivity(intent)
                }
            }
        }

    }

    private fun showDialogInfo(
        errorDialogData: ErrorDialogData
    ) {
        DialogInfoFragment.newInstance(
            resultTag = errorDialogData.dlgTag,
            type = errorDialogData.type,
            title = errorDialogData.title,
            message = errorDialogData.message,
            positiveButtonName = requireContext().getString(R.string.ok_button_title)
        ).show(parentFragmentManager, DIALOG_INFO_TAG)
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun displayItems(items: List<BaseItem>) {
        adapter.clear()
        adapter.addItems(items)
        adapter.notifyDataSetChanged()
    }

    private fun initListeners() {
        binding.navigatorButton.setOnClickListener { viewModel.onNavigatorClick() }
        binding.scanQrPvzButton.setOnClickListener { viewModel.onScanQrPvzClick() }
        binding.scanQrPvzCompleteButton.setOnClickListener { viewModel.onScanQrPvzClick() }
        binding.completeDeliveryButton.setOnClickListener { viewModel.onCompleteDeliveryClick() }
    }

    private fun showProgressDialog() {
        val progressDialog = ProgressDialogFragment.newInstance()
        progressDialog.show(parentFragmentManager, ProgressDialogFragment.PROGRESS_DIALOG_TAG)
    }

    private fun closeProgressDialog() {
        parentFragmentManager.findFragmentByTag(ProgressDialogFragment.PROGRESS_DIALOG_TAG)?.let {
            if (it is ProgressDialogFragment) it.dismiss()
        }
    }

    private fun showDialogConfirmInfo(
        style: Int,
        title: String,
        message: String,
        positiveButtonName: String,
        negativeButtonName: String
    ) {
        DialogConfirmInfoFragment.newInstance(
            type = style,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName,
            negativeButtonName = negativeButtonName
        ).show(parentFragmentManager, DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_TAG)
    }

    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.routes.layoutManager = layoutManager
        binding.routes.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )
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
            addDelegate(CourierIntransitUndeliveredAllDelegate(requireContext(), itemCallback))
            addDelegate(CourierIntransitUnloadingExpectsDelegate(requireContext(), itemCallback))
        }
        binding.routes.adapter = adapter
    }

    private fun scanOfficeAccepted() {
        viewModel.play(R.raw.qr_office_accepted)
    }

    private fun scanOfficeFailed() {
        viewModel.play(R.raw.qr_office_failed)
    }

    private fun scanWrongOffice() {
        viewModel.play(R.raw.wrongoffice)
    }


}