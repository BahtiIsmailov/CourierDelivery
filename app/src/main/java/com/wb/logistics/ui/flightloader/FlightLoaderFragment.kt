package com.wb.logistics.ui.flightloader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.R
import com.wb.logistics.databinding.FlightLoaderFragmentBinding
import com.wb.logistics.ui.splash.NavToolbarTitleListener
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
        viewModel.update()
    }

    private fun initObserver() {
        viewModel.navState.observe(viewLifecycleOwner) { findNavController().navigate(it.navDirections) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}