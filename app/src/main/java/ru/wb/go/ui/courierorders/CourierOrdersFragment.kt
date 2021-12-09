package ru.wb.go.ui.courierorders

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.*
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.go.adapters.DefaultAdapterDelegate
import ru.wb.go.databinding.CourierOrderFragmentBinding
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.courierorderdetails.CourierOrderDetailsParameters
import ru.wb.go.ui.courierorders.delegates.CourierOrderDelegate
import ru.wb.go.ui.courierorders.delegates.OnCourierOrderCallback
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.ProgressDialogFragment
import ru.wb.go.ui.splash.NavDrawerListener
import ru.wb.go.ui.splash.NavToolbarListener
import ru.wb.go.views.ProgressButtonMode


class CourierOrderFragment : Fragment() {

    companion object {
        const val COURIER_ORDER_ID_KEY = "courier_order_id_key"
    }

    private val viewModel by viewModel<CourierOrdersViewModel> {
        parametersOf(
            requireArguments().getParcelable<CourierOrderParameters>(
                COURIER_ORDER_ID_KEY
            )
        )
    }

    private var _binding: CourierOrderFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DefaultAdapterDelegate
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: SmoothScroller

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierOrderFragmentBinding.inflate(inflater, container, false)
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
    }

    private fun initStateObserve() {

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarTitle.text = it.label
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
                CourierOrdersNavigationState.NavigateToBack -> { }
                is CourierOrdersNavigationState.NavigateToDialogInfo -> with(it) {
                    showDialogInfo(type, title, message, button)
                }
                is CourierOrdersNavigationState.NavigateToOrderDetails -> {
                    findNavController().navigate(
                        CourierOrderFragmentDirections.actionCourierOrderFragmentToCourierOrderDetailsFragment(
                            CourierOrderDetailsParameters(it.title, it.order)
                        )
                    )
                }
            }
        }

        viewModel.orders.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierOrdersState.ShowOrders -> {
                    binding.emptyList.visibility = GONE
                    binding.progress.visibility = GONE
                    binding.orders.visibility = VISIBLE
                    binding.update.setState(ProgressButtonMode.ENABLE)
                    displayItems(state.items)
                }
                is CourierOrdersState.Empty -> {
                    binding.emptyList.visibility = VISIBLE
                    binding.progress.visibility = GONE
                    binding.orders.visibility = GONE
                    binding.emptyTitle.text = state.info
                    binding.update.setState(ProgressButtonMode.ENABLE)
                }
            }
        }

        viewModel.progressState.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierOrdersProgressState.Progress -> showProgressDialog()
                CourierOrdersProgressState.Complete -> closeProgressDialog()
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
        binding.orders.layoutManager = layoutManager
        binding.orders.addItemDecoration(
            DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        )
        binding.orders.setHasFixedSize(true)
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
            addDelegate(
                CourierOrderDelegate(requireContext(),
                    object : OnCourierOrderCallback {
                        override fun onOrderClick(idView: Int) {
                            viewModel.onItemClick(idView)
                        }
                    })
            )
        }
        binding.orders.adapter = adapter
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

@Parcelize
data class CourierOrderParameters(val currentWarehouseId: Int, val address: String) : Parcelable