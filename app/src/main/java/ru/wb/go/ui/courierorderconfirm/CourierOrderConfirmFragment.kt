package ru.wb.go.ui.courierorderconfirm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.CourierOrderConfirmFragmentBinding
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.dialogs.ProgressDialogFragment
import ru.wb.go.ui.splash.NavDrawerListener
import ru.wb.go.utils.LogUtils


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
        initView()
        initObservable()
        initListeners()
        initReturnResult()
    }

    private fun initView() {
        (activity as NavDrawerListener).lock()
        binding.toolbarLayout.toolbarTitle.text = "Подтверждение заказа"
        binding.toolbarLayout.back.visibility = INVISIBLE
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

        viewModel.navigateToDialogInfo.observe(viewLifecycleOwner) {
            showDialogInfo(it.type, it.title, it.message, it.button)
        }

        viewModel.progressState.observe(viewLifecycleOwner) {
            when (it) {
                CourierOrderConfirmProgressState.Progress -> showProgressDialog()
                CourierOrderConfirmProgressState.ProgressComplete -> closeProgressDialog()
            }
        }

        viewModel.navigationState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderConfirmNavigationState.NavigateToRefuseOrderDialog -> {
                }
                CourierOrderConfirmNavigationState.NavigateToBack -> {
                    findNavController().popBackStack()
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
        parentFragmentManager.fragments.forEach { frg -> LogUtils { frg.tag?.let { logDebugApp(it) } } }
        parentFragmentManager.findFragmentByTag(ProgressDialogFragment.PROGRESS_DIALOG_TAG)?.let {
            if (it is ProgressDialogFragment) it.dismiss()
        }
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
        ).show(parentFragmentManager, DialogInfoFragment.DIALOG_INFO_TAG)
    }

}