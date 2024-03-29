package ru.wb.go.ui.courierintransit

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.*
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.adapters.DefaultAdapterDelegate
import ru.wb.go.databinding.CourierIntransitFragmentBinding
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.BaseServiceFragment
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.couriercompletedelivery.CourierCompleteDeliveryParameters
import ru.wb.go.ui.courierintransit.delegates.*
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.dialogs.ProgressDialogFragment
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.managers.ErrorDialogData


class CourierIntransitFragment :
    BaseServiceFragment<CourierIntransitViewModel, CourierIntransitFragmentBinding>(
        CourierIntransitFragmentBinding::inflate
    ) {

    override val viewModel by viewModel<CourierIntransitViewModel>()

    private val adapter: DefaultAdapterDelegate
        get() = binding.routes.adapter as DefaultAdapterDelegate


    private val itemCallback = object : OnCourierIntransitCallback {
        override fun onPickToPointClick(idItem: Int) {
            viewModel.onItemOfficeClick(idItem)
            val sheetBehavior = BottomSheetBehavior.from(binding.bottomSheetLayoutListItem)
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecyclerView()
        initAdapter()
        initObservable()
        initListeners()
        initReturnDialogResult()
        viewModel.update()
    }

    private fun initReturnDialogResult() {
        setFragmentResultListener(DIALOG_INFO_TAG) { _, bundle ->
            if (bundle.containsKey(DialogInfoFragment.DIALOG_INFO_BACK_KEY)) {
                viewModel.onErrorDialogConfirmClick()
            }
        }
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lockNavDrawer()
    }


    /*private var currentOrderId1: String? = null
    private fun showBottomSheetDialogCardOfOrders() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.courier_intransit_card_of_orders)
        bottomSheetDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        bottomSheetDialog.context.setTheme(R.style.AppBottomSheetDialogTheme)

        val sheetBehavior = BottomSheetBehavior.from(binding.bottomSheetLayoutListItem)
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        bottomSheetDialog.show()

        val closeButton = bottomSheetDialog.findViewById<ImageView>(R.id.addresses_close1)
        val scheduleOrder = bottomSheetDialog.findViewById<TextView>(R.id.time_work_detail1)
        val address = bottomSheetDialog.findViewById<TextView>(R.id.fullAddressOrder1)
        val currentOrderId1 = bottomSheetDialog.findViewById<TextView>(R.id.currentOrderId1)
        val scanQrPvzButton =
            bottomSheetDialog.findViewById<AppCompatButton>(R.id.scan_qr_pvz_button1)
        val navigatorButton = bottomSheetDialog.findViewById<ImageButton>(R.id.navigator_button1)
        val scanQrPvzCompleteButton =
            bottomSheetDialog.findViewById<ImageButton>(R.id.scan_qr_pvz_complete_button1)
        val completeDeliveryButton =
            bottomSheetDialog.findViewById<AppCompatButton>(R.id.complete_delivery_button1)
        val imageItemBorder = bottomSheetDialog.findViewById<ImageView>(R.id.image_item_border1)
        val selectedBackground1 =
            bottomSheetDialog.findViewById<ImageView>(R.id.selected_background1)
        val icon = bottomSheetDialog.findViewById<ImageView>(R.id.icon1)

        visibleAppCompatButton(binding.scanQrPvzButton, scanQrPvzButton!!)
        visibleAppCompatButton(binding.navigatorButton, navigatorButton!!)
        visibleAppCompatButton(binding.scanQrPvzCompleteButton, scanQrPvzCompleteButton!!)
        visibleAppCompatButton(binding.completeDeliveryButton, completeDeliveryButton!!)

        fun initialiseBackgroundForItem(
            @DrawableRes imageItemBorderValue: Int,
            @DrawableRes selectedBackgroundValue: Int,
            @DrawableRes iconValue: Int
        ) {
            imageItemBorder?.setImageResource(imageItemBorderValue)
            selectedBackground1?.setImageResource(selectedBackgroundValue)
            icon?.setImageResource(iconValue)
        }

        viewModel.courierIntransitEmptyItemLiveData.observe(viewLifecycleOwner){
            address?.text = it.fullAddress
            scheduleOrder?.text = it.timeWork
            currentOrderId1?.text = this.currentOrderId1
        }

        viewModel.courierIntransitCompleteItemLiveData.observe(viewLifecycleOwner){
            address?.text = it.fullAddress
            scheduleOrder?.text = it.timeWork
            currentOrderId1?.text = this.currentOrderId1
        }

        viewModel.courierIntransitUndeliveredAllItemLiveData.observe(viewLifecycleOwner){
            address?.text = it.fullAddress
            scheduleOrder?.text = it.timeWork
            currentOrderId1?.text = this.currentOrderId1
        }

        viewModel.courierIntransitUnloadingExpectsItemLiveData.observe(viewLifecycleOwner){
            address?.text = it.fullAddress
            scheduleOrder?.text = it.timeWork
            currentOrderId1?.text = this.currentOrderId1
        }

        viewModel.currentItemBackgroundForBottomSheet.observe(viewLifecycleOwner) {
            when (it) {
                IntransitItemType.Empty -> {
                    initialiseBackgroundForItem(
                        R.drawable.ic_courier_intransit_item_border_primary,
                        R.drawable.courier_background_select_warehouse,
                        R.drawable.ic_intransit_item_empty
                    )

                }
                IntransitItemType.UnloadingExpects -> {
                    initialiseBackgroundForItem(
                        R.drawable.ic_courier_intransit_item_border_green,
                        R.drawable.courier_intransit_background_select_green,
                        R.drawable.ic_intransit_item_wait_new
                    )
                }
                IntransitItemType.FailedUnloadingAll -> {
                    initialiseBackgroundForItem(
                        R.drawable.ic_courier_intransit_item_border_red,
                        R.drawable.courier_intransit_background_select_red,
                        R.drawable.ic_intransit_item_error
                    )
                }

                IntransitItemType.Complete -> {
                    initialiseBackgroundForItem(
                        R.drawable.ic_courier_intransit_item_border_green,
                        R.drawable.courier_intransit_background_select_green,
                        R.drawable.ic_intransit_item_complete
                        )
                }
            }
        }

        closeButton!!.setOnClickListener {
            bottomSheetDialog.dismiss()
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
        }

        scanQrPvzButton.setOnClickListener {
            viewModel.onScanQrPvzClick()
            bottomSheetDialog.dismiss()
        }
        navigatorButton.setOnClickListener {
            viewModel.onNavigatorClick()
            bottomSheetDialog.dismiss()
        }
        scanQrPvzCompleteButton.setOnClickListener {
            bottomSheetDialog.dismiss()
            viewModel.onScanQrPvzClick()
        }
        completeDeliveryButton.setOnClickListener {
            bottomSheetDialog.dismiss()
            viewModel.onCompleteDeliveryClick()
        }
    }

    private fun visibleAppCompatButton(view: View, bottomSheetButton: View) {
        if (view.isVisible) {
            bottomSheetButton.isVisible = true
        }
    }
*/

    private fun setColorNavigatorTint(@ColorRes colorRes: Int) {
        binding.navigatorButton1.setColorFilter(
            ContextCompat.getColor(requireContext(), colorRes),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun initObservable() {

        viewModel.courierIntransitEmptyItemLiveData.observe(viewLifecycleOwner){
            binding.fullAddressOrder1.text = it.fullAddress
            binding.timeWorkDetail1.text = it.timeWork
            binding.deliveryTotalCount1.text = "${it.deliveryCount} / ${it.fromCount}"
            setColorNavigatorTint(R.color.colorPrimary)
        }

        viewModel.courierIntransitCompleteItemLiveData.observe(viewLifecycleOwner){
            binding.fullAddressOrder1.text = it.fullAddress
            binding.timeWorkDetail1.text = it.timeWork
            binding.deliveryTotalCount1.text = "${it.deliveryCount} / ${it.fromCount}"
            setColorNavigatorTint(R.color.green)
        }

        viewModel.courierIntransitUndeliveredAllItemLiveData.observe(viewLifecycleOwner){
            binding.fullAddressOrder1.text = it.fullAddress
            binding.timeWorkDetail1.text = it.timeWork
            binding.deliveryTotalCount1.text = "${it.deliveryCount} / ${it.fromCount}"
            setColorNavigatorTint(R.color.red)
        }

        viewModel.courierIntransitUnloadingExpectsItemLiveData.observe(viewLifecycleOwner){
            binding.fullAddressOrder1.text = it.fullAddress
            binding.timeWorkDetail1.text = it.timeWork
            binding.deliveryTotalCount1.text = "${it.deliveryCount} / ${it.fromCount}"
            setColorNavigatorTint(R.color.green)
        }


        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            //currentOrderId1 = it.label
            binding.currentOrderId.text = it.label
        }

        viewModel.navigateToErrorDialog.observe(viewLifecycleOwner) {
            showDialogInfo(it)
        }

        viewModel.navigatorState.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierIntransitNavigatorUIState.Disable -> {
                    binding.addressConstraint.isGone = true
                }
                is CourierIntransitNavigatorUIState.Enable -> {
                    binding.addressConstraint.isVisible = true
                    //showBottomSheetDialogCardOfOrders()
                }
            }
        }

        viewModel.beepEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierIntransitScanOfficeBeepState.Office -> scanOfficeAccepted()
                CourierIntransitScanOfficeBeepState.UnknownQrOffice -> scanOfficeFailed()
                CourierIntransitScanOfficeBeepState.WrongOffice -> scanWrongOffice()
            }
        }

        viewModel.intransitTime.observe(viewLifecycleOwner) {
            when (it) {
                is CourierIntransitTimeState.Time -> {
                    binding.mapTimer.text = it.time
                }
            }
        }

        viewModel.isEnableBottomState.observe(viewLifecycleOwner) { state ->
            binding.scanQrPvzCompleteButton.isEnabled = state
            binding.completeDeliveryButton.isEnabled = state
        }

        viewModel.intransitOrders.observe(viewLifecycleOwner) {
            when (it) {
                is CourierIntransitItemState.InitItems -> {
                    binding.deliveryTotalCount.text = it.boxTotal

                    val value = it.boxTotal.split("/")
                    binding.progressLimit.max = value[1].toInt()
                    binding.progressLimit.progress = value[0].toInt()
                    if(value[0] == value[1]){
//                        binding.box.foregroundTintList = ColorStateList.valueOf(resources.getColor(R.color.green,context?.theme))
//                        binding.deliveryTotalCount.setTextColor(resources.getColor(R.color.green,context?.theme))
                        binding.progressLimit.progressTintList =  ColorStateList.valueOf(resources.getColor(R.color.green,context?.theme))
                    }
                    binding.emptyList.visibility = GONE
                    binding.routes.visibility = VISIBLE
                    displayItems(it.items)
                }
                is CourierIntransitItemState.Empty -> {
                    binding.emptyList.visibility = VISIBLE
                    binding.routes.visibility = GONE
                }
                is CourierIntransitItemState.UpdateItems -> displayItems(it.items)
                is CourierIntransitItemState.CompleteDelivery -> {
                    binding.scanQrPvzButton.visibility = INVISIBLE
                    binding.scanQrPvzCompleteButton.visibility = VISIBLE
                    binding.completeDeliveryButton.visibility = VISIBLE
                }
                is CourierIntransitItemState.ScrollTo -> binding.routes.scrollToPosition(it.position)

            }
        }

        viewModel.waitLoader.observe(viewLifecycleOwner) {
            when (it) {
                WaitLoader.Wait -> showProgressDialog()
                WaitLoader.Complete -> closeProgressDialog()
            }
        }

        viewModel.navigateToDialogConfirmInfo.observe(viewLifecycleOwner) {
            showDialogConfirmInfo(it.type, it.title, it.message, it.positive, it.negative)
        }

        viewModel.navigationState.observe(viewLifecycleOwner) {
            when (it) {
                CourierIntransitNavigationState.NavigateToScanner -> {
                    binding.scanQrPvzButton.isEnabled = false
                    binding.scanQrPvzCompleteButton.isEnabled = false
                    binding.completeDeliveryButton.isEnabled = false

                    binding.holdList.visibility = VISIBLE

                    findNavController().navigate(
                        CourierIntransitFragmentDirections.actionCourierIntransitFragmentToCourierIntransitOfficeScannerFragment()
                    )
                }
                is CourierIntransitNavigationState.NavigateToCompleteDelivery -> {
                    findNavController().navigate(
                        CourierIntransitFragmentDirections.actionCourierIntransitFragmentToCourierCompleteDeliveryFragment(
                            CourierCompleteDeliveryParameters(
                                it.amount,
                                it.unloadedCount,
                                it.fromCount
                            )
                        )
                    )
                }
                is CourierIntransitNavigationState.NavigateToNavigator -> {
                    val geoLocation = Uri.parse("geo:${it.latitude},${it.longitude}")
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = geoLocation
                    startActivity(intent)
                }
            }
        }

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
        val adapter = adapter
        adapter.clear()
        adapter.addItems(items)
        adapter.notifyDataSetChanged()
    }

    private fun initListeners() {
        binding.navigatorButton1.setOnClickListener { viewModel.onNavigatorClick() }
        binding.scanQrPvzButton.setOnClickListener { viewModel.onScanQrPvzClick() }
        binding.scanQrPvzCompleteButton.setOnClickListener { viewModel.onScanQrPvzClick() }
        binding.completeDeliveryButton.setOnClickListener { viewModel.onCompleteDeliveryClick() }
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

    private fun showDialogConfirmInfo(
        style: Int,
        title: String,
        message: String,
        positiveButtonName: String,
        negativeButtonName: String
    ) {
        DialogConfirmInfoFragment.newInstance(
            type = style,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName,
            negativeButtonName = negativeButtonName
        ).show(parentFragmentManager, DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_TAG)
    }

    private fun initRecyclerView() {
        binding.routes.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.routes.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.routes.setHasFixedSize(true)
        initSmoothScroller()
    }

    private fun initSmoothScroller() {
          object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
    }

    private fun initAdapter() {
        binding.routes.adapter = with(DefaultAdapterDelegate()) {
            addDelegate(CourierIntransitEmptyDelegate(requireContext(), itemCallback))
            addDelegate(CourierIntransitCompleteDelegate(requireContext(), itemCallback))
            addDelegate(CourierIntransitUndeliveredAllDelegate(requireContext(), itemCallback))
            addDelegate(CourierIntransitUnloadingExpectsDelegate(requireContext(), itemCallback))
        }
    }

    private fun scanOfficeAccepted() {
        viewModel.play(R.raw.qr_office_accepted)
    }

    private fun scanOfficeFailed() {
        viewModel.play(R.raw.qr_office_failed)
    }

    private fun scanWrongOffice() {
        viewModel.play(R.raw.wrongoffice)
    }
}