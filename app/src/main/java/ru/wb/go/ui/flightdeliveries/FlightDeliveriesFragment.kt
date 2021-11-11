package ru.wb.go.ui.flightdeliveries

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
import androidx.recyclerview.widget.RecyclerView.*
import ru.wb.go.R
import ru.wb.go.adapters.DefaultAdapterDelegate
import ru.wb.go.databinding.FlightDeliveriesFragmentBinding
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.dialogs.InformationDialogFragment
import ru.wb.go.ui.flightdeliveries.delegates.*
import ru.wb.go.ui.flightdeliveriesdetails.FlightDeliveriesDetailsParameters
import ru.wb.go.ui.splash.NavToolbarListener
import ru.wb.go.ui.unloadingscan.UnloadingScanParameters
import ru.wb.go.views.ProgressImageButtonMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class FlightDeliveriesFragment : Fragment() {

    private val viewModel by viewModel<FlightDeliveriesViewModel>()

    private var _binding: FlightDeliveriesFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DefaultAdapterDelegate
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
        initView()
        initRecyclerView()
        initAdapter()
        initListener()
        initStateObserve()
        viewModel.update()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        binding.toolbarLayout.back.visibility = View.INVISIBLE
    }

    private fun initListener() {
        binding.toolbarLayout.noInternetImage.setOnClickListener {
            (activity as NavToolbarListener).showNetworkDialog()
        }
        binding.completeDeliveryPositive.setOnClickListener {
            viewModel.onCompleteDeliveryPositiveClick()
        }
        binding.completeDeliveryNegative.setOnClickListener {
            viewModel.onCompleteDeliveryNegativeClick()
        }
    }

    private fun initStateObserve() {

        viewModel.navigateToMessageInfo.observe(viewLifecycleOwner) {
            InformationDialogFragment.newInstance(it.title, it.message, it.button)
                .show(parentFragmentManager, "INFO_MESSAGE_TAG")
        }

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
        title.text = getString(R.string.flight_deliveries_dialog_negative_title)
        message.text = description
        negative.setOnClickListener { alertDialog.dismiss() }
        negative.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        negative.text = getString(R.string.flight_deliveries_dialog_force_negative_button)
        positive.setOnClickListener {
            alertDialog.dismiss()
            viewModel.onCompleteConfirm()
        }
        positive.setTextColor(ContextCompat.getColor(requireContext(), R.color.accept))
        positive.text = getString(R.string.flight_deliveries_dialog_force_positive_button)
        alertDialog.show()
    }

}