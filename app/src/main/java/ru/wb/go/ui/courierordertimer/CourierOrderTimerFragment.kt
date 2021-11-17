package ru.wb.go.ui.courierordertimer

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.databinding.CourierOrderTimerFragmentBinding
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment.Companion.DIALOG_CONFIRM_INFO_POSITIVE_KEY
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment.Companion.DIALOG_CONFIRM_INFO_RESULT_TAG
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_BACK_KEY
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_RESULT_TAG
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.splash.NavDrawerListener
import ru.wb.go.ui.splash.NavToolbarListener


class CourierOrderTimerFragment : Fragment() {

    private val viewModel by viewModel<CourierOrderTimerViewModel>()

    private lateinit var _binding: CourierOrderTimerFragmentBinding
    private val binding get() = _binding
    private lateinit var progressDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierOrderTimerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObservable()
        initListeners()
        initReturnDialogResult()
    }

    private fun initReturnDialogResult() {
        setFragmentResultListener(DIALOG_INFO_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DIALOG_INFO_BACK_KEY)) {
                viewModel.returnToListOrderClick()
            }
        }

        setFragmentResultListener(DIALOG_CONFIRM_INFO_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DIALOG_CONFIRM_INFO_POSITIVE_KEY)) {
                viewModel.refuseOrderConfirmClick()
            }
        }
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lock()
    }

    private fun initObservable() {

        viewModel.orderInfo.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderTimerInfoUIState.InitOrderInfo -> {
                    binding.order.text = it.order
                    binding.name.text = it.name
                    binding.coast.text = it.coast
                    binding.volume.text = it.countBoxAndVolume
                    binding.pvz.text = it.countPvz
                    binding.gateDigit.text = it.gate
                }
            }
        }

        viewModel.orderTimer.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderTimerState.Timer -> {
                    binding.timer.setProgress(it.timeAnalog)
                    binding.timeDigit.text = it.timeDigit
                }
            }
        }

        viewModel.progressState.observe(viewLifecycleOwner) {
            when (it) {
                CourierOrderTimerProgressState.Progress -> showProgressDialog()
                CourierOrderTimerProgressState.ProgressComplete -> closeProgressDialog()
            }
        }

        viewModel.navigateToDialogTimeIsOut.observe(viewLifecycleOwner) {
            showDialogTimeIsOut(it.type, it.title, it.message, it.button)
        }

        viewModel.navigateToDialogRefuseOrder.observe(viewLifecycleOwner) {
            showRefuseOrderDialog(it.type, it.title, it.message, it.positive, it.negative)
        }

        viewModel.navigationState.observe(viewLifecycleOwner) {
            when (it) {
                CourierOrderTimerNavigationState.NavigateToWarehouse -> {
                    findNavController().navigate(CourierOrderTimerFragmentDirections.actionCourierOrderTimerFragmentToCourierWarehouseFragment())
                }
                CourierOrderTimerNavigationState.NavigateToScanner -> findNavController().navigate(
                    CourierOrderTimerFragmentDirections.actionCourierOrderTimerFragmentToCourierScannerLoadingScanFragment()
                )
            }
        }

    }

    private fun showDialogTimeIsOut(
        style: Int,
        title: String,
        message: String,
        positiveButtonName: String
    ) {
        DialogInfoFragment.newInstance(
            type = style,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName
        ).show(parentFragmentManager, DIALOG_INFO_TAG)
    }

    private fun showRefuseOrderDialog(
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

    private fun initListeners() {
        binding.refuseOrder.setOnClickListener { viewModel.refuseOrderClick() }
        binding.iArrived.setOnClickListener { viewModel.iArrivedClick() }
    }

    private fun closeProgressDialog() {
        if (progressDialog.isShowing) progressDialog.dismiss()
    }

    private fun showProgressDialog() {
        progressDialog.show()
    }

}