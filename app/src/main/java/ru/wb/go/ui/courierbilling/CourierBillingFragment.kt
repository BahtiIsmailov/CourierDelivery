package ru.wb.go.ui.courierbilling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.adapters.DefaultAdapterDelegate
import ru.wb.go.databinding.CourierBillingFragmentBinding
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.courierbilling.delegates.CourierBillingNegativeDelegate
import ru.wb.go.ui.courierbilling.delegates.CourierBillingPositiveDelegate
import ru.wb.go.ui.courierbilling.delegates.OnCourierBillingCallback
import ru.wb.go.ui.courierbillingaccountdata.CourierBillingAccountDataAmountParameters
import ru.wb.go.ui.courierbillingaccountselector.CourierBillingAccountSelectorAmountParameters
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.ProgressDialogFragment
import ru.wb.go.ui.splash.NavDrawerListener
import ru.wb.go.ui.splash.NavToolbarListener


class CourierBillingFragment : Fragment() {

    private val viewModel by viewModel<CourierBillingViewModel>()

    private var _binding: CourierBillingFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DefaultAdapterDelegate
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: SmoothScroller

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierBillingFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecyclerView()
        initAdapter()
        initListeners()
        initStateObserve()
        initReturnResult()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lock()
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
        binding.account.setOnClickListener { viewModel.onAccountClick() }
    }

    private fun initStateObserve() {

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarTitle.text = it
        }

        viewModel.balanceInfo.observe(viewLifecycleOwner) {
            binding.coast.text = it
        }

        viewModel.toolbarNetworkState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Failed -> {
                    binding.toolbarLayout.noInternetImage.visibility = View.VISIBLE
                }

                is NetworkState.Complete -> {
                    binding.toolbarLayout.noInternetImage.visibility = View.INVISIBLE
                }
            }
        }

        viewModel.navigationState.observe(viewLifecycleOwner) {
            when (it) {
                CourierBillingNavigationState.NavigateToBack -> {
                }
                is CourierBillingNavigationState.NavigateToDialogInfo -> with(it) {
                    showDialogInfo(type, title, message, button)
                }
                is CourierBillingNavigationState.NavigateToAccountCreate -> findNavController().navigate(
                    CourierBillingFragmentDirections.actionCourierBalanceFragmentToCourierBillingAccountDataFragment(
                        CourierBillingAccountDataAmountParameters(it.account, it.balance)
                    )
                )
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
                    binding.progress.visibility = GONE
                    binding.progress.visibility = VISIBLE
                }
                is CourierBillingState.ShowBilling -> {
                    binding.emptyList.visibility = GONE
                    binding.progress.visibility = GONE
                    binding.operations.visibility = VISIBLE
                    displayItems(state.items)
                }
                is CourierBillingState.Empty -> {
                    binding.emptyList.visibility = VISIBLE
                    binding.progress.visibility = GONE
                    binding.operations.visibility = GONE
                    binding.emptyTitle.text = state.info
                }
            }
        }

        viewModel.progressState.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierBillingProgressState.Progress -> showProgressDialog()
                CourierBillingProgressState.Complete -> closeProgressDialog()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun displayItems(items: List<BaseItem>) {
        adapter.clear()
        adapter.addItems(items)
        adapter.notifyDataSetChanged()
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

        val callback = object : OnCourierBillingCallback {
            override fun onOrderClick(idView: Int) {
                viewModel.onItemClick(idView)
            }
        }

        adapter = with(DefaultAdapterDelegate()) {
            addDelegate(CourierBillingPositiveDelegate(requireContext(), callback))
            addDelegate(CourierBillingNegativeDelegate(requireContext(), callback))
        }
        binding.operations.adapter = adapter
    }

    private fun showDialogInfo(
        style: Int,
        title: String,
        message: String,
        positiveButtonName: String
    ) {
        DialogInfoFragment.newInstance(style, title, message, positiveButtonName)
            .show(parentFragmentManager, DialogInfoFragment.DIALOG_INFO_TAG)
    }

}