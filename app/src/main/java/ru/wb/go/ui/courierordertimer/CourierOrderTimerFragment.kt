package ru.wb.go.ui.courierordertimer

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
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment.Companion.DIALOG_CONFIRM_INFO_NEGATIVE_KEY
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment.Companion.DIALOG_CONFIRM_INFO_POSITIVE_KEY
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_BACK_KEY
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.dialogs.ProgressDialogFragment
import ru.wb.go.ui.splash.NavDrawerListener
import ru.wb.go.ui.splash.NavToolbarListener


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

        viewModel.navigateToDialogInfo.observe(viewLifecycleOwner) {
            showDialogInfo(it.type, it.title, it.message, it.button)
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
        type: Int,
        title: String,
        message: String,
        positiveButtonName: String,
    ) {
        DialogInfoFragment.newInstance(
            DIALOG_INFO_TAG,
            type = type,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName,
        ).show(parentFragmentManager, DIALOG_INFO_TAG)
    }

}