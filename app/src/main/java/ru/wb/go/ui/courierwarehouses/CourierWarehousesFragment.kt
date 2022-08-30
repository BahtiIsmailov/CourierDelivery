package ru.wb.go.ui.courierwarehouses

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.View.*
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.CourierWarehouseFragmentBinding
import ru.wb.go.ui.BaseServiceFragment
import ru.wb.go.ui.app.KeyboardListener
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.couriercarnumber.CourierCarNumberFragment
import ru.wb.go.ui.couriercarnumber.CourierCarNumberParameters
import ru.wb.go.ui.couriercarnumber.CourierCarNumberResult
import ru.wb.go.ui.courierorders.*
import ru.wb.go.ui.courierwarehouses.adapter.CourierListAddressesItemAdapter
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.WaitLoaderForOrder
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.map.MapPoint


class CourierWarehousesFragment :
    BaseServiceFragment<CourierWarehousesViewModel, CourierWarehouseFragmentBinding>(
        CourierWarehouseFragmentBinding::inflate
    ) {

    override val viewModel by viewModel<CourierWarehousesViewModel>()
    private var mapPointFromViewModel:MapPoint? = null

    private val bottomSheetOrderDetails: BottomSheetBehavior<ConstraintLayout>
        get() = BottomSheetBehavior.from(binding.orderDetailsLayout)

    private val bottomSheetListOfOrders: BottomSheetBehavior<FrameLayout>
        get() = BottomSheetBehavior.from(binding.listOfOrdersLayout)



    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initFromCarNumber()
        initObservable()
        initListeners()
        initReturnDialogResult()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).unlockNavDrawer()
        (activity as KeyboardListener).panMode()
    }

    private fun initReturnDialogResult() {
        setFragmentResultListener(DIALOG_TASK_NOT_EXIST_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DialogInfoFragment.DIALOG_INFO_BACK_KEY)) {
                //viewModel.onTaskNotExistConfirmClick()
            }
        }

        setFragmentResultListener(DIALOG_CONFIRM_SCORE_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_POSITIVE_KEY)) {
                viewModel.onConfirmOrderClick()
            }
        }

        setFragmentResultListener(DIALOG_INFO_TAG) { _, bundle ->
            if (bundle.containsKey(DialogInfoFragment.DIALOG_INFO_BACK_KEY)) {
                //viewModel.goBack()
            }
        }

        setFragmentResultListener(DIALOG_REGISTRATION_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_POSITIVE_KEY)) {
                viewModel.onRegistrationConfirmClick()
            }
            if (bundle.containsKey(DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_NEGATIVE_KEY)) {
                //viewModel.onRegistrationCancelClick()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObservable() {
         //var adapter: CourierWarehousesAdapter? = null

        viewModel.selectedMapPointForFragment.observeEvent { mapPoint ->
            mapPointFromViewModel = mapPoint
        }
        viewModel.navigateToDialogInfo.observe{
            showDialogInfo(it)
        }

        viewModel.warehouseState.observe{
            when (it) {
                is CourierWarehouseItemState.InitItems -> {
                    //binding.emptyList.visibility = GONE
                    //binding.refresh.isRefreshing = false
                    //binding.items.visibility = VISIBLE
                    val callback = object : CourierWarehousesAdapter.OnItemClickCallBack {
                        override fun onItemClick(index: Int) {
                            viewModel.onItemClick(index)
                        }
                    }
                    //adapter = CourierWarehousesAdapter(requireContext(), it.items, callback)
                    //binding.items.adapter = adapter

                }
                is CourierWarehouseItemState.UpdateItems -> { // когда нажимаешь
//                    adapter?.clear()
//                    adapter?.addItems(it.items)
//                    adapter?.notifyDataSetChanged()
                }
                is CourierWarehouseItemState.Empty -> {
//                    binding.emptyList.visibility = VISIBLE
//                    binding.emptyTitle.text = it.info
                    //binding.refresh.isRefreshing = false
//                    binding.items.visibility = GONE
                }
                is CourierWarehouseItemState.UpdateItem -> {
//                    adapter?.setItem(it.position, it.item)
//                    adapter?.notifyItemChanged(it.position, it.item)
                }
                is CourierWarehouseItemState.ScrollTo -> {
                    smoothScrollToPosition(it.position)
                }
                CourierWarehouseItemState.Success -> {
                    binding.noInternetLayout.isGone = true
                }
                CourierWarehouseItemState.NoInternet -> {
                    binding.noInternetLayout.isVisible = true
                }
            }
        }

        viewModel.showOrderState.observe(viewLifecycleOwner) {
            when (it) {
                CourierOrderShowOrdersState.Disable -> {
//                    with(binding.showOrderFab){
//                         isEnabled = false
//                         //isClickable = false
//                         backgroundTintList = colorFab(R.color.tertiary)
//                    }

                }
                CourierOrderShowOrdersState.Enable -> {
//                    with(binding.showOrderFab) {
//                         isEnabled = true
//                         //isClickable = true
//                         backgroundTintList = colorFab(R.color.colorPrimary)
//                    }
                }
                CourierOrderShowOrdersState.Invisible ->{
//                      binding.showOrderFab.visibility = INVISIBLE
                }
                CourierOrderShowOrdersState.Visible -> {
                    //binding.showOrderFab.visibility = VISIBLE
                }
            }
        }
        viewModel.orderAddresses.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderAddressesUIState.InitItems -> {
//                    binding.emptyList.visibility = GONE
//                    binding.addresses.visibility = VISIBLE
//                    binding.takeOrder.isEnabled = true
//                    val callback = object : CourierOrderDetailsAddressAdapter.OnItemClickCallBack {
//                        override fun onItemClick(index: Int) {
//                            viewModel.onAddressItemClick(index)
//                        }
//                    }
//                    binding.addresses.adapter =
//                        CourierOrderDetailsAddressAdapter(requireContext(), it.items, callback)
                }
                CourierOrderAddressesUIState.Empty -> {
//                    binding.emptyList.visibility = VISIBLE
//                    binding.addresses.visibility = GONE
//                    binding.takeOrder.isEnabled = false
                }
                is CourierOrderAddressesUIState.UpdateItems -> {
//                    val addressAdapter = addressAdapter
//                    addressAdapter.clear()
//                    addressAdapter.addItems(it.items)
//                    addressAdapter.notifyDataSetChanged()
                }
            }
        }

        viewModel.orderDetails.observe{
            when (it) {
                is CourierOrderDetailsInfoUIState.InitOrderDetails -> {
                    with(binding.selectedOrder) {
                        when (it.carNumber) {
                            CarNumberState.Empty -> {
                                binding.carNumber.visibility = GONE
//                                binding.carNumberEmpty.visibility = VISIBLE
                            }
                            is CarNumberState.Indicated -> {
                                binding.carNumber.visibility = VISIBLE
//                                binding.carNumberEmpty.visibility = GONE
                                binding.carNumber.setText(
                                    carNumberSpannable(it.carNumber.carNumber),
                                    TextView.BufferType.SPANNABLE
                                )
                            }
                        }
                        binding.carChangeImage.visibility =
                            if (it.isChangeCarNumber) VISIBLE else GONE
                        linerNumber.text = it.itemId
                        taskDistance.text = it.taskDistance + " км"
                        cost.text = it.cost
                        cargo.text = it.cargo
                        countOffice.text = it.countPvz
                        reserve.text = it.reserve
                    }
                }
            }
        }

        viewModel.waitLoader.observe{ state ->
            when (state) {
                WaitLoader.Wait -> {
                    binding.progress.isVisible = true
                    binding.holdLayout.isVisible = true
                }
                WaitLoader.Complete -> {
                    binding.progress.isGone = true
                    binding.holdLayout.isGone = true
                }
            }
        }
        viewModel.waitLoaderForOrder.observe{ state ->
            when (state) {
                WaitLoaderForOrder.Wait -> {
                    binding.progressOnButton.isVisible = true
                    binding.textOnButton.isGone = true
                }
                WaitLoaderForOrder.Complete -> {
                    binding.progressOnButton.isGone = true
                    binding.textOnButton.isVisible = true
                    binding.warehouseCard.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.fade_out
                        ))
                    binding.warehouseCard.isGone = true
                    binding.closeOrders.isVisible = true
                }
            }
        }

        viewModel.showOrdersState.observe{
            when (it) {
                CourierWarehousesShowOrdersState.Disable -> {}
                is CourierWarehousesShowOrdersState.Enable -> {
                    binding.warehouseCard.isVisible = true
                    it.warehouseItem?.map {warehouseItem ->
                        binding.nameWarehouse.text = warehouseItem.name
                        binding.warehouseAddress.text = warehouseItem.fullAddress
                    }
                    binding.km.text = it.distance
                    binding.warehouseCard.startAnimation(
                            AnimationUtils.loadAnimation(
                                requireContext(),
                                R.anim.fade_in
                            ))
                }
            }
        }

        viewModel.demoState.observe{
            when (it) {
                true -> {
                    binding.navDrawerMenu.visibility = INVISIBLE
                    binding.toRegistration.visibility = VISIBLE
                    binding.supportApp.visibility = INVISIBLE
                }
                false -> {
                    binding.navDrawerMenu.visibility = VISIBLE
                    binding.toRegistration.visibility = GONE
                    binding.supportApp.visibility = VISIBLE
                }
            }
        }

        viewModel.navigationState.observe{
            when (it) {
                CourierWarehousesNavigationState.NavigateToBack -> findNavController().popBackStack()
                is CourierWarehousesNavigationState.NavigateToCourierOrders -> {
                    viewModel.onMapPointClick(mapPointFromViewModel!!)
                }

                CourierWarehousesNavigationState.NavigateToRegistration -> {
                    findNavController().navigate(
                        CourierWarehousesFragmentDirections.actionCourierWarehousesFragmentToAuthNavigation()
                    )
                }
            }
        }
        viewModel.navigateToDialogConfirmScoreInfo.observe{
            showDialogConfirmScoreInfo(it.type, it.title, it.message, it.positive, it.negative)
        }
        viewModel.visibleButtonBackLiveData.observe{
            if (!it){
                binding.closeOrders.isGone = true
            }else{
                binding.closeOrders.isVisible = true
            }
        }
        viewModel.navigationStateOrder.observe{
            when (it) {
                is CourierOrdersNavigationState.NavigateToCarNumber -> navigateToCarNumber(it)
                CourierOrdersNavigationState.NavigateToRegistration -> navigateToRegistration()
                CourierOrdersNavigationState.NavigateToWarehouse -> {
//                    findNavController().popBackStack(R.id.courier,true)
                }
                CourierOrdersNavigationState.NavigateToOrders -> {
                    hideBottomSheetOrders()
                }
                is CourierOrdersNavigationState.NavigateToOrderDetails ->{
                    showBottomSheetOrderDetails(it.isDemo)
                }

                CourierOrdersNavigationState.NavigateToAddresses -> {
                    showBottomSheetListOfOrders()
                }
                CourierOrdersNavigationState.NavigateToRegistrationDialog ->{
                    showRegistrationDialogConfirmInfo()
                }

                CourierOrdersNavigationState.NavigateToTimer -> navigateToTimer()
                is CourierOrdersNavigationState.ShowAddressDetail -> {
                    if (bottomSheetOrderDetails.state == BottomSheetBehavior.STATE_HIDDEN) {
                        bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                    ResourcesCompat.getDrawable(resources, it.icon, null)
                        ?.let { binding.addressDetailLayoutItem.iconAddress.setImageDrawable(it) }
                    binding.addressDetailLayoutItem.addressDetail.text = it.address
                    binding.addressDetailLayoutItem.timeWorkDetail.text = it.workTime
                    if (binding.addressDetailLayoutItem.root.visibility != VISIBLE) {
                        fadeOut(binding.addressDetailLayoutItem.root).start()
                    }
                }
                CourierOrdersNavigationState.CloseAddressesDetail -> {
                    //fadeIn(binding.addressDetailLayout).start()
                }
                CourierOrdersNavigationState.OnMapClick ->
                    if (isOrderDetailsExpanded()) {
                        viewModel.onMapClickWithDetail()
                    }
                CourierOrdersNavigationState.CourierLoader ->
                    findNavController().navigate(
                        CourierWarehousesFragmentDirections.actionCourierWarehousesFragmentToCourierLoaderFragment()
                    )
            }
        }
        viewModel.orders.observe{ state ->
            //val adapter = adapter
            when (state) {
                is CourierOrderItemState.ShowItems -> {
//                    binding.emptyList.visibility = GONE
//                    binding.orderProgress.visibility = GONE
//                    binding.orders.visibility = VISIBLE
                }
                is CourierOrderItemState.Empty -> {
//                    binding.emptyList.visibility = VISIBLE
//                    binding.orderProgress.visibility = GONE
//                    binding.orders.visibility = GONE
//                    binding.emptyTitle.text = state.info
                }
                is CourierOrderItemState.UpdateItem -> {
//                    adapter.setItem(state.position, state.item)
//                    adapter.notifyItemChanged(state.position, state.item)
                }
                is CourierOrderItemState.UpdateItems -> {
//                    adapter.clear()
//                    adapter.addItems(state.items)
//                    adapter.notifyDataSetChanged()
                }
                is CourierOrderItemState.ScrollTo -> {
                    smoothScrollToPosition(state.position)
                }
            }
        }

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

    private fun isOrderDetailsExpanded() =
        bottomSheetOrderDetails.state == BottomSheetBehavior.STATE_EXPANDED

    private fun navigateToTimer() {
        findNavController().navigate(
            CourierWarehousesFragmentDirections.actionCourierWarehousesFragmentToCourierOrderTimerFragment()
        )
    }

    private fun carNumberSpannable(number: String): Spannable {
        val spannable: Spannable = SpannableString(number)
        spannable.setSpan(RelativeSizeSpan(0.8f), 0, 1, 0)
        spannable.setSpan(RelativeSizeSpan(0.8f), 6, 8, 0)
        return spannable
    }

    private fun fadeOut(view: View): ObjectAnimator {
        val fadeOut =
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
//        fadeOut.duration = FADE_ADDRESS_DETAILS
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                view.visibility = VISIBLE
                view.alpha = 0f
            }
        })
        return fadeOut
    }
    private fun initListeners() {
        binding.navDrawerMenu.setOnClickListener {
            (activity as NavDrawerListener).showNavDrawer()
        }
        binding.toRegistration.setOnClickListener { viewModel.toRegistrationClick() }
        binding.carChangeImage.setOnClickListener { viewModel.onChangeCarNumberClick() }
        binding.toRegistration.setOnClickListener { viewModel.toRegistrationClick() }
        binding.takeOrder.setOnClickListener { viewModel.onConfirmTakeOrderClick() }
        binding.closeOrderDetails.setOnClickListener {
            binding.addressDetailLayoutItem.root.isGone = true
            viewModel.onCloseOrderDetailsClick(getHalfHeightDisplay())
        }
        binding.addressesOrder.setOnClickListener {
            displayItems(viewModel.getOrderAddressItems())
            Log.e("stateItemsBaseItem","${viewModel.getOrderAddressItems()}")
            viewModel.onAddressesClick()
        }

        binding.goToOrder.setOnClickListener { viewModel.onNextFab(getHalfHeightDisplay()) }
        binding.updateWhenNoInternet.setOnClickListener { viewModel.getWarehouses() }
        binding.toRegistration.setOnClickListener { viewModel.toRegistrationClick() }
        binding.cardWarehouseClose.setOnClickListener{
            viewModel.onMapPointClick(mapPointFromViewModel!!)
            binding.warehouseCard.startAnimation(
                AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.fade_out
            ))
            binding.warehouseCard.isGone = true
        }
        binding.closeOrders.setOnClickListener {
            viewModel.updateData()
        }
        binding.closeOrdersAddressList.setOnClickListener{
            bottomSheetListOfOrders.state = BottomSheetBehavior.STATE_HIDDEN
            bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_EXPANDED
            binding.navDrawerMenu.isInvisible = false
            binding.supportApp.isInvisible = false
            binding.listOfOrdersLayoutMain.isGone = true
        }

    }


    private fun hideBottomSheetOrders() {
        bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun showBottomSheetOrderDetails(isDemo: Boolean) {
        binding.navDrawerMenu.visibility = if (isDemo) INVISIBLE else VISIBLE
        binding.toRegistration.visibility = if (isDemo) VISIBLE else INVISIBLE
        binding.supportApp.visibility = if (isDemo) INVISIBLE else VISIBLE
        binding.orderDetailsLayout.isVisible = true
        bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun showBottomSheetListOfOrders(){
        binding.navDrawerMenu.isInvisible = true
        binding.supportApp.isInvisible = true
        binding.listOfOrdersLayoutMain.isVisible = true
        hideBottomSheetOrders()
        bottomSheetListOfOrders.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onResume() {
        super.onResume()
        viewModel.resumeInit()
        if (viewModel.isStateCarNumber()){
            viewModel.updateData()
        }else {
            viewModel.getWarehouses()
        }
    }

    private fun navigateToRegistration() {
        findNavController().navigate(
            CourierWarehousesFragmentDirections.actionCourierWarehousesFragmentToAuthNavigation()
        )
    }

    private fun navigateToCarNumber(it: CourierOrdersNavigationState.NavigateToCarNumber) {
        findNavController().navigate(
            CourierWarehousesFragmentDirections.actionCourierWarehousesFragmentToCourierCarNumberFragment(
                CourierCarNumberParameters(it.result)
            )
        )
    }

    private fun initFromCarNumber(){
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<CourierCarNumberResult>(CourierCarNumberFragment.COURIER_CAR_NUMBER_ID_EDIT_KEY)
            ?.observe { viewModel.onChangeCarNumberOrders(it) }
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



//    fun clear() {
//        activity!!.viewModelStore.clear();
//    }


    private fun displayItems(items: MutableSet<CourierOrderDetailsAddressItem>) {
        val adapter = CourierListAddressesItemAdapter()
        binding.listOfOrdersRecycle.adapter = adapter
        adapter.clear()
        adapter.addItems(items)
        adapter.notifyDataSetChanged()
    }

    private fun smoothScrollToPosition(position: Int) {
//        val smoothScroller: SmoothScroller = createSmoothScroller()
//        smoothScroller.targetPosition = position
//        val layoutManager = binding.items.layoutManager as? LinearLayoutManager
//        layoutManager?.startSmoothScroll(smoothScroller)
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

    private fun createSmoothScroller(): SmoothScroller {
        return object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
    }

    companion object {
        const val COURIER_ORDER_ID_KEY = "courier_order_id_key"
        const val DIALOG_TASK_NOT_EXIST_RESULT_TAG = "DIALOG_TASK_NOT_EXIST_RESULT_TAG"
        const val DIALOG_CONFIRM_SCORE_RESULT_TAG = "DIALOG_CONFIRM_SCORE_RESULT_TAG"
        const val DIALOG_REGISTRATION_RESULT_TAG = "DIALOG_REGISTRATION_RESULT_TAG"
        const val FADE_ADDRESS_DETAILS = 50L
    }
}




