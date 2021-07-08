package com.wb.logistics.ui.flightserror

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.databinding.FlightsErrorFragmentBinding
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class FlightsErrorFragment : Fragment() {

    private val viewModel by viewModel<FlightsErrorViewModel> {
        parametersOf(requireArguments().getParcelable<FlightsErrorParameters>(
            FLIGHTS_ERROR_KEY))
    }

    private var _binding: FlightsErrorFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FlightsErrorFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        initStateObserve()
    }

    private fun initListener() {
        binding.update.setOnClickListener { viewModel.onRefresh() }
    }

    private fun initStateObserve() {
        viewModel.stateUINav.observe(viewLifecycleOwner, { findNavController().popBackStack() })
        viewModel.stateUI.observe(viewLifecycleOwner, { binding.message.text = it.message })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val FLIGHTS_ERROR_KEY = "flights_error_key"
    }

}

@Parcelize
data class FlightsErrorParameters(val message: String) : Parcelable