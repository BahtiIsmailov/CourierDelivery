package ru.wb.go.ui.courierbilling

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.adapters.DefaultAdapterDelegate
import ru.wb.go.databinding.CourierBillingFragmentBinding
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.BaseServiceFragment
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.courierbilling.delegates.CourierBillingNegativeDelegate
import ru.wb.go.ui.courierbilling.delegates.CourierBillingPositiveDelegate
import ru.wb.go.ui.courierbillingaccountselector.CourierBillingAccountSelectorAmountParameters
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.ProgressDialogFragment
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.managers.ErrorDialogData


class CourierBillingFragment :
    BaseServiceFragment<CourierBillingViewModel, CourierBillingFragmentBinding>(
        CourierBillingFragmentBinding::inflate
    ) {

    private lateinit var adapter: DefaultAdapterDelegate
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: SmoothScroller

    override val viewModel by viewModel<CourierBillingViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecyclerView()
        initAdapter()
        initListeners()
        initStateObserve()
        initReturnResult()
        viewModel.init()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lockNavDrawer()
    }

    private fun initReturnResult() {
        setFragmentResultListener(ProgressDialogFragment.PROGRESS_DIALOG_RESULT) { _, bundle ->
            if (bundle.containsKey(ProgressDialogFragment.PROGRESS_DIALOG_BACK_KEY)) {
                viewModel.onCancelLoadClick()
            }
        }
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

    private fun initListeners() {
        binding.toolbarLayout.back.setOnClickListener { findNavController().popBackStack() }
        binding.update.setOnClickListener { viewModel.onUpdateClick() }
        binding.toAccount.setOnClickListener { viewModel.gotoBillingAccountsClick() }
    }

    private fun initStateObserve() {

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarTitle.text = it
        }

        viewModel.balanceInfo.observe(viewLifecycleOwner) {
            with(binding) {
                coast.text = it
                if (viewModel.canCheckout()) {
                    toAccount.visibility = VISIBLE
                }else {
                    toAccount.visibility = GONE
                }
            }
        }

        viewModel.navigateToDialogInfo.observe(viewLifecycleOwner) {
            showDialogInfo(it)
        }

        viewModel.navigationState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierBillingNavigationState.NavigateToAccountSelector -> findNavController().navigate(
                    CourierBillingFragmentDirections.actionCourierBalanceFragmentToCourierBillingAccountSelectorFragment(
                        CourierBillingAccountSelectorAmountParameters(it.balance)
                    )
                )
            }
        }

        viewModel.billingItems.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierBillingState.Init -> {
                    binding.emptyList.visibility = GONE
                }
                is CourierBillingState.ShowBilling -> {
                    binding.emptyList.visibility = GONE
                    binding.operations.visibility = VISIBLE
                    displayItems(state.items)
                }
                is CourierBillingState.Empty -> {
                    binding.emptyList.visibility = VISIBLE
                    binding.operations.visibility = GONE
                    binding.emptyTitle.text = state.info
                }
            }
        }

        viewModel.waitLoader.observe(viewLifecycleOwner) { state ->
            when (state) {
                WaitLoader.Wait -> showProgressDialog()
                WaitLoader.Complete -> closeProgressDialog()
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun displayItems(items: List<BaseItem>) {
        with(adapter) {
            clear()
            addItems(items)
            notifyDataSetChanged()
        }
    }

    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.operations.layoutManager = layoutManager
        binding.operations.setHasFixedSize(true)
        initSmoothScroller()
    }

    private fun initSmoothScroller() {
        smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
    }

    private fun initAdapter() {

        adapter = with(DefaultAdapterDelegate()) {
            addDelegate(CourierBillingPositiveDelegate(requireContext()))
            addDelegate(CourierBillingNegativeDelegate(requireContext()))
        }
        binding.operations.adapter = adapter
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
        ).show(parentFragmentManager, DialogInfoFragment.DIALOG_INFO_TAG)
    }

}