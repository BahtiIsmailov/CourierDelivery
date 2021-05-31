package com.wb.logistics.ui.flightsempty

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.databinding.FlightsEmptyFragmentBinding
import com.wb.logistics.ui.splash.NavToolbarTitleListener
import com.wb.logistics.ui.splash.OnFlightsCount
import org.koin.androidx.viewmodel.ext.android.viewModel


class FlightsEmptyFragment : Fragment() {

    private val viewModel by viewModel<FlightsEmptyViewModel>()

    private var _binding: FlightsEmptyFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var onFlightsCount: OnFlightsCount

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FlightsEmptyFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        initStateObserve()
        initDrawer()
    }

    private fun initListener() {
        binding.update.setOnClickListener { viewModel.onRefresh() }
    }

    private fun initStateObserve() {
        viewModel.stateUINav.observe(viewLifecycleOwner, { findNavController().popBackStack() })
        viewModel.stateUI.observe(viewLifecycleOwner, { showFlight(it.countFlight) })
    }

    private fun initDrawer() {
        (activity as NavToolbarTitleListener).updateDrawer()
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

}