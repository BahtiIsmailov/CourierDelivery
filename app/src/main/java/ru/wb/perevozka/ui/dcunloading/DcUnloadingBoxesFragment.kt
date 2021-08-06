package ru.wb.perevozka.ui.dcunloading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.DcUnloadingBoxesFragmentBinding
import ru.wb.perevozka.ui.splash.NavToolbarListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class DcUnloadingBoxesFragment : Fragment() {

    private val viewModel by viewModel<DcUnloadingBoxesViewModel>()

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
        initView()
        initObserver()
        initListener()
    }

    private fun initListener() {
        binding.toolbarLayout.back.setOnClickListener { findNavController().popBackStack() }
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.unloading_boxes_label)
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
                DcUnloadingBoxesState.BoxesEmpty -> {
                }
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