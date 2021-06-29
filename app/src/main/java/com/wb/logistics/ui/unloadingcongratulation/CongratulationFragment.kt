package com.wb.logistics.ui.unloadingcongratulation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.databinding.UnloadingCongratulationFragmentBinding
import com.wb.logistics.ui.splash.NavToolbarListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class CongratulationFragment : Fragment() {

    private val viewModel by viewModel<CongratulationViewModel>()

    private var _binding: UnloadingCongratulationFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = UnloadingCongratulationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
        initListener()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
    }

    private fun initObserver() {

        viewModel.infoState.observe(viewLifecycleOwner) { binding.deliveredCount.text = it }

        viewModel.navigateToBack.observe(viewLifecycleOwner) {
            findNavController().navigate(CongratulationFragmentDirections.actionCongratulationFragmentToDcUnloadingScanFragment())
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