package ru.wb.go.ui.courierorders

import android.annotation.SuppressLint
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
import androidx.fragment.app.setFragmentResultListener
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
import ru.wb.go.ui.couriercarnumber.CourierCarNumberFragment.Companion.COURIER_CAR_NUMBER_ID_EDIT_KEY
import ru.wb.go.ui.couriercarnumber.CourierCarNumberParameters
import ru.wb.go.ui.courierorders.delegates.CourierOrderDelegate
import ru.wb.go.ui.courierorders.delegates.OnCourierOrderCallback
import ru.wb.go.ui.courierwarehouses.gethorizontalDividerDecoration
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.managers.ErrorDialogData


class CourierOrdersFragment : Fragment() {

    companion object {
        const val COURIER_ORDER_ID_KEY = "courier_order_id_key"
        const val DIALOG_TASK_NOT_EXIST_RESULT_TAG = "DIALOG_TASK_NOT_EXIST_RESULT_TAG"
        const val DIALOG_CONFIRM_SCORE_RESULT_TAG = "DIALOG_CONFIRM_SCORE_RESULT_TAG"
        const val DIALOG_REGISTRATION_RESULT_TAG = "DIALOG_REGISTRATION_RESULT_TAG"
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

    private var isDetailsShowing: Boolean = false
    private var isAddressShowing: Boolean = false

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
        initReturnDialogResult()

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
        viewModel.resumeInit()

        bottomSheetOrders.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetOrderAddresses.state = BottomSheetBehavior.STATE_HIDDEN


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

    private fun initReturnDialogResult() {

        setFragmentResultListener(DIALOG_TASK_NOT_EXIST_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DialogInfoFragment.DIALOG_INFO_BACK_KEY)) {
                viewModel.onTaskNotExistConfirmClick()
            }
        }

