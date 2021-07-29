package com.wb.logistics.ui.flightsempty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.R
import com.wb.logistics.databinding.FlightsEmptyFragmentBinding
import com.wb.logistics.network.monitor.NetworkState
import com.wb.logistics.ui.splash.NavToolbarListener
import org.koin.androidx.viewmodel.ext.android.viewModel


class FlightsEmptyFragment : Fragment() {

    private val viewModel by viewModel<FlightsEmptyViewModel>()

    private var _binding: FlightsEmptyFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FlightsEmptyFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        initStateObserve()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.flights_label)
        binding.toolbarLayout.back.visibility = INVISIBLE
    }

    private fun initListener() {
        binding.toolbarLayout.noInternetImage.setOnClickListener {
            (activity as NavToolbarListener).showNetworkDialog()
        }
        binding.update.setOnClickListener { viewModel.onRefresh() }
    }

    private fun initStateObserve() {
        viewModel.stateUINav.observe(viewLifecycleOwner, { findNavController().popBackStack() })

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}