package ru.wb.go.ui.courierloading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.CourierLoadingFragmentBinding
import ru.wb.go.network.monitor.NetworkState
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
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.app.OnCourierScanner
import ru.wb.go.ui.app.OnSoundPlayer
import ru.wb.go.views.ProgressButtonMode

class CourierLoadingScanFragment : Fragment() {

    private val viewModel by viewModel<CourierLoadingScanViewModel>()

    private var _binding: CourierLoadingFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierLoadingFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        initObserver()
        initReturnResult()
    }

    private fun initReturnResult() {

        setFragmentResultListener(DialogInfoFragment.DIALOG_INFO_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DIALOG_INFO_BACK_KEY)) {
                isDialogActive = false
                viewModel.onStartScanner()
            }
        }

        setFragmentResultListener(DIALOG_ERROR_INFO_TAG) { _, bundle ->
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
            if (bundle.containsKey(DIALOG_INFO_BACK_KEY)) {
                viewModel.returnToListOrderClick()
            }
        }

    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lockNavDrawer()
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.courier_order_scanner_label)
        binding.toolbarLayout.back.setOnClickListener { findNavController().popBackStack() }
    }

    private var isDialogActive: Boolean = false

    override fun onStart() {
        super.onStart()
        if (!isDialogActive) viewModel.onStartScanner()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStopScanner()
    }

    private fun initObserver() {

        viewModel.navigateToErrorMessage.observe(viewLifecycleOwner) {
            showErrorOrderDialog(it.type, it.title, it.message, it.button)
        }

        viewModel.navigateToDialogInfo.observe(viewLifecycleOwner) {
            isDialogActive = true
            showDialogInfo(it.type, it.title, it.message, it.button)
        }

        viewModel.toolbarNetworkState.observe(viewLifecycleOwner) {
            val ic = when (it) {
                is NetworkState.Complete -> R.drawable.ic_inet_complete
                else -> R.drawable.ic_inet_failed
            }
            binding.toolbarLayout.noInternetImage.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), ic)
            )
        }

        viewModel.versionApp.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarVersion.text = it
        }

        viewModel.orderTimer.observe(viewLifecycleOwner) {
            when (it) {
                is CourierLoadingScanTimerState.Timer -> {
                    binding.timeDigit.visibility = View.VISIBLE
                    binding.timer.visibility = View.VISIBLE
                    binding.timeDigit.text = it.timeDigit
                    binding.timer.setProgress(it.timeAnalog)
                }
                is CourierLoadingScanTimerState.TimeIsOut -> {
                    showTimeIsOutDialog(it.type, it.title, it.message, it.button)
                }
                CourierLoadingScanTimerState.Stopped -> {
                    binding.timerLayout.visibility = View.GONE
                }
                is CourierLoadingScanTimerState.Info -> binding.gateDigit.text = it.gate
            }
        }

        val navigationObserver = Observer<CourierLoadingScanNavAction> { state ->
            when (state) {
                is CourierLoadingScanNavAction.NavigateToUnknownBox -> {
                    findNavController().navigate(CourierLoadingScanFragmentDirections.actionCourierScannerLoadingScanFragmentToCourierScannerLoadingBoxNotBelongFragment())
                }
                CourierLoadingScanNavAction.NavigateToBoxes -> {
                    findNavController().navigate(CourierLoadingScanFragmentDirections.actionCourierScannerLoadingScanFragmentToCourierLoadingBoxesFragment())
                }
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
            }
        }
        viewModel.navigationEvent.observe(viewLifecycleOwner, navigationObserver)

        viewModel.beepEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierLoadingScanBeepState.BoxFirstAdded -> beepFirstSuccess()
                CourierLoadingScanBeepState.BoxAdded -> beepSuccess()
                CourierLoadingScanBeepState.UnknownBox -> beepUnknownBox()
                CourierLoadingScanBeepState.UnknownQR -> beepUnknownQr()
            }
        }

        viewModel.progressEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierLoadingScanProgress.LoaderProgress -> showProgressDialog()
                CourierLoadingScanProgress.LoaderComplete -> closeProgressDialog()
            }
        }

        viewModel.boxDataStateUI.observe(viewLifecycleOwner) { state ->
            binding.qrCode.text = state.qrCode
            binding.address.text = state.address
            binding.receive.text = state.accepted
        }

        viewModel.isEnableBottomState.observe(viewLifecycleOwner) { state ->
            when (state) {
                true -> binding.complete.setState(ProgressButtonMode.ENABLE)
                false -> binding.complete.setState(ProgressButtonMode.DISABLE)
            }
        }

        viewModel.boxStateUI.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierLoadingScanBoxState.InitScanner -> {
                    binding.timerLayout.visibility = View.VISIBLE
                    binding.scannerInfoLayout.visibility = View.GONE
                    binding.status.setText(R.string.courier_loading_init_scanner)
                    binding.status.setBackgroundColor(getColor(R.color.init_scan_status))
                }
                is CourierLoadingScanBoxState.LoadInCar -> {
                    holdBackButtonOnScanBox()
                    binding.status.setText(R.string.courier_loading_load_in_car)
                    binding.status.setBackgroundColor(getColor(R.color.complete_scan_status))
                    binding.qrCode.setTextColor(getColor(R.color.black_text))
                    binding.timerLayout.visibility = View.GONE
                    binding.scannerInfoLayout.visibility = View.VISIBLE
                }
                is CourierLoadingScanBoxState.ForbiddenTakeWithTimer -> {
                    binding.timerLayout.visibility = View.VISIBLE
                    binding.scannerInfoLayout.visibility = View.GONE
                    binding.status.setText(R.string.courier_loading_forbidden_take)
                    binding.status.setBackgroundColor(getColor(R.color.forbidden_scan_status))
                }
                is CourierLoadingScanBoxState.ForbiddenTakeBox -> {
                    holdBackButtonOnScanBox()
                    binding.timerLayout.visibility = View.GONE
                    binding.scannerInfoLayout.visibility = View.VISIBLE
                    binding.status.setText(R.string.courier_loading_forbidden_take)
                    binding.status.setBackgroundColor(getColor(R.color.forbidden_scan_status))
                    binding.qrCode.setTextColor(getColor(R.color.black_text))
                    binding.address.setTextColor(getColor(R.color.black_text))
                }
                CourierLoadingScanBoxState.NotRecognizedQrWithTimer -> {
                    binding.timerLayout.visibility = View.VISIBLE
                    binding.scannerInfoLayout.visibility = View.GONE
                    binding.status.setText(R.string.courier_loading_not_recognized_qr)
                    binding.status.setBackgroundColor(getColor(R.color.not_recognized_scan_status))
                }
                CourierLoadingScanBoxState.NotRecognizedQr -> {
                    holdBackButtonOnScanBox()
                    binding.timerLayout.visibility = View.GONE
                    binding.scannerInfoLayout.visibility = View.VISIBLE
                    binding.status.setText(R.string.courier_loading_not_recognized_qr)
                    binding.status.setBackgroundColor(getColor(R.color.not_recognized_scan_status))
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

    private fun showErrorOrderDialog(
        type: Int,
        title: String,
        message: String,
        positiveButtonName: String
    ) {
        DialogInfoFragment.newInstance(
            DIALOG_ERROR_INFO_TAG,
            type,
            title,
            message,
            positiveButtonName
        ).show(parentFragmentManager, DIALOG_INFO_TAG)
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
        type: Int,
        title: String,
        message: String,
        positiveButtonName: String
    ) {
        DialogInfoFragment.newInstance(
            type = type,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName
        ).show(parentFragmentManager, DIALOG_INFO_TAG)
    }

    private fun initListener() {
        binding.complete.setOnClickListener { viewModel.onCompleteLoaderClicked() }
        // TODO: 28.09.2021 включить для отладки
        //binding.listLayout.setOnClickListener { viewModel.onListClicked() }
    }

    companion object {
        const val DIALOG_LOADING_CONFIRM_TAG = "DIALOG_LOADING_CONFIRM_TAG"
        const val DIALOG_ERROR_INFO_TAG = "DIALOG_EMPTY_INFO_TAG"
        const val DIALOG_TIME_IS_OUT_INFO_TAG = "DIALOG_TIME_IS_OUT_INFO_TAG"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun beepFirstSuccess() {
        // TODO: 17.11.2021 выключено до замены мелодии
        //play(R.raw.qr_box_first_accepted)
    }

    private fun beepSuccess() {
        // TODO: 11.10.2021 ignore
//        play(R.raw.sound_scan_success)
    }

    private fun beepUnknownBox() {
        play(R.raw.unloading_unknown_box)
    }

    private fun beepUnknownQr() {
        play(R.raw.unloading_scan_unknown_qr)
    }

    private fun play(resId: Int) {
        (activity as OnSoundPlayer).play(resId)
    }

}