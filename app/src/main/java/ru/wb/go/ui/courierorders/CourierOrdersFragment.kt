package ru.wb.go.ui.courierorders

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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
import ru.wb.go.ui.BaseServiceFragment
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.couriercarnumber.CourierCarNumberFragment.Companion.COURIER_CAR_NUMBER_ID_EDIT_KEY
import ru.wb.go.ui.couriercarnumber.CourierCarNumberParameters
import ru.wb.go.ui.couriercarnumber.CourierCarNumberResult
import ru.wb.go.ui.courierorders.delegates.CourierOrderDelegate
import ru.wb.go.ui.courierorders.delegates.OnCourierOrderCallback
import ru.wb.go.ui.courierwarehouses.getHorizontalDividerDecoration
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.managers.ErrorDialogData

class CourierOrdersFragment :
    BaseServiceFragment<CourierOrdersViewModel, CourierOrdersFragmentBinding>(
        CourierOrdersFragmentBinding::inflate
    ) {

    companion object {
        const val COURIER_ORDER_ID_KEY = "courier_order_id_key"
        const val DIALOG_TASK_NOT_EXIST_RESULT_TAG = "DIALOG_TASK_NOT_EXIST_RESULT_TAG"
        const val DIALOG_CONFIRM_SCORE_RESULT_TAG = "DIALOG_CONFIRM_SCORE_RESULT_TAG"
        const val DIALOG_REGISTRATION_RESULT_TAG = "DIALOG_REGISTRATION_RESULT_TAG"
    }

    private lateinit var adapter: DefaultAdapterDelegate
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: SmoothScroller

    private lateinit var addressAdapter: CourierOrderDetailsAddressAdapter
    private lateinit var addressLayoutManager: LinearLayoutManager
    private lateinit var addressSmoothScroller: SmoothScroller

    private lateinit var bottomSheetOrders: BottomSheetBehavior<FrameLayout>
    private lateinit var bottomSheetOrderDetails: BottomSheetBehavior<ConstraintLayout>
    private lateinit var bottomSheetOrderAddresses: BottomSheetBehavior<FrameLayout>

    override val viewModel by viewModel<CourierOrdersViewModel> {
        parametersOf(requireArguments().getParcelable<CourierOrderParameters>(COURIER_ORDER_ID_KEY))
    }

    private val bottomSheetOrdersCallback = object :
        BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                removeBottomSheetOrdersListener()
                viewModel.onCloseOrdersClick()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    private val bottomSheetCallbackOrderDetails = object :
        BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                removeBottomSheetCallbackOrderDetailsListener()
                viewModel.onCloseOrderDetailsClick(getHalfHeightDisplay())
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    private val bottomSheetOrderAddressesCallback = object :
        BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                removeBottomSheetCallbackOrderAddressesListener()
                viewModel.onShowOrderDetailsClick()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, onBackPressedCallback())
    }

    private fun onBackPressedCallback() = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            when {
                isOrdersExpanded() -> viewModel.onCloseOrdersClick()
                isOrderDetailsExpanded() -> viewModel.onCloseOrderDetailsClick(getHalfHeightDisplay())
                isOrderAddressesExpanded() -> viewModel.onShowOrderDetailsClick()
            }
        }
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
        viewModel.resumeInit()
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
        super.onResume()
        if (isOrdersExpanded()) viewModel.updateOrders(getHalfHeightDisplay())
        else if (isOrderDetailsExpanded()) {
            addBottomSheetCallbackOrderDetailsListener()
        }
    }

    override fun onDestroyView() {
        viewModel.clearSubscription()
        super.onDestroyView()
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
        initBottomSheet()
        showBottomSheetOrders()
    }

    private fun initBottomSheet() {
        binding.orderAddresses.visibility = VISIBLE

        bottomSheetOrders = BottomSheetBehavior.from(binding.ordersLayout)
        bottomSheetOrders.skipCollapsed = true

        bottomSheetOrderDetails = BottomSheetBehavior.from(binding.orderDetailsLayout)
        bottomSheetOrderDetails.skipCollapsed = true


        bottomSheetOrderAddresses = BottomSheetBehavior.from(binding.orderAddresses)
        bottomSheetOrderAddresses.skipCollapsed = true
    }

    private fun addBottomSheetOrdersListener() {
        bottomSheetOrders.addBottomSheetCallback(bottomSheetOrdersCallback)
    }

    private fun removeBottomSheetOrdersListener() {
        bottomSheetOrders.removeBottomSheetCallback(bottomSheetOrdersCallback)
    }

    private fun addBottomSheetCallbackOrderDetailsListener() {
        bottomSheetOrderDetails.addBottomSheetCallback(bottomSheetCallbackOrderDetails)
    }

    private fun removeBottomSheetCallbackOrderDetailsListener() {
        bottomSheetOrderDetails.removeBottomSheetCallback(bottomSheetCallbackOrderDetails)
    }

    private fun addBottomSheetCallbackOrderAddressesListener() {
        bottomSheetOrderAddresses.addBottomSheetCallback(bottomSheetOrderAddressesCallback)
    }

    private fun removeBottomSheetCallbackOrderAddressesListener() {
        bottomSheetOrderAddresses.removeBottomSheetCallback(bottomSheetOrderAddressesCallback)
    }

    private fun showAddresses() {
        binding.navDrawerMenu.visibility = INVISIBLE
        binding.toRegistration.visibility = INVISIBLE
        showBottomSheetOrderAddresses()
    }

    private fun initListeners() {
        binding.navDrawerMenu.setOnClickListener { (activity as NavDrawerListener).showNavDrawer() }
        binding.toRegistration.setOnClickListener { viewModel.toRegistrationClick() }
        binding.closeOrders.setOnClickListener { viewModel.onCloseOrdersClick() }

        binding.carChangeImage.setOnClickListener { viewModel.onChangeCarNumberClick() }
        binding.toRegistration.setOnClickListener { viewModel.toRegistrationClick() }
        binding.takeOrder.setOnClickListener {
            viewModel.onConfirmTakeOrderClick()
        }
        binding.closeOrderDetails.setOnClickListener {
            viewModel.onCloseOrderDetailsClick(getHalfHeightDisplay())
        }
        binding.addressesOrder.setOnClickListener { viewModel.onAddressesClick() }
        binding.addressesClose.setOnClickListener { viewModel.onShowOrderDetailsClick() }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initStateObserve() {
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<CourierCarNumberResult>(COURIER_CAR_NUMBER_ID_EDIT_KEY)
            ?.observe(viewLifecycleOwner) { viewModel.onChangeCarNumberOrders(it) }

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.title.text = it.label
        }

        viewModel.navigateToDialogInfo.observe(viewLifecycleOwner) {
            showDialogInfo(it)
        }

        viewModel.navigationState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrdersNavigationState.NavigateToCarNumber -> navigateToCarNumber(it)
                CourierOrdersNavigationState.NavigateToRegistration -> navigateToRegistration()
                CourierOrdersNavigationState.NavigateToWarehouse -> findNavController().popBackStack()
                CourierOrdersNavigationState.NavigateToOrders -> showBottomSheetOrders()
                is CourierOrdersNavigationState.NavigateToOrderDetails ->
                    showBottomSheetOrderDetails(it.isDemo)
                CourierOrdersNavigationState.NavigateToAddresses -> showAddresses()
                CourierOrdersNavigationState.NavigateToRegistrationDialog ->
                    showRegistrationDialogConfirmInfo()
                CourierOrdersNavigationState.NavigateToTimer -> navigateToTimer()
                is CourierOrdersNavigationState.ShowAddressDetail -> {
                    binding.addressDetailLayout.visibility = VISIBLE
                    binding.addressDetail.text = it.address
                }
                CourierOrdersNavigationState.CloseAddressesDetail ->
                    binding.addressDetailLayout.visibility = GONE
                CourierOrdersNavigationState.OnMapClick ->
                    if (isOrderDetailsExpanded()) viewModel.onMapClickWithDetail()
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
                    binding.supportApp.visibility = INVISIBLE
                }
                false -> {
                    binding.navDrawerMenu.visibility = VISIBLE
                    binding.toRegistration.visibility = GONE
                    carNumberTextColor(R.color.primary)
                    binding.carChangeImage.visibility = VISIBLE
                    binding.supportApp.visibility = VISIBLE
                }
            }
        }

        viewModel.orderDetails.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderDetailsInfoUIState.InitOrderDetails -> {
                    with(binding.selectedOrder) {
                        binding.carNumber.text = it.carNumber
                        binding.carChangeImage.visibility =
                            if (it.isChangeCarNumber) VISIBLE else GONE
                        linerNumber.text = it.itemId
                        orderId.text = it.orderId
                        cost.text = it.cost
                        cargo.text = it.cargo
                        countOffice.text = it.countPvz
                        reserve.text = it.reserve
                    }
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
                            viewModel.onAddressItemClick(index)
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

        viewModel.visibleShowAll.observe(viewLifecycleOwner) {
            if (isOrdersExpanded()) viewModel.onShowAllOrdersClick(getHalfHeightDisplay())
            else if (isOrderDetailsExpanded()) viewModel.onShowAllOrderDetailsClick()
        }

    }

    private fun isOrdersExpanded() =
        bottomSheetOrders.state == BottomSheetBehavior.STATE_EXPANDED

    private fun isOrderDetailsExpanded() =
        bottomSheetOrderDetails.state == BottomSheetBehavior.STATE_EXPANDED

    private fun isOrderAddressesExpanded() =
        bottomSheetOrderAddresses.state == BottomSheetBehavior.STATE_EXPANDED

    private fun navigateToTimer() {
        findNavController().navigate(
            CourierOrdersFragmentDirections.actionCourierOrdersFragmentToCourierOrderTimerFragment()
        )
    }

    private fun navigateToRegistration() {
        findNavController().navigate(
            CourierOrdersFragmentDirections.actionCourierOrdersFragmentToAuthNavigation()
        )
    }

    private fun navigateToCarNumber(it: CourierOrdersNavigationState.NavigateToCarNumber) {
        findNavController().navigate(
            CourierOrdersFragmentDirections.actionCourierOrderFragmentToCourierCarNumberFragment(
                CourierCarNumberParameters(it.result)
            )
        )
    }

    private fun showBottomSheetOrders() {
        bottomSheetOrders.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetOrderAddresses.state = BottomSheetBehavior.STATE_HIDDEN
        addBottomSheetOrdersListener()
    }

    private fun showBottomSheetOrderDetails(isDemo: Boolean) {
        binding.navDrawerMenu.visibility = if (isDemo) INVISIBLE else VISIBLE
        binding.toRegistration.visibility = if (isDemo) VISIBLE else INVISIBLE
//        binding.showAll.visibility = VISIBLE
        removeBottomSheetOrdersListener()
        bottomSheetOrders.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetOrderAddresses.state = BottomSheetBehavior.STATE_HIDDEN
        addBottomSheetCallbackOrderDetailsListener()
    }

    private fun showBottomSheetOrderAddresses() {
        removeBottomSheetCallbackOrderDetailsListener()
        bottomSheetOrders.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetOrderAddresses.state = BottomSheetBehavior.STATE_EXPANDED
        addBottomSheetCallbackOrderAddressesListener()
    }

    private fun carNumberTextColor(color: Int) {
        binding.carNumber.setTextColor(ContextCompat.getColor(requireContext(), color))
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

    @SuppressLint("NotifyDataSetChanged")
    private fun displayItems(items: List<BaseItem>) {
        adapter.clear()
        adapter.addItems(items)
        adapter.notifyDataSetChanged()
    }

    private fun initRecyclerViewOrders() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.orders.layoutManager = layoutManager
        binding.orders.addItemDecoration(getHorizontalDividerDecoration())
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
                            viewModel.onOrderClick(idView)
                        }
                    })
            )
        }
        binding.orders.adapter = adapter
    }

    private fun showRegistrationDialogConfirmInfo() {
        DialogConfirmInfoFragment.newInstance(
            resultTag = DIALOG_REGISTRATION_RESULT_TAG,
            type = DialogInfoStyle.INFO.ordinal,
            title = getString(R.string.demo_registration_title_dialog),
            message = getString(R.string.demo_registration_message_dialog),
            positiveButtonName = getString(R.string.demo_registration_positive_dialog),
            negativeButtonName = getString(R.string.demo_registration_negative_dialog)
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