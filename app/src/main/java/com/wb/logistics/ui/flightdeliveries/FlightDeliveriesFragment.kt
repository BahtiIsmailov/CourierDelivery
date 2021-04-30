package com.wb.logistics.ui.flightdeliveries

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.wb.logistics.R
import com.wb.logistics.adapters.DefaultAdapter
import com.wb.logistics.databinding.FlightDeliveriesFragmentBinding
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.dialogs.SimpleResultDialogFragment
import com.wb.logistics.ui.flightdeliveries.delegates.*
import com.wb.logistics.ui.nav.NavToolbarTitleListener
import com.wb.logistics.ui.unloading.UnloadingScanParameters
import org.koin.androidx.viewmodel.ext.android.viewModel


class FlightDeliveriesFragment : Fragment() {

    companion object {
        private const val GO_DELIVERY_REQUEST_CODE = 100
        private const val GO_DELIVERY_TAG = "START_DELIVERY_TAG"
    }

    private val viewModel by viewModel<FlightDeliveriesViewModel>()

    private var _binding: FlightDeliveriesFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DefaultAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: SmoothScroller

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FlightDeliveriesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initAdapter()
        initListener()
        initStateObserve()
        viewModel.update()
    }

    private fun initListener() {
        binding.goToDelivery.setOnClickListener {
            viewModel.action(FlightDeliveriesUIAction.GoToDeliveryClick)
        }
    }

    private fun initStateObserve() {

        viewModel.stateUIToolBar.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FlightDeliveriesUIToolbarState.Flight -> updateToolbarLabel(state.label)
                is FlightDeliveriesUIToolbarState.Delivery -> {
                    updateToolbarDeliveryIcon()
                    updateToolbarLabel(state.label)
                }
            }
        }

        viewModel.stateUINav.observe(viewLifecycleOwner, { state ->
            when (state) {
                FlightDeliveriesUINavState.Empty -> {
                }
                FlightDeliveriesUINavState.ShowDeliveryDialog -> showDeliveryDialog()
                FlightDeliveriesUINavState.NavigateToDelivery -> {
                    findNavController().navigate(FlightDeliveriesFragmentDirections.actionFlightDeliveriesFragmentSelf())
                }
                is FlightDeliveriesUINavState.NavigateToUpload -> {
                    findNavController().navigate(FlightDeliveriesFragmentDirections.actionFlightDeliveriesFragmentToUnloadingScanFragment(
                        UnloadingScanParameters(state.dstOfficeId, state.officeName)))
                }
            }
        })

        viewModel.stateUIList.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FlightDeliveriesUIListState.ShowFlight -> {
                    updateStatus(state.receptionBox)
                    displayItems(state.items)
                }
                is FlightDeliveriesUIListState.ProgressFlight -> {
                    updateStatus(state.receptionBox)
                    displayItems(state.items)
                }
                is FlightDeliveriesUIListState.UpdateFlight -> {
                    updateStatus(state.receptionBox)
                    displayItems(state.items)
                }
            }
        }

        viewModel.stateUIBottom.observe(viewLifecycleOwner) { state ->
            when (state) {
                FlightDeliveriesUIBottomState.GoToDelivery -> {
                    binding.goToDelivery.visibility = VISIBLE
                }
                FlightDeliveriesUIBottomState.Empty -> {
                    binding.goToDelivery.visibility = GONE
                }
            }
        }

    }

    private fun showDeliveryDialog() {
        val dialog = SimpleResultDialogFragment.newInstance(
            getString(R.string.flight_deliveries_dialog_title),
            getString(R.string.flight_deliveries_dialog_description),
            getString(R.string.flight_deliveries_dialog_positive_button),
            getString(R.string.flight_deliveries_dialog_negative_button)
        )
        dialog.setTargetFragment(this, GO_DELIVERY_REQUEST_CODE)
        dialog.show(parentFragmentManager, GO_DELIVERY_TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == GO_DELIVERY_REQUEST_CODE) {
            viewModel.action(FlightDeliveriesUIAction.GoToDeliveryConfirmClick)
        }
    }

    private fun updateToolbarLabel(toolbarTitle: String) {
        (activity as NavToolbarTitleListener).updateTitle(toolbarTitle)
    }

    private fun updateToolbarDeliveryIcon() {
        (activity as NavToolbarTitleListener).backButtonIcon(R.drawable.ic_fligt_delivery_transport_doc)
    }

    private fun updateStatus(status: String) {
        binding.status.text = status
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
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)
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
        adapter = with(DefaultAdapter()) {
            addDelegate(FlightDeliveriesDelegate(requireContext(),
                object : OnFlightDeliveriesCallback {
                    override fun onPickToPointClick(idItem: Int) {
                        viewModel.onItemClicked(idItem)
                    }
                }))
            addDelegate(
                FlightDeliveriesRefreshDelegate(
                    requireContext(),
                    object : OnFlightDeliveriesUpdateCallback {
                        override fun onUpdateRouteClick() {
                            viewModel.action(FlightDeliveriesUIAction.Refresh)
                        }
                    })
            )
            addDelegate(FlightDeliveriesProgressDelegate(requireContext()))
        }
        binding.recyclerView.adapter = adapter
    }

}