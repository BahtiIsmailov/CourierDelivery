package com.wb.logistics.ui.unloading

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.wb.logistics.databinding.UnloadingBoxesFragmentBinding
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class UnloadingBoxesFragment : Fragment() {

    private val viewModel by viewModel<UnloadingBoxesViewModel> {
        parametersOf(requireArguments().getParcelable<UnloadingScanParameters>(UNLOADING_BOXES_KEY))
    }

    private var _binding: UnloadingBoxesFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UnloadingBoxesAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: RecyclerView.SmoothScroller

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = UnloadingBoxesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObserver()
        initListener()
    }

    private fun initBoxes(routeItems: MutableList<String>) {
        adapter = UnloadingBoxesAdapter(requireContext(), routeItems)
        binding.boxes.adapter = adapter
    }

    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.boxes.layoutManager = layoutManager
        binding.boxes.setHasFixedSize(true)
        initSmoothScroller()
    }

    private fun initSmoothScroller() {
        smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
    }

    private fun initObserver() {

        viewModel.boxesState.observe(viewLifecycleOwner) {
            when (it) {
                is UnloadingBoxesState.BoxesComplete -> initBoxes(it.boxes)
            }
        }

        viewModel.navigateToBack.observe(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
    }

    private fun initListener() {
        binding.complete.setOnClickListener { viewModel.onCompleteClick() }
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