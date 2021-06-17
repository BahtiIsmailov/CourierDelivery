package com.wb.logistics.ui.flightpickpoint

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.google.android.material.snackbar.Snackbar
import com.wb.logistics.R
import com.wb.logistics.adapters.DefaultAdapterDelegate
import com.wb.logistics.databinding.FlightPickPointFragmentBinding
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.dialogs.SimpleResultDialogFragment
import com.wb.logistics.ui.flightpickpoint.delegates.FlightPickPointDelegate
import com.wb.logistics.ui.splash.NavToolbarListener
import com.wb.logistics.views.ProgressImageButtonMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class FlightPickPointFragment : Fragment() {

    companion object {
        private const val GO_DELIVERY_REQUEST_CODE = 100
        private const val GO_DELIVERY_TAG = "START_DELIVERY_TAG"
    }

    private val viewModel by viewModel<FlightPickPointViewModel>()

    private var _binding: FlightPickPointFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DefaultAdapterDelegate
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: SmoothScroller

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FlightPickPointFragmentBinding.inflate(inflater, container, false)
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
            viewModel.action(FlightPickPointUIAction.GoToDeliveryClick)
        }
    }

    private fun initStateObserve() {

        viewModel.stateUIToolBar.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FlightPickPointUIToolbarState.Flight -> updateToolbarLabel(state.label)
                is FlightPickPointUIToolbarState.Delivery -> {
                    updateToolbarLabel(state.label)
                }
            }
        }

        viewModel.stateUINav.observe(viewLifecycleOwner, { state ->
            when (state) {
                FlightPickPointUINavState.ShowDeliveryDialog -> showDeliveryDialog()
                FlightPickPointUINavState.NavigateToDelivery -> {
                    findNavController().navigate(FlightPickPointFragmentDirections.actionFlightPickPointFragmentToFlightDeliveriesFragment())
                }
            }
        })

        viewModel.stateUIList.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FlightPickPointUIListState.ShowFlight -> {
                    updateStatus(state.receptionBox)
                    displayItems(state.items)
                }
                is FlightPickPointUIListState.ProgressFlight -> {
                    updateStatus(state.receptionBox)
                    displayItems(state.items)
                }
                is FlightPickPointUIListState.UpdateFlight -> {
                    updateStatus(state.receptionBox)
                    displayItems(state.items)
                }
            }
        }

        viewModel.bottomProgressEvent.observe(viewLifecycleOwner) { progress ->
            binding.goToDelivery.setState(
                if (progress) ProgressImageButtonMode.PROGRESS else ProgressImageButtonMode.ENABLED)
        }

        viewModel.stateUI.observe(viewLifecycleOwner, { state ->
            when (state) {
                is FlightPickPointUIState.Error -> {
                    showBarMessage(state.message)
                    binding.goToDelivery.setState(ProgressImageButtonMode.ENABLED)
                }
            }

        })

    }

    private fun showBarMessage(state: String) {
        Snackbar.make(binding.main, state, Snackbar.LENGTH_LONG).show()
    }

    private fun showDeliveryDialog() {
        val dialog = SimpleResultDialogFragment.newInstance(
            getString(R.string.flight_deliveries_dialog_title),
            getString(R.string.flight_deliveries_dialog_description),
            getString(R.string.flight_deliveries_dialog_positive_button),
            getString(R.string.flight_deliveries_dialog_negative_button),
            ContextCompat.getColor(requireContext(), R.color.accept),
            ContextCompat.getColor(requireContext(), R.color.primary)
        )
        dialog.setTargetFragment(this, GO_DELIVERY_REQUEST_CODE)
        dialog.show(parentFragmentManager, GO_DELIVERY_TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == GO_DELIVERY_REQUEST_CODE) {
            viewModel.action(FlightPickPointUIAction.GoToDeliveryConfirmClick)
        }
    }

    private fun updateToolbarLabel(toolbarTitle: String) {
        (activity as NavToolbarListener).updateTitle(toolbarTitle)
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
        adapter = with(DefaultAdapterDelegate()) {
            addDelegate(FlightPickPointDelegate(requireContext()))
        }
        binding.recyclerView.adapter = adapter
    }

}