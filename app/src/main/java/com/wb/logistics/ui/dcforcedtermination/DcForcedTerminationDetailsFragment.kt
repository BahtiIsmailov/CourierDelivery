package com.wb.logistics.ui.dcforcedtermination

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.databinding.DcForcedTerminationDetailsFragmentBinding
import com.wb.logistics.ui.splash.NavToolbarTitleListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class DcForcedTerminationDetailsFragment : Fragment() {

    private val viewModel by viewModel<DcForcedTerminationDetailsViewModel>()

    private var _binding: DcForcedTerminationDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DcForcedTerminationDetailsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        initListener()
    }

    private fun initBoxes(routeItems: List<DcForcedTerminationDetailsItem>) {
        val chartLegendAdapter =
            DcForcedTerminationDetailsAdapter(
                requireContext(),
                routeItems)
        binding.boxes.adapter = chartLegendAdapter
    }

    private fun initObserver() {

        viewModel.boxesState.observe(viewLifecycleOwner) {
            when (it) {
                is DcForcedTerminationDetailsState.Title ->
                    (activity as NavToolbarTitleListener).updateTitle(it.toolbarTitle)
                is DcForcedTerminationDetailsState.BoxesComplete -> initBoxes(it.boxes)
            }
        }

        viewModel.navigateToBack.observe(viewLifecycleOwner) {
            when (it) {
                DcForcedTerminationDetailsNavAction.NavigateToBack -> findNavController().popBackStack()
            }
        }
    }

    private fun initListener() {
        binding.complete.setOnClickListener { viewModel.onCompleteClick() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}