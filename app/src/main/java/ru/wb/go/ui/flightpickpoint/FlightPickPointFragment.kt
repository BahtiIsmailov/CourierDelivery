package ru.wb.go.ui.flightpickpoint

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.google.android.material.snackbar.Snackbar
import ru.wb.go.R
import ru.wb.go.adapters.DefaultAdapterDelegate
import ru.wb.go.databinding.FlightPickPointFragmentBinding
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.flightpickpoint.delegates.FlightPickPointDelegate
import ru.wb.go.ui.splash.NavToolbarListener
import ru.wb.go.views.ProgressImageButtonMode
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
    }

    private fun initListener() {
        binding.toolbarLayout.back.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.toolbarLayout.noInternetImage.setOnClickListener {
            (activity as NavToolbarListener).showNetworkDialog()
        }
        binding.goToDelivery.setOnClickListener {
            viewModel.action(FlightPickPointUIAction.GoToDeliveryClick)
        }
    }

    private fun initStateObserve() {

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarTitle.text = it.label
        }

        viewModel.toolbarNetworkState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Failed -> {
                    binding.toolbarLayout.noInternetImage.visibility = View.VISIBLE
                }

                is NetworkState.Complete -> {
                    binding.toolbarLayout.noInternetImage.visibility = View.INVISIBLE
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
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val viewGroup: ViewGroup = binding.recyclerView
        val dialogView: View =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.custom_layout_dialog, viewGroup, false)
        val title: TextView = dialogView.findViewById(R.id.title)
        val message: TextView = dialogView.findViewById(R.id.message)
        val negative: Button = dialogView.findViewById(R.id.negative)
        val positive: Button = dialogView.findViewById(R.id.positive)

        builder.setView(dialogView)
        val alertDialog: AlertDialog = builder.create()
        title.text = getString(R.string.flight_deliveries_dialog_title)
        message.text = getString(R.string.flight_deliveries_dialog_description)
        negative.setOnClickListener { alertDialog.dismiss() }
        negative.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        negative.text = getString(R.string.flight_deliveries_dialog_negative_button)
        positive.setOnClickListener {
            alertDialog.dismiss()
            viewModel.action(FlightPickPointUIAction.GoToDeliveryConfirmClick)
        }
        positive.setTextColor(ContextCompat.getColor(requireContext(), R.color.accept))
        positive.text = getString(R.string.flight_deliveries_dialog_positive_button)
        alertDialog.show()
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