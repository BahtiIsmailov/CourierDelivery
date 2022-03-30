package ru.wb.go.ui.courierorders

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.go.R
import ru.wb.go.adapters.DefaultAdapterDelegate
import ru.wb.go.databinding.CourierOrdersFragmentBinding
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.couriercarnumber.CourierCarNumberParameters
import ru.wb.go.ui.courierorderdetails.CourierOrderDetailsInfoUIState
import ru.wb.go.ui.courierorders.delegates.CourierOrderDelegate
import ru.wb.go.ui.courierorders.delegates.OnCourierOrderCallback
import ru.wb.go.ui.courierwarehouses.gethorizontalDividerDecoration
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.managers.ErrorDialogData


class CourierOrderFragment : Fragment() {

    companion object {
        const val COURIER_ORDER_ID_KEY = "courier_order_id_key"
    }

    private val viewModel by viewModel<CourierOrdersViewModel> {
        parametersOf(requireArguments().getParcelable<CourierOrderParameters>(COURIER_ORDER_ID_KEY))
    }

    private var _binding: CourierOrdersFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DefaultAdapterDelegate
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: SmoothScroller

    private lateinit var addressAdapter: CourierOrderDetailsAddressAdapter
    private lateinit var addressLayoutManager: LinearLayoutManager
    private lateinit var addressSmoothScroller: SmoothScroller

    private lateinit var bottomSheetOrders: BottomSheetBehavior<FrameLayout>
    private lateinit var bottomSheetOrderDetails: BottomSheetBehavior<FrameLayout>
    private lateinit var bottomSheetOrderAddresses: BottomSheetBehavior<FrameLayout>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierOrdersFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecyclerViewOrders()
        initRecyclerViewAddress()
        initAdapter()
        initListeners()
        initStateObserve()

//        val display: Display = getWindowManager().getDefaultDisplay()
//        binding.ordersLayout

//        val wm = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        val display = wm.defaultDisplay


        binding.ordersLayout.apply {
            viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)

                        //behavior.peekHeight = findViewById<View>(targetViewId).bottom

                    }
                })
        }

//        app:behavior_fitToContents="false"
//        app:behavior_halfExpandedRatio="0.5"

       val halfHeightDisplay = getHalfHeightDisplay()
//        bottomSheetOrders.peekHeight = halfHeightDisplay
//        bottomSheetOrders.isGestureInsetBottomIgnored = true
//        bottomSheetOrders.isFitToContents = false

        viewModel.update(halfHeightDisplay)


//        binding.ordersLayout.viewTreeObserver.addOnGlobalLayoutListener(
//            object :
//                ViewTreeObserver.OnGlobalLayoutListener {
//                override fun onGlobalLayout() {
//                    binding.ordersLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                    viewModel.update(getHalfHeightDisplay()) //binding.ordersLayout.height
//                    //viewModel.onHeightInfoBottom(binding.orderDetails.height)
//                }
//            })


        //viewModel.update()
    }

    private fun getHalfHeightDisplay(): Int {
        val outMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display = requireContext().display
            display?.getRealMetrics(outMetrics)
        } else {
            @Suppress("DEPRECATION")
            val display = requireActivity().windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(outMetrics)
        }
        return outMetrics.heightPixels / 2
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lockNavDrawer()

        binding.selectedOrder.selectedBackground.visibility = INVISIBLE
        initBottomSheet()
        hideAllBottomSheet()
