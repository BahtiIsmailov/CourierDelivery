package com.wb.logistics.ui.dcunloadingcongratulation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.databinding.DcUnloadingCongratulationFragmentBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class DcUnloadingCongratulationFragment : Fragment() {

    private val viewModel by viewModel<DcUnloadingCongratulationViewModel>()

    private var _binding: DcUnloadingCongratulationFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DcUnloadingCongratulationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        initListener()
    }

    private fun initObserver() {
        viewModel.infoState.observe(viewLifecycleOwner) {
            binding.deliveredCount.text = it.deliveredCount
            binding.returnedCount.text = it.returnCount
        }

        viewModel.navigateToBack.observe(viewLifecycleOwner) {
            findNavController().navigate(DcUnloadingCongratulationFragmentDirections.actionDcUnloadingCongratulationFragmentToFlightLoaderFragment())
        }
    }

    private fun initListener() {
        binding.complete.setOnClickListener {
            viewModel.onCompleteClick()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}