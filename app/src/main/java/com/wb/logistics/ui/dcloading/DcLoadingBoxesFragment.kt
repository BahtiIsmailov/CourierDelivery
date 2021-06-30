package com.wb.logistics.ui.dcloading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.wb.logistics.databinding.DcLoadingBoxesFragmentBinding
import com.wb.logistics.ui.dialogs.InformationDialogFragment
import com.wb.logistics.views.ProgressImageButtonMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class DcLoadingBoxesFragment : Fragment() {

    private val viewModel by viewModel<DcLoadingBoxesViewModel>()

    private var _binding: DcLoadingBoxesFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DcLoadingBoxesAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: RecyclerView.SmoothScroller


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DcLoadingBoxesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObservable()
        initListener()
    }

    private fun initObservable() {
        viewModel.navigateToMessage.observe(viewLifecycleOwner) {
            InformationDialogFragment.newInstance(it.title, it.message, it.button)
                .show(parentFragmentManager, "INFO_MESSAGE_TAG")
        }

        viewModel.boxes.observe(viewLifecycleOwner) {
            when (it) {
                is DcLoadingBoxesUIState.ReceptionBoxesItem -> {
                    binding.emptyList.visibility = GONE
                    binding.boxes.visibility = VISIBLE
                    binding.remove.setState(ProgressImageButtonMode.DISABLED)
                    val callback = object : DcLoadingBoxesAdapter.OnItemClickCallBack {
                        override fun onItemClick(index: Int, isChecked: Boolean) {
                            viewModel.onItemClick(index, isChecked)
                        }
                    }
                    adapter = DcLoadingBoxesAdapter(requireContext(), it.items, callback)
                    binding.boxes.adapter = adapter
                }
                is DcLoadingBoxesUIState.ReceptionBoxItem -> {
                    adapter.setItem(it.index, it.item)
                    adapter.notifyItemChanged(it.index, it.item)
                }
                DcLoadingBoxesUIState.Empty -> {
                    binding.emptyList.visibility = VISIBLE
                    binding.boxes.visibility = GONE
                    binding.remove.setState(ProgressImageButtonMode.DISABLED)
                }
                DcLoadingBoxesUIState.Progress -> {
                    binding.overlayBoxes.visibility = VISIBLE
                    binding.remove.setState(ProgressImageButtonMode.PROGRESS)
                }

                DcLoadingBoxesUIState.ProgressComplete -> {
                    binding.overlayBoxes.visibility = GONE
                    binding.remove.setState(ProgressImageButtonMode.DISABLED)
                }
            }
        }

        viewModel.navigateToBack.observe(viewLifecycleOwner) {
            findNavController().popBackStack()
        }

        viewModel.enableRemove.observe(viewLifecycleOwner) {
            binding.remove.setState(if (it) ProgressImageButtonMode.ENABLED else ProgressImageButtonMode.DISABLED)
        }
    }

    private fun initListener() {
        binding.remove.setOnClickListener {
            viewModel.onRemoveClick()
        }
    }

    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.boxes.layoutManager = layoutManager
        binding.boxes.addItemDecoration(DividerItemDecoration(activity,
            DividerItemDecoration.VERTICAL))
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}