package com.wb.logistics.ui.flightdeliveries

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.*
import com.wb.logistics.R
import com.wb.logistics.adapters.DefaultAdapter
import com.wb.logistics.databinding.FlightDeliveriesFragmentBinding
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.dialogs.SimpleResultDialogFragment
import com.wb.logistics.ui.flightdeliveries.delegates.*
import com.wb.logistics.ui.flightdeliveriesdetails.FlightDeliveriesDetailsParameters
import com.wb.logistics.ui.splash.NavToolbarTitleListener
import com.wb.logistics.ui.unloading.UnloadingScanParameters
import com.wb.logistics.ui.unloadingcongratulation.CongratulationParameters
import org.koin.androidx.viewmodel.ext.android.viewModel

class FlightDeliveriesFragment : Fragment() {

    private val viewModel by viewModel<FlightDeliveriesViewModel>()

    private var _binding: FlightDeliveriesFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DefaultAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: SmoothScroller

    companion object {
        private const val FLIGHT_DELIVERY_REQUEST_CODE = 100
        private const val FLIGHT_DELIVERY_TAG = "FLIGHT_DELIVERY_TAG"
    }

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
        binding.complete.setOnClickListener {
            viewModel.onCompleteClick()
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
                is FlightDeliveriesUINavState.NavigateToUpload -> {
                    findNavController().navigate(FlightDeliveriesFragmentDirections.actionFlightDeliveriesFragmentToUnloadingScanFragment(
                        UnloadingScanParameters(state.dstOfficeId, state.officeName)))
                }
                FlightDeliveriesUINavState.NavigateToCongratulation ->
                    findNavController().navigate(FlightDeliveriesFragmentDirections.actionFlightDeliveriesFragmentToCongratulationFragment(
                        CongratulationParameters()))
                is FlightDeliveriesUINavState.NavigateToDialogComplete -> showDialogReturnBalance(
                    state.description)
                is FlightDeliveriesUINavState.NavigateToUnloadDetails ->
                    findNavController().navigate(FlightDeliveriesFragmentDirections.actionFlightDeliveriesFragmentToFlightDeliveriesDetailsFragment(
                        FlightDeliveriesDetailsParameters(state.dstOfficeId, state.officeName)))
            }
        })

        viewModel.stateUIList.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FlightDeliveriesUIListState.ShowFlight -> {
                    updateBottom(state.isComplete)
                    displayItems(state.items)
                }
                is FlightDeliveriesUIListState.ProgressFlight -> {
                    updateBottom(state.isComplete)
                    displayItems(state.items)
                }
                is FlightDeliveriesUIListState.UpdateFlight -> {
                    updateBottom(state.isComplete)
                    displayItems(state.items)
                }
            }
        }

    }

    private fun updateToolbarLabel(toolbarTitle: String) {
        (activity as NavToolbarTitleListener).updateTitle(toolbarTitle)
    }

    private fun updateToolbarDeliveryIcon() {
        (activity as NavToolbarTitleListener).backButtonIcon(R.drawable.ic_fligt_delivery_transport_doc)
    }

    private fun updateBottom(isComplete: Boolean) {
        binding.complete.visibility = if (isComplete) VISIBLE else GONE
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

            addDelegate(FlightDeliveriesUnloadDelegate(requireContext(),
                object : OnFlightDeliveriesCallback {
                    override fun onPickToPointClick(idItem: Int) {
                        viewModel.onItemClicked(idItem)
                    }
                }))

            addDelegate(FlightDeliveriesNotUnloadDelegate(requireContext(),
                object : OnFlightDeliveriesCallback {
                    override fun onPickToPointClick(idItem: Int) {
                        viewModel.onItemClicked(idItem)
                    }
                }))

            addDelegate(
                FlightDeliveriesRefreshDelegate(
                    requireContext(),
                    object : OnFlightDeliveriesUpdateCallback {
                        override fun onUpdateRouteClick() {}
                    })
            )
            addDelegate(FlightDeliveriesProgressDelegate(requireContext()))
        }
        binding.recyclerView.adapter = adapter
    }

    private fun showDialogReturnBalance(description: String) {
        val dialog = SimpleResultDialogFragment.newInstance(
            getString(R.string.reception_return_dialog_title),
            description,
            getString(R.string.flight_deliveries_dialog_force_positive_button),
            getString(R.string.flight_deliveries_dialog_force_negative_button)
        )
        dialog.setTargetFragment(this, FLIGHT_DELIVERY_REQUEST_CODE)
        dialog.show(parentFragmentManager, FLIGHT_DELIVERY_TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == FLIGHT_DELIVERY_REQUEST_CODE) {
            viewModel.onCompleteConfirm()
        }
    }

}