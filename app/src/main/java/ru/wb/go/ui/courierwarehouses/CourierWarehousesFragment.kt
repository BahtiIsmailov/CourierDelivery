package ru.wb.go.ui.courierwarehouses

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.View.*
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.CourierWarehouseFragmentBinding
import ru.wb.go.ui.BaseServiceFragment
import ru.wb.go.ui.app.KeyboardListener
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.courierorders.CourierOrderItemState
import ru.wb.go.ui.courierorders.CourierOrderParameters
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.WaitLoaderForOrder
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.map.MapPoint
import kotlin.math.log


class CourierWarehousesFragment :
    BaseServiceFragment<CourierWarehousesViewModel, CourierWarehouseFragmentBinding>(
        CourierWarehouseFragmentBinding::inflate
    ) {

    override val viewModel by viewModel<CourierWarehousesViewModel>()
    private var mapPointfromViewModel:MapPoint? = null

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecyclerView()
        initObservable()
        initListeners()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).unlockNavDrawer()
        (activity as KeyboardListener).panMode()
//        binding.refresh.setColorSchemeResources(
//            R.color.colorPrimary,
//            R.color.colorPrimary,
//            R.color.colorPrimary,
//            R.color.colorPrimary
//        )
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun initObservable() {
         var adapter: CourierWarehousesAdapter? = null

        viewModel.selectedMapPointForFragment.observeEvent { mapPoint ->
            mapPointfromViewModel = mapPoint
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
                    adapter = CourierWarehousesAdapter(requireContext(), it.items, callback)
                    //binding.items.adapter = adapter

                }
                is CourierWarehouseItemState.UpdateItems -> { // когда нажимаешь
                    adapter?.clear()
                    adapter?.addItems(it.items)
                    adapter?.notifyDataSetChanged()
                }
                is CourierWarehouseItemState.Empty -> {
                    //binding.emptyList.visibility = VISIBLE
                    //binding.emptyTitle.text = it.info
                    //binding.refresh.isRefreshing = false
                    //binding.items.visibility = GONE
                }
                is CourierWarehouseItemState.UpdateItem -> {
                    adapter?.setItem(it.position, it.item)
                    adapter?.notifyItemChanged(it.position, it.item)
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
                CourierWarehousesShowOrdersState.Disable -> {

                }
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
                    viewModel.onMapPointClick(mapPointfromViewModel!!)
//                    binding.warehouseCard.startAnimation(
//                        AnimationUtils.loadAnimation(
//                            requireContext(),
//                            R.anim.fade_out
//                        ))

                }
//                    findNavController().navigate(
//                        CourierWarehousesFragmentDirections.actionCourierWarehousesFragmentToCourierOrdersFragment(
//                            CourierOrderParameters(
//                                it.officeId,
//                                it.warehouseLatitude,
//                                it.warehouseLongitude,
//                                it.address
//                            )
//                        )
//                    )
                CourierWarehousesNavigationState.NavigateToRegistration -> {
                    findNavController().navigate(
                        CourierWarehousesFragmentDirections.actionCourierWarehousesFragmentToAuthNavigation()
                    )
                }
            }
        }
        viewModel.orders.observe(viewLifecycleOwner) { state ->
            //val adapter = adapter
            when (state) {
                is CourierOrderItemState.ShowItems -> {
//                    binding.emptyList.visibility = GONE
//                    binding.orderProgress.visibility = GONE
//                    binding.orders.visibility = VISIBLE
//                    displayItems(state.items)
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

    private fun initListeners() {
        binding.navDrawerMenu.setOnClickListener { (activity as NavDrawerListener).showNavDrawer() }
        binding.goToOrder.setOnClickListener { viewModel.onNextFab(getHalfHeightDisplay()) }
        //binding.refresh.setOnRefreshListener { viewModel.updateData() }
        binding.updateWhenNoInternet.setOnClickListener { viewModel.updateData(false) }
        binding.toRegistration.setOnClickListener { viewModel.toRegistrationClick() }
        binding.cardWarehouseClose.setOnClickListener{
            viewModel.onMapPointClick(mapPointfromViewModel!!)
            binding.warehouseCard.startAnimation(
                AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.fade_out
            ))
            binding.warehouseCard.isGone = true
        }
        binding.closeOrders.setOnClickListener {
            it.isGone = true
            viewModel.updateData(true)
            //viewModel.onMapPointClick(mapPointfromViewModel!!)
        }

    }


    private fun initRecyclerView() {
        //binding.items.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        //binding.items.addItemDecoration(getHorizontalDividerDecoration())
        //binding.items.setHasFixedSize(true)
        initSmoothScroller()
    }

    private fun initSmoothScroller() {
        object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.resumeInit()// если убрать то показывается дэмо версию
        viewModel.updateData(false)// если убрать то не отображается список складов
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

    private fun smoothScrollToPosition(position: Int) {
        val smoothScroller: SmoothScroller = createSmoothScroller()
        smoothScroller.targetPosition = position
        //val layoutManager = binding.items.layoutManager as? LinearLayoutManager
       // layoutManager?.startSmoothScroll(smoothScroller)
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

}




