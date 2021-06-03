package com.wb.logistics.ui.dcforcedtermination

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.databinding.DcForcedTerminationFragmentBinding
import com.wb.logistics.ui.splash.NavToolbarTitleListener
import com.wb.logistics.views.ProgressImageButtonMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class DcForcedTerminationFragment : Fragment() {

    private val viewModel by viewModel<DcForcedTerminationViewModel>()

    private var _binding: DcForcedTerminationFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DcForcedTerminationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        initListener()
    }

    private fun initTitleBoxes(count: String) {
        binding.boxesCount.text = count
    }

    private fun initObserver() {

        viewModel.boxesState.observe(viewLifecycleOwner) {
            when (it) {
                is DcForcedTerminationState.Title ->
                    (activity as NavToolbarTitleListener).updateTitle(it.toolbarTitle)
                is DcForcedTerminationState.BoxesUnloadCount -> initTitleBoxes(it.countBoxes)
            }
        }

        viewModel.navigateToBack.observe(viewLifecycleOwner) {
            when (it) {
                DcForcedTerminationNavAction.NavigateToDetails -> findNavController().navigate(
                    DcForcedTerminationFragmentDirections.actionDcForcedTerminationFragmentToDcForcedTerminationDetailsFragment())
                DcForcedTerminationNavAction.NavigateToCongratulation -> findNavController().navigate(
                    DcForcedTerminationFragmentDirections.actionDcForcedTerminationFragmentToDcUnloadingCongratulationFragment())
            }
        }

        viewModel.bottomProgressEvent.observe(viewLifecycleOwner) { progress ->
            binding.complete.setState(
                if (progress) ProgressImageButtonMode.PROGRESS else ProgressImageButtonMode.ENABLED)
        }

    }

    private fun initListener() {
        binding.radioGroup.clearCheck()

        binding.radioGroup.setOnCheckedChangeListener { _, _ ->
            binding.complete.setState(ProgressImageButtonMode.ENABLED)
        }

        binding.icDetails.setOnClickListener { viewModel.onDetailsClick() }

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

}