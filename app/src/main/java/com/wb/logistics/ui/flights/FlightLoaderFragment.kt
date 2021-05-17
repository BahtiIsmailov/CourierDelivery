package com.wb.logistics.ui.flights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.R
import com.wb.logistics.databinding.FlightLoaderFragmentBinding
import com.wb.logistics.ui.splash.NavToolbarTitleListener
import com.wb.logistics.ui.unloading.UnloadingScanParameters
import org.koin.androidx.viewmodel.ext.android.viewModel

class FlightLoaderFragment : Fragment(R.layout.flight_loader_fragment) {

    private var _binding: FlightLoaderFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<FlightLoaderViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FlightLoaderFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()

        (activity as NavToolbarTitleListener).hideBackButton()

//        val safeArgs: NumberPhoneFragmentArgs by navArgs()
//        val flowStepNumber = safeArgs.navigationFlowStep
//        if (flowStepNumber == 1) findNavController().navigate(R.id.navigationActivity)
    }

    private fun initObserver() {
        viewModel.navState.observe(viewLifecycleOwner) { state ->
            when (state) {

                FlightLoaderUINavState.NavigateToFlight -> findNavController().navigate(
                    FlightLoaderFragmentDirections.actionFlightLoaderFragmentToFlightsFragment())
                FlightLoaderUINavState.NavigateToReceptionScan -> {
                    findNavController().navigate(
                        FlightLoaderFragmentDirections.actionFlightLoaderFragmentToReceptionFragment())

//                    val deepLink = "perevozka://reception_scan".toUri()
//                    findNavController().navigate(deepLink)
                }
                FlightLoaderUINavState.NavigateToPickUpPoint -> findNavController().navigate(
                    FlightLoaderFragmentDirections.actionFlightLoaderFragmentToFlightPickPointFragment())
                FlightLoaderUINavState.NavigateToDelivery -> findNavController().navigate(
                    FlightLoaderFragmentDirections.actionFlightLoaderFragmentToFlightDeliveriesFragment())
                is FlightLoaderUINavState.NavigateToUnloading -> findNavController().navigate(
                    FlightLoaderFragmentDirections.actionFlightLoaderFragmentToUnloadingScanFragment(
                        UnloadingScanParameters(state.officeId, state.shortAddress)))
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
