package com.wb.logistics.ui.flightdeliveriesdetails

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.wb.logistics.adapters.DefaultAdapterDelegate
import com.wb.logistics.databinding.FlightDeliveriesDetailsFragmentBinding
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.flightdeliveriesdetails.delegates.FlightDeliveriesDetailsDelegate
import com.wb.logistics.ui.flightdeliveriesdetails.delegates.FlightDeliveriesDetailsTitleDelegate
import com.wb.logistics.ui.splash.NavToolbarListener
import com.wb.logistics.ui.unloading.UnloadingScanParameters
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class FlightDeliveriesDetailsFragment : Fragment() {

    private val viewModel by viewModel<FlightDeliveriesDetailsViewModel> {
        parametersOf(requireArguments().getParcelable<FlightDeliveriesDetailsParameters>(
            FLIGHT_DELIVERY_DETAILS_KEY))
    }
    private var _binding: FlightDeliveriesDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DefaultAdapterDelegate
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: RecyclerView.SmoothScroller

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FlightDeliveriesDetailsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initAdapter()
        initObserver()
        initListener()
    }

    private fun initListener() {
        binding.proceedBoxes.setOnClickListener {
            viewModel.onCompleteClick()
        }
    }

    private fun displayItems(items: List<BaseItem>) {
        adapter.clear()
        adapter.addItems(items)
        adapter.notifyDataSetChanged()
    }

    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.details.layoutManager = layoutManager
        binding.details.setHasFixedSize(true)
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
            addDelegate(FlightDeliveriesDetailsTitleDelegate(requireContext()))
            addDelegate(FlightDeliveriesDetailsDelegate(requireContext()))
        }
        binding.details.adapter = adapter
    }

    private fun initObserver() {

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            (activity as NavToolbarListener).updateTitle(it.label)
        }

        viewModel.itemsState.observe(viewLifecycleOwner) {
            when (it) {
                is FlightDeliveriesDetailsItemsState.Items -> displayItems(it.items)
            }
        }

        viewModel.stateUINav.observe(viewLifecycleOwner, { state ->
            when (state) {
                is FlightDeliveriesDetailsUINavState.NavigateToUpload -> {
                    findNavController().navigate(FlightDeliveriesDetailsFragmentDirections.actionFlightDeliveriesDetailsFragmentToUnloadingScanFragment(
                        UnloadingScanParameters(state.currentOfficeId)))
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val FLIGHT_DELIVERY_DETAILS_KEY = "flight_deliveries_details_key"
    }

}

@Parcelize
data class FlightDeliveriesDetailsParameters(val currentOfficeId: Int, val shortAddress: String) :
    Parcelable