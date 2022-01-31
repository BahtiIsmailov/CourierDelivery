package ru.wb.go.ui.courierwarehouses

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.CourierWarehouseFragmentBinding
import ru.wb.go.ui.app.KeyboardListener
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.courierorders.CourierOrderParameters
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG


class CourierWarehousesFragment : Fragment() {

    private val viewModel by viewModel<CourierWarehousesViewModel>()

    private var _binding: CourierWarehouseFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CourierWarehousesAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: SmoothScroller

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierWarehouseFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecyclerView()
        initObservable()
        initListeners()
        viewModel.update()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).unlockNavDrawer()
        (activity as KeyboardListener).panMode()
        binding.refresh.setColorSchemeResources(
            R.color.refreshColor,
            R.color.refreshColor,
            R.color.refreshColor,
            R.color.refreshColor
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObservable() {

        viewModel.navigateToDialogInfo.observe(viewLifecycleOwner) {
            showDialogInfo(it.type, it.title, it.message, it.button)
        }

        viewModel.warehouses.observe(viewLifecycleOwner) {
            when (it) {
                is CourierWarehouseItemState.InitItems -> {
                    binding.emptyList.visibility = GONE
                    binding.refresh.isRefreshing = false
                    binding.items.visibility = VISIBLE
                    val callback = object : CourierWarehousesAdapter.OnItemClickCallBack {
                        override fun onItemClick(index: Int) {
                            viewModel.onItemClick(index)
                        }
                    }
                    adapter = CourierWarehousesAdapter(requireContext(), it.items, callback)
                    binding.items.adapter = adapter
                }
                is CourierWarehouseItemState.UpdateItems -> {
                    adapter.clear()
                    adapter.addItems(it.items)
                    adapter.notifyDataSetChanged()
                }
                is CourierWarehouseItemState.Empty -> {
                    binding.emptyList.visibility = VISIBLE
                    binding.refresh.isRefreshing = false
                    binding.items.visibility = GONE
                    binding.emptyTitle.text = it.info
                }
                is CourierWarehouseItemState.UpdateItem -> {
                    adapter.setItem(it.position, it.item)
                    adapter.notifyItemChanged(it.position, it.item)
                }
                is CourierWarehouseItemState.ScrollTo -> {
                    smoothScrollToPosition(it.position)
                }
            }
        }

        viewModel.warehousesProgressState.observe(viewLifecycleOwner) {
            binding.refresh.isRefreshing = when (it) {
                CourierWarehousesProgressState.Progress -> true
                CourierWarehousesProgressState.ProgressComplete -> false
            }
        }

        viewModel.holdState.observe(viewLifecycleOwner) {
            binding.holdLayout.visibility = when (it) {
                true -> VISIBLE
                false -> GONE
            }
        }

        viewModel.showOrdersState.observe(viewLifecycleOwner) {
            when (it) {
                CourierWarehousesShowOrdersState.Disable -> showOrdersDisable()
                CourierWarehousesShowOrdersState.Enable -> {
                    binding.showOrdersFab.isEnabled = true
                    binding.showOrdersFab.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.fab_enable
                        )
                    )
                }
                CourierWarehousesShowOrdersState.Progress -> {}
            }
        }

        viewModel.demoState.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    binding.navDrawerMenu.visibility = GONE
                    binding.toRegistrationBack.visibility = VISIBLE
                    binding.toRegistration.visibility = VISIBLE
                }
                false -> {
                    binding.navDrawerMenu.visibility = VISIBLE
                    binding.toRegistrationBack.visibility = GONE
                    binding.toRegistration.visibility = GONE
                }
            }
        }

        viewModel.navigationState.observe(viewLifecycleOwner) {
            when (it) {
                CourierWarehousesNavigationState.NavigateToBack -> findNavController().popBackStack()
                is CourierWarehousesNavigationState.NavigateToCourierOrder ->
                    findNavController().navigate(
                        CourierWarehousesFragmentDirections.actionCourierWarehouseFragmentToCourierOrderFragment(
                            CourierOrderParameters(
                                it.officeId,
                                it.warehouseLatitude,
                                it.warehouseLongitude,
                                it.address
                            )
                        )
                    )
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

    private fun initListeners() {
        binding.navDrawerMenu.setOnClickListener { (activity as NavDrawerListener).showNavDrawer() }
        binding.showOrdersFab.setOnClickListener { viewModel.onDetailClick() }
        binding.showAll.setOnClickListener { viewModel.onShowAllClick() }
        binding.refresh.setOnRefreshListener { viewModel.update() }
        binding.toRegistration.setOnClickListener { viewModel.toRegistrationClick() }
    }

    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.items.layoutManager = layoutManager
        binding.items.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.items.setHasFixedSize(true)
        initSmoothScroller()
    }

    private fun initSmoothScroller() {
        smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDialogInfo(
        type: Int,
        title: String,
        message: String,
        positiveButtonName: String
    ) {
        DialogInfoFragment.newInstance(
            type = type,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName
        ).show(parentFragmentManager, DIALOG_INFO_TAG)
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

}