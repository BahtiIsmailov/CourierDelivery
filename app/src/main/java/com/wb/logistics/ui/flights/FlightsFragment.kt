package com.wb.logistics.ui.flights

import android.app.Activity.RESULT_OK
import android.content.Context
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
import com.wb.logistics.databinding.FlightsFragmentBinding
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.dialogs.SimpleResultDialogFragment
import com.wb.logistics.ui.flights.delegates.FlightsDelegate
import com.wb.logistics.ui.flights.delegates.FlightsProgressDelegate
import com.wb.logistics.ui.flights.delegates.FlightsRefreshDelegate
import com.wb.logistics.ui.flights.delegates.OnFlightsUpdateCallback
import com.wb.logistics.ui.splash.NavToolbarTitleListener
import org.koin.androidx.viewmodel.ext.android.viewModel


class FlightsFragment : Fragment() {

    interface OnFlightsCount {
        fun flightCount(count: String)
    }

    companion object {
        private const val RETURN_BALANCE_REQUEST_CODE = 100
        private const val RETURN_BALANCE_TAG = "RETURN_BALANCE_TAG"
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
        savedInstanceState: Bundle?,
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
        initDrawer()
        viewModel.update()
    }

    private fun initListener() {
        binding.returnBalance.setOnClickListener {
            showDialogReturnBalance()
        }
        binding.continueAcceptance.setOnClickListener {
            viewModel.action(FlightsUIAction.ReceptionBoxesClick)
        }
        binding.scanBoxes.setOnClickListener {
            viewModel.action(FlightsUIAction.ReceptionBoxesClick)
        }
    }

    private fun initStateObserve() {
        viewModel.stateUINav.observe(viewLifecycleOwner, { state ->
            when (state) {
                FlightsUINavState.Empty -> {
                }
                FlightsUINavState.NavigateToNetworkInfoDialog -> {
                }
                FlightsUINavState.NavigateToReceptionBox -> findNavController().navigate(R.id.receptionFragment)
                FlightsUINavState.NavigateToReturnBalanceDialog -> {
                }
            }
        })

        viewModel.stateUIList.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FlightsUIListState.ShowFlight -> {
                    displayItems(state.items)
                    showFlight(state.countFlight)
                }
                is FlightsUIListState.ProgressFlight -> {
                    displayItems(state.items)
                    goneStartAddingBoxes()
                    showFlight(state.countFlight)
                }
                is FlightsUIListState.UpdateFlight -> {
                    displayItems(state.items)
                    goneStartAddingBoxes()
                    showFlight(state.countFlight)
                }
            }
        }

        viewModel.stateUIBottom.observe(viewLifecycleOwner) { state ->
            when (state) {
                FlightsUIBottomState.ReturnBox -> {
                    binding.returnGroup.visibility = VISIBLE
                    binding.scanBoxes.visibility = INVISIBLE
                }
                FlightsUIBottomState.ScanBox -> {
                    binding.returnGroup.visibility = INVISIBLE
                    binding.scanBoxes.visibility = VISIBLE
                }
            }
        }

    }

    private fun initDrawer() {
        (activity as NavToolbarTitleListener).updateDrawer()
    }

    private fun showDialogReturnBalance() {
        val dialog = SimpleResultDialogFragment.newInstance(
            getString(R.string.reception_return_dialog_title),
            getString(R.string.reception_return_dialog_description),
            getString(R.string.reception_return_dialog_positive_button),
            getString(R.string.reception_return_dialog_negative_button)
        )
        dialog.setTargetFragment(this, RETURN_BALANCE_REQUEST_CODE)
        dialog.show(parentFragmentManager, RETURN_BALANCE_TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == RETURN_BALANCE_REQUEST_CODE) {
            viewModel.action(FlightsUIAction.RemoveBoxesClick)
        }
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
                            viewModel.action(FlightsUIAction.Refresh)
                        }
                    })
            )
            addDelegate(FlightsProgressDelegate(requireContext()))
        }
        binding.recyclerView.adapter = adapter
    }

    private fun goneStartAddingBoxes() {
        binding.scanBoxes.visibility = View.GONE
        binding.returnGroup.visibility = View.GONE
    }

}