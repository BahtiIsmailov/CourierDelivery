package ru.wb.go.ui.courierloading

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.CourierLoadingFragmentBinding
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.BaseServiceFragment
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.app.OnCourierScanner
import ru.wb.go.ui.courierstartdelivery.CourierStartDeliveryParameters
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment.Companion.DIALOG_CONFIRM_INFO_NEGATIVE_KEY
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment.Companion.DIALOG_CONFIRM_INFO_POSITIVE_KEY
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment.Companion.DIALOG_CONFIRM_INFO_TAG
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_BACK_KEY
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.ProgressDialogFragment
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.managers.ErrorDialogData

class CourierLoadingScanFragment :
    BaseServiceFragment<CourierLoadingScanViewModel, CourierLoadingFragmentBinding>(
        CourierLoadingFragmentBinding::inflate
    ) {

    private lateinit var addressAdapter: CourierLoadingDetailsAdapter
    private lateinit var addressLayoutManager: LinearLayoutManager
    private lateinit var addressSmoothScroller: RecyclerView.SmoothScroller

    private lateinit var bottomSheetDetails: BottomSheetBehavior<FrameLayout>

    override val viewModel by viewModel<CourierLoadingScanViewModel>()

    private val bottomSheetDetailsCallback = object :
        BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                viewModel.onCloseDetailsClick()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        initObserver()
        initReturnResult()
        initRecyclerViewDetails()
        initBottomSheet()
    }

    private fun initBottomSheet() {
        binding.detailsLayout.visibility = VISIBLE
        bottomSheetDetails = BottomSheetBehavior.from(binding.detailsGoals)
        bottomSheetDetails.skipCollapsed = true
        bottomSheetDetails.addBottomSheetCallback(bottomSheetDetailsCallback)
        bottomSheetDetails.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun initRecyclerViewDetails() {
        addressLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.boxDetails.layoutManager = addressLayoutManager
        binding.boxDetails.setHasFixedSize(true)
        initSmoothScrollerAddress()
    }

    private fun initSmoothScrollerAddress() {
        addressSmoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference() = SNAP_TO_START
        }
    }

    private fun initReturnResult() {
        setFragmentResultListener(DialogInfoFragment.DIALOG_INFO_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DIALOG_INFO_BACK_KEY)) {
                viewModel.onStartScanner()
            }
        }

        setFragmentResultListener(DIALOG_INFO_TAG) { _, bundle ->
            if (viewModel.timeOut.value == true) {
                viewModel.returnToListOrderClick()
            } else
                if (bundle.containsKey(DIALOG_INFO_BACK_KEY)) {
                    viewModel.onErrorDialogConfirmClick()
                }
        }

        setFragmentResultListener(DIALOG_LOADING_CONFIRM_TAG) { _, bundle ->
            if (bundle.containsKey(DIALOG_CONFIRM_INFO_POSITIVE_KEY)) {
                viewModel.onConfirmLoadingClick()
            }
            if (bundle.containsKey(DIALOG_CONFIRM_INFO_NEGATIVE_KEY)) {
                viewModel.onCancelLoadingClick()
            }
        }

        setFragmentResultListener(DIALOG_TIME_IS_OUT_INFO_TAG) { _, bundle ->
            if (viewModel.timeOut.value == true) {
                viewModel.returnToListOrderClick()
            }
        }
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lockNavDrawer()
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.courier_order_scanner_label)
        binding.toolbarLayout.back.setOnClickListener { findNavController().popBackStack() }
        setFragmentResultListener(DialogInfoFragment.ROUTE_ID) { key, bundle ->
            binding.routeTV.text = bundle.getString("bundleKey")
        }
    }

    private fun initObserver() {
        viewModel.navigateToDialogInfo.observe(viewLifecycleOwner) {
            showDialogInfo(it)
        }

        viewModel.networkState.observe(viewLifecycleOwner) {
            val ic = when (it) {
                is NetworkState.Complete -> R.drawable.ic_inet_complete
                else -> R.drawable.ic_inet_failed
            }
            binding.toolbarLayout.noInternetImage.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), ic)
            )
        }

        viewModel.versionApp.observe(viewLifecycleOwner) {
            binding.toolbarLayout.versionApp.text = it
        }

        viewModel.orderTimer.observe(viewLifecycleOwner) {
            when (it) {
                is CourierLoadingScanTimerState.Timer -> {
                    binding.timeDigit.visibility = VISIBLE
                    binding.timer.visibility = VISIBLE
                    binding.timeDigit.text = it.timeDigit
                    binding.timer.setProgress(it.timeAnalog)
                }
                is CourierLoadingScanTimerState.TimeIsOut -> {
                    showTimeIsOutDialog(it.type, it.title, it.message, it.button)
                }
                is CourierLoadingScanTimerState.Stopped -> {
                    binding.timerLayout.visibility = View.GONE
                }
                is CourierLoadingScanTimerState.Info -> {
                    binding.gate.text = if (it.gate == "0"){
                        binding.gateDigit.isGone = true
                        requireContext().getText(R.string.courier_loading_pandus)
                    }else {
                        binding.gateDigit.isVisible = true
                        binding.gateDigit.text = it.gate
                         requireContext().getText(R.string.courier_loading_gate)
                    }
                }
            }
        }

        val navigationObserver = Observer<CourierLoadingScanNavAction> { state ->
            when (state) {
                CourierLoadingScanNavAction.NavigateToConfirmDialog -> {
                    showDialogConfirmInfo(
                        DialogInfoStyle.INFO.ordinal,
                        getString(R.string.courier_loading_dialog_done_title),
                        getString(R.string.courier_loading_dialog_done_message),
                        getString(R.string.courier_order_scanner_dialog_positive_button),
                        getString(R.string.courier_order_scanner_dialog_negative_button)
                    )
                }
                CourierLoadingScanNavAction.NavigateToWarehouse ->
                    findNavController().navigate(CourierLoadingScanFragmentDirections.actionCourierScannerLoadingScanFragmentToCourierWarehouseFragment())
                is CourierLoadingScanNavAction.NavigateToStartDelivery ->
                    findNavController().navigate(
                        CourierLoadingScanFragmentDirections.actionCourierScannerLoadingScanFragmentToCourierStartDeliveryFragment(
                            CourierStartDeliveryParameters(state.amount, state.count)
                        )
                    )
                is CourierLoadingScanNavAction.InitAndShowLoadingItems -> {
                    binding.pvzCountTitle.text = state.pvzCount
                    binding.boxCountTitle.text = state.boxCount
                    addressAdapter = CourierLoadingDetailsAdapter(requireContext(), state.items)
                    binding.boxDetails.adapter = addressAdapter

                    bottomSheetDetails.state = BottomSheetBehavior.STATE_EXPANDED
                }
                CourierLoadingScanNavAction.HideLoadingItems -> {
                    bottomSheetDetails.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
        }
        viewModel.navigationEvent.observe(viewLifecycleOwner, navigationObserver)

        viewModel.beepEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierLoadingScanBeepState.BoxFirstAdded -> beepFirstSuccess()
                CourierLoadingScanBeepState.BoxAdded -> beepSuccess()
                CourierLoadingScanBeepState.UnknownBox -> beepWrongBox()
                CourierLoadingScanBeepState.UnknownQR -> beepUnknownQr()
            }
        }

        viewModel.waitLoader.observe(viewLifecycleOwner) { state ->
            when (state) {
                WaitLoader.Wait -> showProgressDialog()
                WaitLoader.Complete -> closeProgressDialog()
            }
        }

        viewModel.boxDataStateUI.observe(viewLifecycleOwner) { state ->
            binding.qrCode.text = state.boxId
            binding.boxAddress.text = state.address
            binding.totalBoxes.text = state.totalBoxes
        }

        viewModel.completeButtonState.observe(viewLifecycleOwner) {
            binding.completeButton.isEnabled = it
        }

        viewModel.fragmentStateUI.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierLoadingScanBoxState.InitScanner -> {
                    with(binding) {
                        timerLayout.visibility = VISIBLE
                        boxInfoLayout.visibility = View.GONE
                        ribbonStatus.setText(R.string.courier_loading_init_scanner)
                        ribbonStatus.setBackgroundColor(getColor(R.color.colorPrimary))
                    }

                }
                is CourierLoadingScanBoxState.LoadInCar -> {
                    holdBackButtonOnScanBox()
                    with(binding) {
                        ribbonStatus.setText(R.string.courier_loading_load_in_car)
                        ribbonStatus.setBackgroundColor(getColor(R.color.green))
                        qrCode.setTextColor(getColor(R.color.primary))
                        timerLayout.visibility = View.GONE
                        boxInfoLayout.visibility = VISIBLE
                        counterLayout.isEnabled = true
                    }

                }
                is CourierLoadingScanBoxState.ForbiddenTakeWithTimer -> {
                    with(binding) {
                        timerLayout.visibility = VISIBLE
                        boxInfoLayout.visibility = View.GONE
                        ribbonStatus.setText(R.string.courier_loading_forbidden_take)
                        ribbonStatus.setBackgroundColor(getColor(R.color.red))
                    }

                }
                is CourierLoadingScanBoxState.ForbiddenTakeBox -> {
                    holdBackButtonOnScanBox()
                    with(binding) {
                        timerLayout.visibility = View.GONE
                        boxInfoLayout.visibility = View.VISIBLE
                        ribbonStatus.setText(R.string.courier_loading_forbidden_take)
                        ribbonStatus.setBackgroundColor(getColor(R.color.red))
                        qrCode.setTextColor(getColor(R.color.primary))
                        boxAddress.setTextColor(getColor(R.color.primary))
                    }

                }
                CourierLoadingScanBoxState.NotRecognizedQrWithTimer -> {
                    with(binding){
                        timerLayout.visibility = View.VISIBLE
                        boxInfoLayout.visibility = View.GONE
                        ribbonStatus.setText(R.string.courier_loading_not_recognized_qr)
                        ribbonStatus.setBackgroundColor(getColor(R.color.yellow))
                    }

                }
                is CourierLoadingScanBoxState.NotRecognizedQr -> {
                    holdBackButtonOnScanBox()
                    with(binding){
                        timerLayout.visibility = View.GONE
                        boxInfoLayout.visibility = View.VISIBLE
                        ribbonStatus.setText(R.string.courier_loading_not_recognized_qr)
                        ribbonStatus.setBackgroundColor(getColor(R.color.yellow))
                    }
                }
            }
        }
    }

    private fun getColor(colorId: Int) = ContextCompat.getColor(requireContext(), colorId)

    private fun holdBackButtonOnScanBox() {
        binding.toolbarLayout.back.visibility = View.INVISIBLE
        (activity as OnCourierScanner).holdBackButtonOnScanBox()
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

    private fun showTimeIsOutDialog(
        type: Int,
        title: String,
        message: String,
        positiveButtonName: String
    ) {
        DialogInfoFragment.newInstance(
            DIALOG_TIME_IS_OUT_INFO_TAG,
            type,
            title,
            message,
            positiveButtonName
        )
            .show(parentFragmentManager, DIALOG_INFO_TAG)
    }

    private fun showDialogConfirmInfo(
        type: Int,
        title: String,
        message: String,
        positiveButtonName: String,
        negativeButtonName: String
    ) {
        DialogConfirmInfoFragment.newInstance(
            resultTag = DIALOG_LOADING_CONFIRM_TAG,
            type = type,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName,
            negativeButtonName = negativeButtonName
        ).show(parentFragmentManager, DIALOG_CONFIRM_INFO_TAG)
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

    private fun initListener() {
        binding.completeButton.setOnClickListener {
            viewModel.onCompleteLoaderClicked()
        }
        binding.counterLayout.setOnClickListener { viewModel.onCounterBoxClicked() }
        binding.detailsClose.setOnClickListener { viewModel.onCloseDetailsClick() }
    }

    companion object {
        const val DIALOG_LOADING_CONFIRM_TAG = "DIALOG_LOADING_CONFIRM_TAG"
        const val DIALOG_TIME_IS_OUT_INFO_TAG = "DIALOG_TIME_IS_OUT_INFO_TAG"
    }

    private fun beepFirstSuccess() {
        // TODO: 17.11.2021 выключено до замены мелодии
        //play(R.raw.qr_box_first_accepted)
    }

    private fun beepSuccess() {
        // TODO: 11.10.2021 ignore
//        play(R.raw.sound_scan_success)
    }

    private fun beepWrongBox() {
        viewModel.play(R.raw.unloading_unknown_box)
    }

    private fun beepUnknownQr() {
        viewModel.play(R.raw.unloading_scan_unknown_qr)
    }

}