package com.wb.logistics.ui.flights

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.wb.logistics.R
import com.wb.logistics.adapters.DefaultAdapter
import com.wb.logistics.databinding.FlightsFragmentBinding
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.flights.delegates.FlightsDelegate
import com.wb.logistics.ui.flights.delegates.FlightsProgressDelegate
import com.wb.logistics.ui.flights.delegates.FlightsRefreshDelegate
import com.wb.logistics.ui.flights.delegates.OnFlightsUpdateCallback
import org.koin.androidx.viewmodel.ext.android.viewModel


class FlightsFragment : Fragment() {

    interface OnFlightsCount {
        fun flightCount(count: String)
    }

    private val viewModel by viewModel<FlightsViewModel>()

    private var _binding: FlightsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DefaultAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: SmoothScroller
    private lateinit var onFlightsCount: OnFlightsCount

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FlightsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initAdapter()
        initListener()
        initStateObserve()
    }

    private fun initListener() {
        binding.returnBalance.setOnClickListener {

        }
        binding.continueAcceptance.setOnClickListener {

        }
        binding.startAddingBoxes.setOnClickListener {
            viewModel.action(FlightsPasswordUIAction.ReceptionBoxesClick)
        }
    }

    private fun initStateObserve() {
        viewModel.stateUI.observe(viewLifecycleOwner, { state ->
            when (state) {
                FlightsPasswordUIState.Empty -> {
                }
                FlightsPasswordUIState.NavigateToNetworkInfoDialog -> {
                }
                FlightsPasswordUIState.NavigateToReceptionBox -> findNavController().navigate(R.id.receptionFragment)
                FlightsPasswordUIState.NavigateToReturnBalanceDialog -> {
                }
                is FlightsPasswordUIState.ShowFlight -> {
                    displayItems(state.items)
                    visibleStartAddingBoxes()
                    showFlight(state.countFlight)
                }
                is FlightsPasswordUIState.ProgressFlight -> {
                    displayItems(state.items)
                    goneStartAddingBoxes()
                    showFlight(state.countFlight)
                }
                is FlightsPasswordUIState.UpdateFlight -> {
                    displayItems(state.items)
                    goneStartAddingBoxes()
                    showFlight(state.countFlight)
                }
            }
        })
    }

    private fun showFlight(countFlight: String) {
        onFlightsCount.flightCount(countFlight)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onFlightsCount = context as OnFlightsCount
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
            addDelegate(FlightsDelegate(requireContext()))
            addDelegate(
                FlightsRefreshDelegate(
                    requireContext(),
                    object : OnFlightsUpdateCallback {
                        override fun onUpdateRouteClick() {
                            viewModel.action(FlightsPasswordUIAction.Refresh)
                        }
                    })
            )
            addDelegate(FlightsProgressDelegate(requireContext()))
        }
        binding.recyclerView.adapter = adapter
    }

    private fun visibleStartAddingBoxes() {
        binding.startAddingBoxes.visibility = View.VISIBLE
    }

    private fun goneStartAddingBoxes() {
        binding.startAddingBoxes.visibility = View.GONE
    }

}