package com.wb.logistics.ui.forcedtermination

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.databinding.ForcedTerminationFragmentBinding
import com.wb.logistics.ui.splash.NavToolbarListener
import kotlinx.android.parcel.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ForcedTerminationFragment : Fragment() {

    private val viewModel by viewModel<ForcedTerminationViewModel> {
        parametersOf(requireArguments().getParcelable<ForcedTerminationParameters>(
            FORCED_TERMINATION_KEY))
    }

    private var _binding: ForcedTerminationFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ForcedTerminationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        initListener()
    }

    private fun initTitleBoxes(title: String) {
        binding.boxesCount.text = title
    }

    private fun initBoxes(routeItems: List<ForcedTerminationItem>) {
        val chartLegendAdapter =
            ForcedTerminationAdapter(
                requireContext(),
                routeItems)
        binding.boxes.adapter = chartLegendAdapter
    }

    private fun initObserver() {

        viewModel.boxesState.observe(viewLifecycleOwner) {
            when (it) {
                is ForcedTerminationState.Title ->
                    (activity as NavToolbarListener).updateTitle(it.toolbarTitle)
                is ForcedTerminationState.BoxesComplete -> {
                    initTitleBoxes(it.title)
                    initBoxes(it.boxes)
                }
                ForcedTerminationState.BoxesEmpty -> TODO()
            }
        }

        viewModel.navigateToBack.observe(viewLifecycleOwner) {
            when (it) {
                ForcedTerminationNavAction.NavigateToBack -> findNavController().popBackStack()
                ForcedTerminationNavAction.NavigateToFlightDeliveries ->
                    findNavController().navigate(
                        ForcedTerminationFragmentDirections.actionForcedTerminationFragmentToFlightDeliveriesFragment())
            }
        }
    }

    private fun initListener() {
        binding.complete.setOnClickListener {
            val radioButtonID: Int = binding.radioGroup.checkedRadioButtonId
            val radioButton: View = binding.radioGroup.findViewById(radioButtonID)
            val idx: Int = binding.radioGroup.indexOfChild(radioButton)
            viewModel.onCompleteClick(idx)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val FORCED_TERMINATION_KEY = "forced_termination_key"
    }

}

@Parcelize
data class ForcedTerminationParameters(val dstOfficeId: Int) : Parcelable