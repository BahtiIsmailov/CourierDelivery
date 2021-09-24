package ru.wb.perevozka.ui.courierorderconfirm

import android.app.AlertDialog
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
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.CourierOrderConfirmFragmentBinding
import ru.wb.perevozka.ui.dialogs.DialogInfoFragment
import ru.wb.perevozka.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.perevozka.ui.dialogs.ProgressDialogFragment


class CourierOrderConfirmFragment : Fragment() {

    private val viewModel by viewModel<CourierOrderConfirmViewModel>()

    private lateinit var _binding: CourierOrderConfirmFragmentBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierOrderConfirmFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservable()
        initListeners()
        initReturnResult()
    }

    private fun initReturnResult() {
        setFragmentResultListener(ProgressDialogFragment.PROGRESS_DIALOG_RESULT) { _, bundle ->
            if (bundle.containsKey(ProgressDialogFragment.PROGRESS_DIALOG_BACK_KEY)) {
                viewModel.onCancelLoadClick()
            }
        }
    }

    private fun initObservable() {

        viewModel.orderInfo.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderConfirmInfoUIState.InitOrderInfo -> {
                    binding.order.text = it.order
                    binding.carNumber.text = it.carNumber
                    binding.arrive.text = it.arrive
                    binding.pvz.text = it.pvz
                    binding.volume.text = it.volume
                    binding.coast.text = it.coast
                }
            }
        }

        viewModel.progressState.observe(viewLifecycleOwner) {
            when (it) {
                CourierOrderConfirmProgressState.Progress -> showProgressDialog()
                CourierOrderConfirmProgressState.ProgressComplete -> closeProgressDialog()
            }
        }

        viewModel.navigationState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderConfirmNavigationState.NavigateToRefuseOrderDialog ->
                    showRefuseOrderDialog(it.title, it.message)
                is CourierOrderConfirmNavigationState.NavigateToDialogInfo ->
                    showTimeIsOutDialog(it.title, it.message, it.button)
                CourierOrderConfirmNavigationState.NavigateToWarehouse -> {
                    findNavController().navigate(CourierOrderConfirmFragmentDirections.actionCourierOrderConfirmFragmentToCourierWarehouseFragment())
                }
                CourierOrderConfirmNavigationState.NavigateToTimer -> {
                    findNavController().navigate(CourierOrderConfirmFragmentDirections.actionCourierOrderConfirmFragmentToCourierOrderTimerFragment())
                }
                CourierOrderConfirmNavigationState.NavigateToChangeCar ->
                    findNavController().navigate(CourierOrderConfirmFragmentDirections.actionCourierOrderConfirmFragmentToCourierCarNumberFragment())
            }
        }

    }

    private fun initListeners() {
        binding.refuseOrder.setOnClickListener { viewModel.refuseOrderClick() }
        binding.confirmOrder.setOnClickListener { viewModel.confirmOrderClick() }
        binding.carChangeImage.setOnClickListener { viewModel.onChangeCarClick() }
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