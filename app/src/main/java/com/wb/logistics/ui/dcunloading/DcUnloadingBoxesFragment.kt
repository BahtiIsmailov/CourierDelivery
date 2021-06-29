package com.wb.logistics.ui.dcunloading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.databinding.DcUnloadingBoxesFragmentBinding
import com.wb.logistics.ui.splash.NavToolbarListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class DcUnloadingBoxesFragment : Fragment() {

    private val viewModel by viewModel<DcUnloadingBoxesViewModel> ()

    private var _binding: DcUnloadingBoxesFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DcUnloadingBoxesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
    }

    private fun initBoxes(routeItems: List<DcUnloadingBoxesItem>) {
        val chartLegendAdapter =
            DcUnloadingBoxesAdapter(
                requireContext(),
                routeItems)
        binding.boxes.adapter = chartLegendAdapter
    }

    private fun initObserver() {

        viewModel.boxesState.observe(viewLifecycleOwner) {
            when (it) {
                is DcUnloadingBoxesState.Title ->
                    (activity as NavToolbarListener).updateTitle(it.toolbarTitle)
                is DcUnloadingBoxesState.BoxesComplete -> initBoxes(it.boxes)
                DcUnloadingBoxesState.BoxesEmpty -> TODO()
            }
        }

        viewModel.navigateToBack.observe(viewLifecycleOwner) {
            findNavController().popBackStack()
        }

        binding.complete.setOnClickListener {
            viewModel.onCompleteClick()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}