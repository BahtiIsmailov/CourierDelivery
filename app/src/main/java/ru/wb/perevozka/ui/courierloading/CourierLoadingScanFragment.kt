package ru.wb.perevozka.ui.courierloading

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.CourierLoadingFragmentBinding
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.courierstartdelivery.CourierStartDeliveryParameters
import ru.wb.perevozka.ui.dialogs.DialogInfoFragment
import ru.wb.perevozka.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_BACK_KEY
import ru.wb.perevozka.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_RESULT
import ru.wb.perevozka.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.perevozka.ui.dialogs.ProgressDialogFragment
import ru.wb.perevozka.ui.splash.NavDrawerListener
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.ui.splash.OnCourierScanner
import ru.wb.perevozka.views.ProgressButtonMode

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
        setFragmentResultListener(DIALOG_INFO_RESULT) { _, bundle ->
            if (bundle.containsKey(DIALOG_INFO_BACK_KEY)) {
                viewModel.onDialogInfoConfirmClick()
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

        viewModel.navigateToInformation.observe(viewLifecycleOwner) {
            isDialogActive = true
            showEmptyOrderDialog(it.title, it.message, it.button)
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
                    showTimeIsOutDialog(it.title, it.message, it.button)
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
                    showConfirmDialog(
                        "Завершить погрузку?",
                        "Вы уверены, что хотите начать развозить коробки"
                    )
                }
                CourierLoadingScanNavAction.NavigateToBack -> {
                }
                CourierLoadingScanNavAction.NavigateToWarehouse ->
                    findNavController().navigate(CourierLoadingScanFragmentDirections.actionCourierScannerLoadingScanFragmentToCourierWarehouseFragment())
                is CourierLoadingScanNavAction.NavigateToIntransit ->
                    findNavController().navigate(
                        CourierLoadingScanFragmentDirections.actionCourierScannerLoadingScanFragmentToCourierStartDeliveryFragment(
                            CourierStartDeliveryParameters(state.amount, state.count)
                        )
                    )
                is CourierLoadingScanNavAction.NavigateToDialogInfo -> {
                    showDialogInfo(state.type, state.title, state.message, state.button)
                }
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

//        viewModel.bottomProgressEvent.observe(viewLifecycleOwner) { progress ->
//            when (progress) {
//                CourierLoadingScanBottomState.Disable -> binding.complete.setState(
//                    ProgressButtonMode.DISABLE
//                )
//                CourierLoadingScanBottomState.Enable -> binding.complete.setState(
//                    ProgressButtonMode.ENABLE
//                )
//                CourierLoadingScanBottomState.Progress -> binding.complete.setState(
//                    ProgressButtonMode.PROGRESS
//                )
//            }
//        }

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

//                    binding.qrCode.text = "0000000000"
//                    binding.qrCode.setTextColor(
//                        ContextCompat.getColor(
//                            requireContext(),
//                            R.color.light_text
//                        )
//                    )
//                    binding.address.text = "-"
//                    binding.receive.text = "0 шт."
//                    binding.listLayout.setOnClickListener { null }
//                    binding.complete.setState(ProgressButtonMode.DISABLE)
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

    private fun showDialogInfo(
        style: Int,
        title: String,
        message: String,
        positiveButtonName: String
    ) {
        DialogInfoFragment.newInstance(style, title, message, positiveButtonName)
            .show(parentFragmentManager, DIALOG_INFO_TAG)
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

//    private fun showSimpleDialog(it: CourierLoadingScanViewModel.NavigateToMessageInfo) {
//        val builder: AlertDialog.Builder =
//            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
//        val viewGroup: ViewGroup = binding.main
//        val dialogView: View =
//            LayoutInflater.from(requireContext())
//                .inflate(R.layout.custom_layout_dialog_, viewGroup, false)
//        val title: TextView = dialogView.findViewById(R.id.title)
//        val message: TextView = dialogView.findViewById(R.id.message)
//        val negative: Button = dialogView.findViewById(R.id.negative)
//        builder.setView(dialogView)
//
//        val alertDialog: AlertDialog = builder.create()
//
//        title.text = it.title
//        message.text = it.message
//        negative.setOnClickListener {
//            isDialogActive = false
//            alertDialog.dismiss()
//        }
//        negative.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
//        negative.text = it.button
//        alertDialog.setOnDismissListener {
//            isDialogActive = false
//            viewModel.onStartScanner()
//        }
//        alertDialog.show()
//    }

    private fun showEmptyOrderDialog(title: String, message: String, button: String) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val viewGroup: ViewGroup = binding.main
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

    private fun showTimeIsOutDialog(title: String, message: String, button: String) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val viewGroup: ViewGroup = binding.main
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
            viewModel.returnToListOrderClick()
        }

        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                alertDialog.dismiss()
                viewModel.returnToListOrderClick()
            }
            true
        }

        positive.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        positive.text = button
        alertDialog.show()
    }

    // TODO: 27.08.2021 переработать
    private fun showConfirmDialog(title: String, message: String) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val viewGroup: ViewGroup = binding.main
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
        negative.setOnClickListener {
            alertDialog.dismiss()
            viewModel.onCancelLoadingClick()
        }
        negative.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        negative.text = getString(R.string.courier_order_scanner_dialog_negative_button)
        positive.setOnClickListener {
            alertDialog.dismiss()
            viewModel.confirmLoadingClick()
        }
        positive.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        positive.text = getString(R.string.courier_order_scanner_dialog_positive_button)
        alertDialog.show()
    }

    private fun initListener() {
        binding.complete.setOnClickListener { viewModel.onCompleteLoaderClicked() }
        // TODO: 28.09.2021 включить для отладки
        //binding.listLayout.setOnClickListener { viewModel.onListClicked() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun beepFirstSuccess() {
        play(R.raw.qr_box_first_accepted)
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