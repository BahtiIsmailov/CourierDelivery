package com.wb.logistics.ui.dcunloadingforcedtermination

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.databinding.DcForcedTerminationFragmentBinding
import com.wb.logistics.ui.dialogs.InformationDialogFragment
import com.wb.logistics.ui.splash.NavToolbarListener
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

        viewModel.navigateToMessageInfo.observe(viewLifecycleOwner) {
            InformationDialogFragment.newInstance(it.title, it.message, it.button)
                .show(parentFragmentManager, "INFO_MESSAGE_TAG")
        }

        viewModel.boxesState.observe(viewLifecycleOwner) {
            when (it) {
                is DcForcedTerminationState.Title ->
                    (activity as NavToolbarListener).updateTitle(it.toolbarTitle)
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
        binding.icDetails.setOnClickListener { viewModel.onDetailsClick() }

        binding.checkedBoxNotFound.setOnCheckedChangeListener { _, isChecked ->
            binding.complete.setState(if (isChecked) ProgressImageButtonMode.ENABLED else ProgressImageButtonMode.DISABLED)
        }
        binding.complete.setOnClickListener { viewModel.onCompleteClick() }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}