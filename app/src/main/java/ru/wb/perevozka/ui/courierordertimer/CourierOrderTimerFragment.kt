package ru.wb.perevozka.ui.courierordertimer

import android.app.AlertDialog
import android.os.Bundle
import android.os.Parcelable
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.CourierOrderTimerFragmentBinding
import ru.wb.perevozka.db.entity.courier.CourierOrderEntity
import ru.wb.perevozka.ui.dialogs.DialogInfoFragment
import ru.wb.perevozka.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.perevozka.ui.splash.NavDrawerListener
import ru.wb.perevozka.ui.splash.NavToolbarListener


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

        viewModel.navigationState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderTimerNavigationState.NavigateToRefuseOrderDialog ->
                    showRefuseOrderDialog(it.title, it.message)
                is CourierOrderTimerNavigationState.NavigateToDialogInfo ->
                    showTimeIsOutDialog(it.title, it.message, it.button)
                CourierOrderTimerNavigationState.NavigateToWarehouse -> {
                    findNavController().navigate(CourierOrderTimerFragmentDirections.actionCourierOrderTimerFragmentToCourierWarehouseFragment())
                }
                CourierOrderTimerNavigationState.NavigateToScanner -> findNavController().navigate(
                    CourierOrderTimerFragmentDirections.actionCourierOrderTimerFragmentToCourierScannerLoadingScanFragment()
                )
            }
        }

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

    private fun showTimeIsOutDialog(title: String, message: String, button: String) {
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

    private fun showRefuseOrderDialog(title: String, message: String) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val viewGroup: ViewGroup = binding.layout
        val dialogView: View =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.custom_layout_dialog_result, viewGroup, false)

        val titleLayout: View = dialogView.findViewById(R.id.title_layout)
        val titleText: TextView = dialogView.findViewById(R.id.title)
        val messageText: TextView = dialogView.findViewById(R.id.message)
        val negative: Button = dialogView.findViewById(R.id.negative)
        val positive: Button = dialogView.findViewById(R.id.positive)
        builder.setView(dialogView)
        val alertDialog: AlertDialog = builder.create()

        titleLayout.setBackgroundColor(
            ContextCompat.getColor(requireActivity(), R.color.dialog_alarm)
        )
        titleText.text = title
        messageText.text = message
        negative.setOnClickListener { alertDialog.dismiss() }
        negative.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        negative.text = getString(R.string.courier_orders_timer_dialog_negative_button)
        positive.setOnClickListener {
            alertDialog.dismiss()
            viewModel.refuseOrderConfirmClick()
        }
        positive.setTextColor(ContextCompat.getColor(requireContext(), R.color.deny))
        positive.text = getString(R.string.courier_orders_timer_dialog_positive_button)
        alertDialog.show()
    }

    private fun showDialog(style: Int, title: String, message: String, positiveButtonName: String) {
        DialogInfoFragment.newInstance(style, title, message, positiveButtonName)
            .show(parentFragmentManager, DIALOG_INFO_TAG)
    }

}