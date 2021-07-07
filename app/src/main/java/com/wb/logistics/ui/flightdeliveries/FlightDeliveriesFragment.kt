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
import com.wb.logistics.adapters.DefaultAdapterDelegate
import com.wb.logistics.databinding.FlightDeliveriesFragmentBinding
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.dialogs.InformationDialogFragment
import com.wb.logistics.ui.dialogs.SimpleResultDialogFragment
import com.wb.logistics.ui.flightdeliveries.delegates.*
import com.wb.logistics.ui.flightdeliveriesdetails.FlightDeliveriesDetailsParameters
import com.wb.logistics.ui.splash.NavToolbarListener
import com.wb.logistics.ui.unloading.UnloadingScanParameters
import com.wb.logistics.views.ProgressImageButtonMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class FlightDeliveriesFragment : Fragment() {

    private val viewModel by viewModel<FlightDeliveriesViewModel>()

    private var _binding: FlightDeliveriesFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DefaultAdapterDelegate
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
        binding.completeDeliveryPositive.setOnClickListener {
            viewModel.onCompleteDeliveryPositiveClick()
        }
        binding.completeDeliveryNegative.setOnClickListener {
            viewModel.onCompleteDeliveryNegativeClick()
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
                        UnloadingScanParameters(state.dstOfficeId)))
                }
                FlightDeliveriesUINavState.NavigateToCongratulation -> {
                    findNavController().navigate(FlightDeliveriesFragmentDirections.actionFlightDeliveriesFragmentToCongratulationFragment())
                }
                is FlightDeliveriesUINavState.NavigateToDialogComplete ->
                    showDialogReturnBalance(state.description)
                is FlightDeliveriesUINavState.NavigateToUnloadDetails ->
                    findNavController().navigate(FlightDeliveriesFragmentDirections.actionFlightDeliveriesFragmentToFlightDeliveriesDetailsFragment(
                        FlightDeliveriesDetailsParameters(state.dstOfficeId, state.officeName)))
            }
        })

        viewModel.stateUIList.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FlightDeliveriesUIListState.ShowFlight -> {
                    updateBottom(state.bottomState)
                    displayItems(state.items)
                }
                is FlightDeliveriesUIListState.ProgressFlight -> {
                    updateBottom(state.bottomState)
                    displayItems(state.items)
                }
                is FlightDeliveriesUIListState.UpdateFlight -> {
                    updateBottom(state.bottomState)
                    displayItems(state.items)
                }
            }
        }

        viewModel.stateUIProgress.observe(viewLifecycleOwner) { state ->
            when (state) {
                FlightDeliveriesUIProgressState.CompleteDeliveryNormal -> {
                    binding.overlayBoxes.visibility = GONE
                    binding.completeDeliveryNegative.setState(ProgressImageButtonMode.ENABLED)
                    binding.completeDeliveryPositive.setState(ProgressImageButtonMode.ENABLED)
                }
                FlightDeliveriesUIProgressState.CompleteNegativeDeliveryProgress -> {
                    binding.overlayBoxes.visibility = VISIBLE
                    binding.completeDeliveryNegative.setState(ProgressImageButtonMode.PROGRESS)
                }
                FlightDeliveriesUIProgressState.CompletePositiveDeliveryProgress -> {
                    binding.overlayBoxes.visibility = VISIBLE
                    binding.completeDeliveryPositive.setState(ProgressImageButtonMode.PROGRESS)
                }
            }
        }

        viewModel.navigateToMessageInfo.observe(viewLifecycleOwner) {
            InformationDialogFragment.newInstance(it.title, it.message, it.button)
                .show(parentFragmentManager, "INFO_MESSAGE_TAG")
        }

    }

    private fun updateToolbarLabel(toolbarTitle: String) {
        (activity as NavToolbarListener).updateTitle(toolbarTitle)
    }

    private fun updateToolbarDeliveryIcon() {
        // TODO: 03.06.2021 реализовать ТТН
//        (activity as NavToolbarTitleListener).backButtonIcon(R.drawable.ic_fligt_delivery_transport_doc)
        (activity as NavToolbarListener).hideBackButton()
    }

    private fun updateBottom(state: FlightDeliveriesUIBottomState) {
        when (state) {
            FlightDeliveriesUIBottomState.ShowCompleteNegativeDelivery -> {
                binding.completeDeliveryNegative.visibility = VISIBLE
                binding.completeDeliveryPositive.visibility = GONE
            }
            FlightDeliveriesUIBottomState.ShowCompletePositiveDelivery -> {
                binding.completeDeliveryNegative.visibility = GONE
                binding.completeDeliveryPositive.visibility = VISIBLE
            }
            FlightDeliveriesUIBottomState.Empty -> {
                binding.completeDeliveryNegative.visibility = GONE
                binding.completeDeliveryPositive.visibility = GONE
            }
        }
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
        adapter = with(DefaultAdapterDelegate()) {
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
            addDelegate(FlightDeliveriesProgressDelegate(requireContext()))
        }
        binding.recyclerView.adapter = adapter
    }

    private fun showDialogReturnBalance(description: String) {
        val dialog = SimpleResultDialogFragment.newInstance(
            getString(R.string.flight_deliveries_dialog_negative_title),
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