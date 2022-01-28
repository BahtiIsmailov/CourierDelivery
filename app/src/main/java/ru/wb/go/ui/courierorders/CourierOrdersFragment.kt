package ru.wb.go.ui.courierorders

import android.content.res.ColorStateList
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.go.R
import ru.wb.go.adapters.DefaultAdapterDelegate
import ru.wb.go.databinding.CourierOrderFragmentBinding
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.couriercarnumber.CourierCarNumberParameters
import ru.wb.go.ui.courierorderdetails.CourierOrderDetailsFragmentDirections
import ru.wb.go.ui.courierorderdetails.CourierOrderDetailsParameters
import ru.wb.go.ui.courierorders.delegates.CourierOrderDelegate
import ru.wb.go.ui.courierorders.delegates.OnCourierOrderCallback
import ru.wb.go.ui.courierwarehouses.CourierWarehousesShowOrdersState
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.ProgressDialogFragment


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
        viewModel.update()
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

    private fun initListeners() {
        binding.backFull.setOnClickListener { findNavController().popBackStack() }
        binding.showOrdersFab.setOnClickListener { viewModel.onDetailClick() }
    }

    private fun initStateObserve() {

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.title.text = it.label
        }

        viewModel.navigateToDialogInfo.observe(viewLifecycleOwner) {
            showDialogInfo(it.type, it.title, it.message, it.button)
        }

        viewModel.navigationState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrdersNavigationState.NavigateToOrderDetails -> {
                    findNavController().navigate(
                        CourierOrderFragmentDirections.actionCourierOrderFragmentToCourierOrderDetailsFragment(
                            CourierOrderDetailsParameters(
                                it.title,
                                it.orderNumber,
                                it.order,
                                it.warehouseLatitude,
                                it.warehouseLongitude
                            )
                        )
                    )
                }
                is CourierOrdersNavigationState.NavigateToCarNumber ->
                    findNavController().navigate(
                        CourierOrderFragmentDirections.actionCourierOrderFragmentToCourierCarNumberFragment(
                            CourierCarNumberParameters(it.title, it.orderNumber, it.order)
                        )
                    )
            }
        }

        viewModel.orders.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierOrderItemState.ShowOrders -> {
                    binding.emptyList.visibility = GONE
                    binding.orderProgress.visibility = GONE
                    binding.orders.visibility = VISIBLE
                    displayItems(state.items)
                }
                is CourierOrderItemState.Empty -> {
                    binding.emptyList.visibility = VISIBLE
                    binding.orderProgress.visibility = GONE
                    binding.orders.visibility = GONE
                    binding.emptyTitle.text = state.info
                }
                is CourierOrderItemState.ScrollTo -> {
                    smoothScrollToPosition(state.position)
                }
                is CourierOrderItemState.UpdateItem -> {
                    adapter.setItem(state.position, state.item)
                    adapter.notifyItemChanged(state.position, state.item)
                }
                is CourierOrderItemState.UpdateItems -> {
                    adapter.clear()
                    adapter.addItems(state.items)
                    adapter.notifyDataSetChanged()
                }
            }
        }

        viewModel.progressState.observe(viewLifecycleOwner) { state ->
            binding.orderProgress.visibility = when (state) {
                CourierOrdersProgressState.Progress -> VISIBLE
                CourierOrdersProgressState.Complete -> GONE
            }
        }

        viewModel.holdState.observe(viewLifecycleOwner) {
            when (it) {
                true -> binding.holdLayout.visibility = VISIBLE
                false -> binding.holdLayout.visibility = GONE
            }
        }

        viewModel.showDetailsState.observe(viewLifecycleOwner) {
            when (it) {
                CourierOrderShowDetailsState.Disable -> showOrdersDisable()
                CourierOrderShowDetailsState.Enable -> {
                    binding.showOrdersFab.isEnabled = true
                    binding.showOrdersFab.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.fab_enable
                        )
                    )
                }
                CourierOrderShowDetailsState.Progress -> {}
            }
        }

    }

    private fun smoothScrollToPosition(position: Int) {
        val smoothScroller: SmoothScroller = createSmoothScroller()
        smoothScroller.targetPosition = position
        layoutManager.startSmoothScroll(smoothScroller)
    }

    private fun createSmoothScroller(): SmoothScroller {
        return object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
    }

    private fun showOrdersDisable() {
        binding.showOrdersFab.isEnabled = false
        binding.showOrdersFab.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireContext(),
                R.color.fab_disable
            )
        )
    }

    private fun showDialogInfo(
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
        ).show(parentFragmentManager, DialogInfoFragment.DIALOG_INFO_TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("NotifyDataSetChanged")
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

}

@Parcelize
data class CourierOrderParameters(
    val warehouseId: Int,
    val warehouseLatitude: Double,
    val warehouseLongitude: Double,
    val address: String
) : Parcelable