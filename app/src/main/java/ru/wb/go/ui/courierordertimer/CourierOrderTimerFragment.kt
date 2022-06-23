package ru.wb.go.ui.courierordertimer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.CourierOrderTimerFragmentBinding
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment.Companion.DIALOG_CONFIRM_INFO_POSITIVE_KEY
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_BACK_KEY
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.dialogs.ProgressDialogFragment
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.managers.ErrorDialogData


class CourierOrderTimerFragment : Fragment() {

    private val viewModel by viewModel<CourierOrderTimerViewModel>()

    private lateinit var _binding: CourierOrderTimerFragmentBinding
    private val binding get() = _binding

    companion object {
        const val DIALOG_REFUSE_INFO_TAG = "DIALOG_REFUSE_INFO_TAG"
        const val DIALOG_TIME_OUT_INFO_TAG = "DIALOG_TIME_OUT_INFO_TAG"
    }

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
        setFragmentResultListener(DIALOG_TIME_OUT_INFO_TAG) { _, bundle ->
            if (bundle.containsKey(DIALOG_INFO_BACK_KEY)) {
                viewModel.timeOutReturnToList()
            }
        }

        setFragmentResultListener(DIALOG_REFUSE_INFO_TAG) { _, bundle ->
            if (bundle.containsKey(DIALOG_CONFIRM_INFO_POSITIVE_KEY)) {
                viewModel.onRefuseOrderConfirmClick()
            }
        }

        setFragmentResultListener(DIALOG_INFO_TAG) { _, _ ->
            if (viewModel.timeOut.value == true) {
                viewModel.timeOutReturnToList()
            }
        }
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lockNavDrawer()
        viewModel.getOrderId()
    }

    private fun initObservable() {
        viewModel.getOrderId.observe(viewLifecycleOwner) {
            var textForRouteNumber = requireContext().getText(R.string.route).toString()
            textForRouteNumber += " ${it.route}"
            binding.routeTV.text = textForRouteNumber
            setFragmentResult(
                DialogInfoFragment.ROUTE_ID,
                bundleOf("bundleKey" to textForRouteNumber)
            )
        }
        viewModel.orderInfo.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderTimerInfoUIState.InitOrderInfo -> {
                    binding.order.text = it.order
                    binding.name.text = it.name
                    binding.coast.text = it.coast
                    binding.volume.text = it.countBoxAndVolume
                    binding.pvz.text = it.countPvz
                    binding.gate.text =
                        if (it.gate == "0") {
                            binding.gateDigit.isGone = true
                            requireContext().getString(R.string.courier_loading_pandus)
                        } else {
                            binding.gateDigit.isVisible = true
                            binding.gateDigit.text = it.gate
                            requireContext().getString(
                                R.string.courier_loading_gate
                            )
                        }
                }
            }
        }

        viewModel.orderTimer.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderTimerState.Timer -> {
                    binding.timer.setProgress(it.timeAnalog)
                    binding.timeDigit.text = it.timeDigit
                }
                is CourierOrderTimerState.TimerIsOut -> {
                    val isOutColor = ContextCompat.getColor(requireContext(), R.color.red)
                    binding.timer.setProgress(it.timeAnalog)
                    binding.timer.setScaleWaitColorColor(isOutColor)
                    binding.timeDigit.text = it.timeDigit
                    binding.timeDigit.setTextColor(isOutColor)
                }
            }
        }

        viewModel.waitLoader.observe(viewLifecycleOwner) {
            when (it) {
                WaitLoader.Wait -> showProgressDialog()
                WaitLoader.Complete -> closeProgressDialog()
            }
        }

        viewModel.navigateToDialogTimeIsOut.observe(viewLifecycleOwner) {
            showDialogTimeIsOut(it.type, it.title, it.message, it.button)
        }

        viewModel.navigateToDialogRefuseOrder.observe(viewLifecycleOwner) {
            showRefuseOrderDialog(it.type, it.title, it.message, it.positive, it.negative)
        }

        viewModel.navigateToDialogInfo.observe(viewLifecycleOwner) {
            showDialogInfo(it)
        }
        viewModel.navigationState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderTimerNavigationState.NavigateToWarehouse -> {
                    findNavController().navigate(CourierOrderTimerFragmentDirections.actionCourierOrderTimerFragmentToCourierWarehouseFragment())
                }
                is CourierOrderTimerNavigationState.NavigateToScanner -> findNavController().navigate(
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
            DIALOG_TIME_OUT_INFO_TAG,
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
            DIALOG_REFUSE_INFO_TAG,
            type = style,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName,
            negativeButtonName = negativeButtonName
        ).show(parentFragmentManager, DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_TAG)
    }

    private fun initListeners() {
        binding.refuseOrder.setOnClickListener { viewModel.onRefuseOrderClick() }
        binding.iArrived.setOnClickListener { viewModel.iArrivedClick() }
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

}