//        bottomSheetOrders.state = BottomSheetBehavior.STATE_HALF_EXPANDED
//        bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_HIDDEN
//        bottomSheetOrderAddresses.state = BottomSheetBehavior.STATE_HIDDEN
        //hideAllBottomSheet()
    }

    private fun initBottomSheet() {
        binding.orderAddresses.visibility = VISIBLE

        bottomSheetOrders = BottomSheetBehavior.from(binding.ordersLayout)
        bottomSheetOrders.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                //if (newState == BottomSheetBehavior.STATE_HIDDEN) viewModel.onCloseOrdersClick()
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })



        bottomSheetOrderDetails = BottomSheetBehavior.from(binding.orderDetailsLayout)

        bottomSheetOrderAddresses = BottomSheetBehavior.from(binding.orderAddresses)
        bottomSheetOrderAddresses.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) expandedDetails()
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun expandedDetails() {
        bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun hideAllBottomSheet() {
        bottomSheetOrders.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetOrderAddresses.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun initListeners() {
        //binding.backFull.setOnClickListener { findNavController().popBackStack() }
        binding.showOrdersFab.setOnClickListener { viewModel.onDetailClick() }
        binding.toRegistration.setOnClickListener { viewModel.toRegistrationClick() }
        binding.closeOrders.setOnClickListener { viewModel.onCloseOrdersClick() }


        binding.carChangeImage.setOnClickListener { viewModel.onChangeCarNumberClick() }
        binding.addressesOrder.setOnClickListener { showAddresses() }
        binding.addressClose.setOnClickListener { showDetails() }
        binding.toRegistration.setOnClickListener { viewModel.toRegistrationClick() }
        binding.takeOrder.setOnClickListener { viewModel.confirmTakeOrderClick() }
        binding.closeOrderDetails.setOnClickListener { viewModel.onCloseOrderDetailsClick() }

    }

    private fun showAddresses() {
        bottomSheetOrders.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetOrderAddresses.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initStateObserve() {

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.title.text = it.label
        }

        viewModel.navigateToDialogInfo.observe(viewLifecycleOwner) {
            showDialogInfo(it)
        }

        viewModel.navigationState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrdersNavigationState.NavigateToOrderDetails -> {

//                    findNavController().navigate(
//                        CourierOrderFragmentDirections.actionCourierOrderFragmentToCourierOrderDetailsFragment(
//                            CourierOrderDetailsParameters(
//                                it.title,
//                                it.orderNumber,
//                                it.order,
//                                it.warehouseLatitude,
//                                it.warehouseLongitude
//                            )
//                        )
//                    )

                    bottomSheetOrders.state = BottomSheetBehavior.STATE_HIDDEN
                    bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_EXPANDED
                    bottomSheetOrderAddresses.state = BottomSheetBehavior.STATE_HIDDEN

                }
                is CourierOrdersNavigationState.NavigateToCarNumber ->
                    findNavController().navigate(
                        CourierOrderFragmentDirections.actionCourierOrderFragmentToCourierCarNumberFragment(
                            CourierCarNumberParameters(
                                it.title,
                                it.orderNumber,
                                it.order,
                                it.warehouseLatitude,
                                it.warehouseLongitude
                            )
                        )
                    )
                CourierOrdersNavigationState.NavigateToRegistration -> {
                    findNavController().navigate(
                        CourierOrderFragmentDirections.actionCourierOrdersFragmentToAuthNavigation()
                    )
                }
                CourierOrdersNavigationState.NavigateToWarehouse -> findNavController().popBackStack()
                CourierOrdersNavigationState.NavigateToOrders -> {
                    bottomSheetOrders.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                    bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_HIDDEN
                    bottomSheetOrderAddresses.state = BottomSheetBehavior.STATE_HIDDEN
                }
                CourierOrdersNavigationState.NavigateToRegistrationDialog -> {}
            }
        }

        viewModel.orders.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierOrderItemState.ShowItems -> {
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

        viewModel.waitLoader.observe(viewLifecycleOwner) { state ->
            when (state) {
                WaitLoader.Wait -> {
                    binding.holdLayout.visibility = VISIBLE
                    binding.orderProgress.visibility = VISIBLE
                }
                WaitLoader.Complete -> {
                    binding.holdLayout.visibility = GONE
                    binding.orderProgress.visibility = GONE
                }
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
                            R.color.colorPrimary
                        )
                    )
                }
                CourierOrderShowDetailsState.Progress -> {}
            }
        }

        viewModel.demoState.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    binding.toRegistration.visibility = VISIBLE
                }
                false -> {
                    binding.toRegistration.visibility = GONE
                }
            }
        }

        viewModel.orderInfo.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderDetailsInfoUIState.InitOrderInfo -> {
                    with(binding.selectedOrder) {
                        linerNumber.text = it.itemId
                        orderId.text = it.orderId
                        cost.text = it.cost
                        cargo.text = it.cargo
                        countOffice.text = it.countPvz
                        reserve.text = it.reserve
                    }
                    showDetails()
                    updateHeightInfoPixels()
                }
                is CourierOrderDetailsInfoUIState.NumberSpanFormat -> {
                    binding.carNumber.text = it.numberFormat
                }
            }
        }

        viewModel.orderAddresses.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderAddressesUIState.InitItems -> {
                    binding.emptyList.visibility = GONE
                    binding.addresses.visibility = VISIBLE
                    binding.takeOrder.isEnabled = true
                    val callback = object : CourierOrderDetailsAddressAdapter.OnItemClickCallBack {
                        override fun onItemClick(index: Int) {
                            viewModel.onItemClick(index)
                        }
                    }
                    addressAdapter =
                        CourierOrderDetailsAddressAdapter(requireContext(), it.items, callback)
                    binding.addresses.adapter = addressAdapter
                }
                is CourierOrderAddressesUIState.Empty -> {
                    binding.emptyList.visibility = VISIBLE
                    binding.addresses.visibility = GONE
                    binding.takeOrder.isEnabled = false
                }
                is CourierOrderAddressesUIState.UpdateItems -> {
                    addressAdapter.clear()
                    addressAdapter.addItems(it.items)
                    addressAdapter.notifyDataSetChanged()
                }
            }
        }

    }

    private fun showDetails() {
        bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetOrderAddresses.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun updateHeightInfoPixels() {
//        binding.orderDetails.viewTreeObserver.addOnGlobalLayoutListener(
//            object :
//                ViewTreeObserver.OnGlobalLayoutListener {
//                override fun onGlobalLayout() {
//                    binding.orderDetails.viewTreeObserver.removeOnGlobalLayoutListener(
//                        this
//                    )
//                    viewModel.onHeightInfoBottom(binding.orderDetails.height)
//                }
//            })
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
                R.color.tertiary
            )
        )
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

    private fun initRecyclerViewOrders() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.orders.layoutManager = layoutManager
        binding.orders.addItemDecoration(gethorizontalDividerDecoration())
        binding.orders.setHasFixedSize(true)
        initSmoothScrollerOrders()
    }

    private fun initSmoothScrollerOrders() {
        smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
    }

    private fun initRecyclerViewAddress() {
        addressLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.addresses.layoutManager = addressLayoutManager
        binding.addresses.setHasFixedSize(true)
        initSmoothScrollerAddress()
    }

    private fun initSmoothScrollerAddress() {
        addressSmoothScroller = object : LinearSmoothScroller(context) {
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