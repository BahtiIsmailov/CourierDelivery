package com.wb.logistics.ui.unloading

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.databinding.UnloadingBoxesFragmentBinding
import com.wb.logistics.ui.splash.NavToolbarTitleListener
import kotlinx.android.parcel.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class UnloadingBoxesFragment : Fragment() {

    private val viewModel by viewModel<UnloadingBoxesViewModel> {
        parametersOf(requireArguments().getParcelable<UnloadingScanParameters>(UNLOADING_BOXES_KEY))
    }

    private var _binding: UnloadingBoxesFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = UnloadingBoxesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
    }

    private fun initBoxes(routeItems: List<String>) {
        val chartLegendAdapter = UnloadingBoxesAdapter(requireContext(), routeItems)
        binding.boxes.adapter = chartLegendAdapter
    }

    private fun initObserver() {

        viewModel.boxesState.observe(viewLifecycleOwner) {
            when (it) {
                is UnloadingBoxesState.Title ->
                    (activity as NavToolbarTitleListener).updateTitle(it.toolbarTitle)
                is UnloadingBoxesState.BoxesComplete -> initBoxes(it.boxes)
                UnloadingBoxesState.BoxesEmpty -> TODO()
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

    companion object {
        const val UNLOADING_BOXES_KEY = "unloading_boxes_key"
    }

}

@Parcelize
data class UnloadingBoxesParameters(val dstOfficeId: Int) : Parcelable