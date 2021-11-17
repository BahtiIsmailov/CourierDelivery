package ru.wb.go.ui.courierloading

import android.media.MediaPlayer
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
import ru.wb.go.ui.splash.NavDrawerListener
import ru.wb.go.ui.splash.NavToolbarListener
import ru.wb.go.ui.splash.OnCourierScanner
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
        setFragmentResultListener(DIALOG_EMPTY_INFO_TAG) { _, bundle ->
            if (bundle.containsKey(DIALOG_INFO_BACK_KEY)) {
//                viewModel.onDialogInfoConfirmClick()
                findNavController().popBackStack()
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
        (activity as NavDrawerListener).lock()
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

        viewModel.navigateToEmptyDialog.observe(viewLifecycleOwner) {
            isDialogActive = true
            showEmptyOrderDialog(it.type, it.title, it.message, it.button)
        }

        viewModel.navigateToErrorMessage.observe(viewLifecycleOwner) {
            showEmptyOrderDialog(it.type, it.title, it.message, it.button)
        }

        viewModel.toolbarNetworkState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Failed ->
                    binding.toolbarLayout.noInternetImage.visibility = View.VISIBLE
                is NetworkState.Complete ->
                    binding.toolbarLayout.noInternetImage.visibility = View.INVISIBLE
            }
        }

        viewModel.progressEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierLoadingScanProgress.LoaderProgress -> showProgressDialog()
                CourierLoadingScanProgress.LoaderComplete -> closeProgressDialog()
            }
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
                is CourierLoadingScanNavAction.NavigateToIntransit ->
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
                CourierLoadingScanBeepState.UnknownBox -> beepUnknown()
            }
        }

        viewModel.progressEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierLoadingScanProgress.LoaderProgress -> {
                    binding.listLayout.isEnabled = false
                    binding.complete.setState(ProgressButtonMode.DISABLE)
                }
                is CourierLoadingScanProgress.LoaderComplete -> {
                    binding.listLayout.isEnabled = true
                    binding.complete.setState(ProgressButtonMode.ENABLE)
                }
            }
        }

        viewModel.boxStateUI.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierLoadingScanBoxState.Empty -> {
                    binding.toolbarLayout.back.visibility = View.VISIBLE
                    binding.status.text = "НАЧНИТЕ СКАНИРОВАНИЕ"
                    binding.status.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.disable_scan_status
                        )
                    )
                    binding.timerLayout.visibility = View.VISIBLE
                    binding.scannerInfoLayout.visibility = View.GONE
                }
                is CourierLoadingScanBoxState.BoxInit -> {
                    holdBackButtonOnScanBox()
                    binding.status.text = "ПОГРУЗИТЕ В МАШИНУ"
                    binding.status.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.complete_scan_status
                        )
                    )
                    binding.qrCode.text = state.qrCode
                    binding.qrCode.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black_text
                        )
                    )
                    binding.address.text = state.address
                    binding.receive.text = state.accepted

                    binding.timerLayout.visibility = View.GONE
                    binding.scannerInfoLayout.visibility = View.VISIBLE

                    binding.complete.setState(ProgressButtonMode.ENABLE)

                }
                is CourierLoadingScanBoxState.BoxAdded -> {
                    holdBackButtonOnScanBox()

                    binding.timerLayout.visibility = View.GONE
                    binding.scannerInfoLayout.visibility = View.VISIBLE

                    binding.status.text = "ПОГРУЗИТЕ В МАШИНУ"
                    binding.status.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.complete_scan_status
                        )
                    )
                    binding.qrCode.text = state.qrCode
                    binding.qrCode.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black_text
                        )
                    )
                    binding.address.text = state.address
                    binding.address.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black_text
                        )
                    )
                    binding.receive.text = state.accepted
                    binding.complete.setState(ProgressButtonMode.ENABLE)
                }
                is CourierLoadingScanBoxState.UnknownBox -> {

                    binding.timerLayout.visibility = View.GONE
                    binding.scannerInfoLayout.visibility = View.VISIBLE

                    holdBackButtonOnScanBox()
                    binding.status.text = "КОРОБКУ БРАТЬ ЗАПРЕЩЕНО"
                    binding.status.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.unknown_scan_status
                        )
                    )
                    binding.qrCode.text = state.qrCode
                    binding.qrCode.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black_text
                        )
                    )
                    binding.address.text = state.address
                    binding.address.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black_text
                        )
                    )
                    binding.receive.text = state.accepted
                    binding.complete.setState(ProgressButtonMode.ENABLE)
                }
                CourierLoadingScanBoxState.UnknownBoxTimer -> {
                    binding.timerLayout.visibility = View.VISIBLE
                    binding.scannerInfoLayout.visibility = View.GONE
                    binding.status.text = "КОРОБКУ БРАТЬ ЗАПРЕЩЕНО"
                    binding.status.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.unknown_scan_status
                        )
                    )
                }
            }
        }
    }

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
            type = type,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName
        ).show(parentFragmentManager, DIALOG_INFO_TAG)
    }

    private fun showEmptyOrderDialog(
        type: Int,
        title: String,
        message: String,
        positiveButtonName: String
    ) {
        DialogInfoFragment.newInstance(
            DIALOG_EMPTY_INFO_TAG,
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
            type= type,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName,
            negativeButtonName = negativeButtonName
        ).show(parentFragmentManager, DIALOG_CONFIRM_INFO_TAG)
    }

    private fun initListener() {
        binding.complete.setOnClickListener { viewModel.onCompleteLoaderClicked() }
        // TODO: 28.09.2021 включить для отладки
        //binding.listLayout.setOnClickListener { viewModel.onListClicked() }
    }

    companion object {
        const val DIALOG_LOADING_CONFIRM_TAG = "DIALOG_LOADING_CONFIRM_TAG"
        const val DIALOG_EMPTY_INFO_TAG = "DIALOG_EMPTY_INFO_TAG"
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

    private fun beepUnknown() {
        play(R.raw.unloading_scan_unknown_qr)
    }

    private fun play(resId: Int) {
        MediaPlayer.create(context, resId).start()
    }

}