        setFragmentResultListener(DIALOG_CONFIRM_SCORE_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_POSITIVE_KEY)) {
                viewModel.onConfirmOrderClick()
            }
        }

        setFragmentResultListener(DIALOG_INFO_TAG) { _, bundle ->
            if (bundle.containsKey(DialogInfoFragment.DIALOG_INFO_BACK_KEY)) {
                viewModel.goBack()
            }
        }

        setFragmentResultListener(DIALOG_REGISTRATION_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_POSITIVE_KEY)) {
                viewModel.onRegistrationConfirmClick()
            }
            if (bundle.containsKey(DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_NEGATIVE_KEY)) {
                viewModel.onRegistrationCancelClick()
            }
        }
    }

    override fun onResume() {
        LogUtils { logDebugApp("onResume orders") }
        super.onResume()
    }

    override fun onPause() {
        LogUtils { logDebugApp("onPause orders") }
        super.onPause()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.clearSubscription()
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
    }

    private fun initBottomSheet() {
        binding.orderAddresses.visibility = VISIBLE

        bottomSheetOrders = BottomSheetBehavior.from(binding.ordersLayout)
        bottomSheetOrders.skipCollapsed = true
        bottomSheetOrders.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN && !isDetailsShowing) viewModel.onCloseOrdersClick()
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        bottomSheetOrderDetails = BottomSheetBehavior.from(binding.orderDetailsLayout)
        bottomSheetOrderDetails.skipCollapsed = true
        bottomSheetOrderDetails.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN && !isAddressShowing)
                    viewModel.onCloseOrderDetailsClick(getHalfHeightDisplay())
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })


        bottomSheetOrderAddresses = BottomSheetBehavior.from(binding.orderAddresses)
        bottomSheetOrderAddresses.skipCollapsed = true
        bottomSheetOrderAddresses.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    isAddressShowing = false
                    viewModel.onShowOrderDetailsClick()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun hideAllBottomSheet() {
        bottomSheetOrders.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetOrderAddresses.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun showAddresses() {
        binding.navDrawerMenu.visibility = INVISIBLE
        binding.toRegistration.visibility = INVISIBLE
        binding.showAll.visibility = INVISIBLE
        isAddressShowing = true
        bottomSheetOrders.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetOrderAddresses.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun initListeners() {
        binding.navDrawerMenu.setOnClickListener { (activity as NavDrawerListener).showNavDrawer() }
        binding.showAll.setOnClickListener {
            if (bottomSheetOrders.state == BottomSheetBehavior.STATE_EXPANDED)
                viewModel.onShowAllOrdersClick()
            else if (bottomSheetOrderDetails.state == BottomSheetBehavior.STATE_EXPANDED)
                viewModel.onShowAllOrderDetailsClick()
        }
        binding.toRegistration.setOnClickListener { viewModel.toRegistrationClick() }
        binding.closeOrders.setOnClickListener { viewModel.onCloseOrdersClick() }

        binding.carChangeImage.setOnClickListener { viewModel.onChangeCarNumberClick() }
        binding.toRegistration.setOnClickListener { viewModel.toRegistrationClick() }
        binding.takeOrder.setOnClickListener { viewModel.onConfirmTakeOrderClick() }
        binding.closeOrderDetails.setOnClickListener {
            viewModel.onCloseOrderDetailsClick(
                getHalfHeightDisplay()
            )
        }
        binding.addressesOrder.setOnClickListener { showAddresses() }
        binding.addressClose.setOnClickListener { viewModel.onShowOrderDetailsClick() }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initStateObserve() {

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Int>(COURIER_CAR_NUMBER_ID_EDIT_KEY)
            ?.observe(viewLifecycleOwner) {
                val courierOrdersInit =
                    if (bottomSheetOrders.state == BottomSheetBehavior.STATE_EXPANDED) {
                        CourierOrdersInit.Orders
                    } else {
                        CourierOrdersInit.Details
                    }
                viewModel.onChangeCarNumberComplete(courierOrdersInit, it)
            }

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.title.text = it.label
        }

        viewModel.navigateToDialogInfo.observe(viewLifecycleOwner) {
            showDialogInfo(it)
        }

        viewModel.navigationState.observe(viewLifecycleOwner) {
            when (it) {

                CourierOrdersNavigationState.NavigateToBack -> findNavController().popBackStack()
                is CourierOrdersNavigationState.NavigateToOrderDetails -> {
                    isDetailsShowing = true
                    binding.navDrawerMenu.visibility = if (it.isDemo) INVISIBLE else VISIBLE
                    binding.toRegistration.visibility = VISIBLE
                    binding.showAll.visibility = VISIBLE

                    bottomSheetOrders.state = BottomSheetBehavior.STATE_HIDDEN
                    bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_EXPANDED
                    bottomSheetOrderAddresses.state = BottomSheetBehavior.STATE_HIDDEN

                }
                is CourierOrdersNavigationState.NavigateToCarNumber ->
                    findNavController().navigate(
                        CourierOrdersFragmentDirections.actionCourierOrderFragmentToCourierCarNumberFragment(
                            CourierCarNumberParameters(it.id)
                        )
                    )

                CourierOrdersNavigationState.NavigateToRegistration -> {
                    findNavController().navigate(
                        CourierOrdersFragmentDirections.actionCourierOrdersFragmentToAuthNavigation()
                    )
                }
                CourierOrdersNavigationState.NavigateToWarehouse -> findNavController().popBackStack()
                CourierOrdersNavigationState.NavigateToOrders -> {
                    bottomSheetOrders.state = BottomSheetBehavior.STATE_EXPANDED
                    bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_HIDDEN
                    bottomSheetOrderAddresses.state = BottomSheetBehavior.STATE_HIDDEN
                }
                CourierOrdersNavigationState.NavigateToAddresses -> {

                }

                CourierOrdersNavigationState.NavigateToRegistrationDialog -> {
                    showRegistrationDialogConfirmInfo(
                        DialogInfoStyle.INFO.ordinal,
                        getString(R.string.demo_registration_title_dialog),
                        getString(R.string.demo_registration_message_dialog),
                        getString(R.string.demo_registration_positive_dialog),
                        getString(R.string.demo_registration_negative_dialog)
                    )
                }
                CourierOrdersNavigationState.NavigateToTimer -> {
                    findNavController().navigate(
                        CourierOrdersFragmentDirections.actionCourierOrdersFragmentToCourierOrderTimerFragment()
                    )
                }
            }
        }

        viewModel.orders.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierOrderItemState.ShowItems -> {
                    binding.showAll.visibility = VISIBLE
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

        viewModel.demoState.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    binding.navDrawerMenu.visibility = INVISIBLE
                    binding.toRegistration.visibility = VISIBLE
                    carNumberTextColor(R.color.red)
                    binding.carChangeImage.visibility = GONE
                }
                false -> {
                    binding.navDrawerMenu.visibility = VISIBLE
                    binding.toRegistration.visibility = GONE
                    carNumberTextColor(R.color.primary)
                    binding.carChangeImage.visibility = VISIBLE
                }
            }
        }

        viewModel.orderInfo.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderDetailsInfoUIState.InitOrderInfo -> {
                    with(binding.selectedOrder) {
                        binding.carNumber.text = it.carNumber
                        linerNumber.text = it.itemId
                        orderId.text = it.orderId
                        cost.text = it.cost
                        cargo.text = it.cargo
                        countOffice.text = it.countPvz
                        reserve.text = it.reserve
                    }
                    // showDetails()
                    //updateHeightInfoPixels()
                }
//                is CourierOrderDetailsInfoUIState.NumberSpanFormat -> {
//
//                }
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
                            //viewModel.onItemClick(index)
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

        viewModel.navigateToDialogConfirmScoreInfo.observe(viewLifecycleOwner) {
            showDialogConfirmScoreInfo(it.type, it.title, it.message, it.positive, it.negative)
        }

    }

    private fun carNumberTextColor(color: Int) {
        binding.carNumber.setTextColor(ContextCompat.getColor(requireContext(), color))
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

//    private fun showOrdersDisable() {
//        binding.showOrdersFab.isEnabled = false
//        binding.showOrdersFab.backgroundTintList = ColorStateList.valueOf(
//            ContextCompat.getColor(
//                requireContext(),
//                R.color.tertiary
//            )
//        )
//    }

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

    private fun showRegistrationDialogConfirmInfo(
        style: Int,
        title: String,
        message: String,
        positiveButtonName: String,
        negativeButtonName: String
    ) {
        DialogConfirmInfoFragment.newInstance(
            resultTag = DIALOG_REGISTRATION_RESULT_TAG,
            type = style,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName,
            negativeButtonName = negativeButtonName
        ).show(parentFragmentManager, DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_TAG)
    }

    private fun showDialogConfirmScoreInfo(
        type: Int,
        title: String,
        message: String,
        positiveButtonName: String,
        negativeButtonName: String
    ) {
        DialogConfirmInfoFragment.newInstance(
            DIALOG_CONFIRM_SCORE_RESULT_TAG,
            type,
            title,
            message,
            positiveButtonName,
            negativeButtonName
        ).show(parentFragmentManager, DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_TAG)
    }

}

@Parcelize
data class CourierOrderParameters(
    val warehouseId: Int,
    val warehouseLatitude: Double,
    val warehouseLongitude: Double,
    val address: String
) : Parcelable