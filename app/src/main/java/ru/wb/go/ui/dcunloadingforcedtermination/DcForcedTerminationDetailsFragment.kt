package ru.wb.go.ui.dcunloadingforcedtermination

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.wb.go.R
import ru.wb.go.databinding.DcForcedTerminationDetailsFragmentBinding
import ru.wb.go.ui.splash.NavToolbarListener
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
        initView()
        initObserver()
        initListener()
    }

    private fun initView() {
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.dc_unloading_forced_termination_details_label)
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
                    (activity as NavToolbarListener).updateTitle(it.toolbarTitle)
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
        binding.toolbarLayout.back.setOnClickListener { findNavController().popBackStack() }
        binding.complete.setOnClickListener { viewModel.onCompleteClick() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}