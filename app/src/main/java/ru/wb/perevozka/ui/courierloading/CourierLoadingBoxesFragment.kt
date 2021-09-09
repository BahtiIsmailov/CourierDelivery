package ru.wb.perevozka.ui.courierloading

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
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.CourierLoadingBoxesFragmentBinding
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.dialogs.InformationDialogFragment
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.views.ProgressButtonMode
import ru.wb.perevozka.views.ProgressImageButtonMode

class CourierLoadingBoxesFragment : Fragment() {

    private val viewModel by viewModel<CourierLoadingBoxesViewModel>()

    private var _binding: CourierLoadingBoxesFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CourierLoadingBoxesAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: RecyclerView.SmoothScroller

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierLoadingBoxesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecyclerView()
        initObservable()
        initListener()
    }

    private fun initView() {
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.dc_loading_boxes_label)
    }

    private fun initObservable() {

        viewModel.navigateToMessage.observe(viewLifecycleOwner) {
            InformationDialogFragment.newInstance(it.title, it.message, it.button)
                .show(parentFragmentManager, "INFO_MESSAGE_TAG")
        }

        viewModel.toolbarNetworkState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Failed -> {
                    binding.toolbarLayout.noInternetImage.visibility = View.VISIBLE
                }

                is NetworkState.Complete -> {
                    binding.toolbarLayout.noInternetImage.visibility = View.INVISIBLE
                }
            }
        }

        viewModel.boxes.observe(viewLifecycleOwner) {
            when (it) {
                is CourierLoadingBoxesUIState.ReceptionBoxesItem -> {
                    binding.emptyList.visibility = GONE
                    binding.boxes.visibility = VISIBLE
                    binding.remove.setState(ProgressButtonMode.DISABLE)
                    val callback = object : CourierLoadingBoxesAdapter.OnItemClickCallBack {
                        override fun onItemClick(index: Int, isChecked: Boolean) {
                            viewModel.onItemClick(index, isChecked)
                        }
                    }
                    adapter = CourierLoadingBoxesAdapter(requireContext(), it.items, callback)
                    binding.boxes.adapter = adapter
                }
                is CourierLoadingBoxesUIState.ReceptionBoxItem -> {
                    adapter.setItem(it.index, it.item)
                    adapter.notifyItemChanged(it.index, it.item)
                }
                CourierLoadingBoxesUIState.Empty -> {
                    binding.emptyList.visibility = VISIBLE
                    binding.boxes.visibility = GONE
                    binding.remove.setState(ProgressButtonMode.DISABLE)
                }
                CourierLoadingBoxesUIState.Progress -> {
                    binding.overlayBoxes.visibility = VISIBLE
                    binding.remove.setState(ProgressButtonMode.PROGRESS)
                }

                CourierLoadingBoxesUIState.ProgressComplete -> {
                    binding.overlayBoxes.visibility = GONE
                    binding.remove.setState(ProgressButtonMode.DISABLE)
                }
            }
        }

        viewModel.navigateToBack.observe(viewLifecycleOwner) {
            findNavController().popBackStack()
        }

        viewModel.enableRemove.observe(viewLifecycleOwner) {
            binding.remove.setState(if (it) ProgressButtonMode.ENABLE else ProgressButtonMode.DISABLE)
        }
    }

    private fun initListener() {
        binding.toolbarLayout.back.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.toolbarLayout.noInternetImage.setOnClickListener {
            (activity as NavToolbarListener).showNetworkDialog()
        }
        binding.remove.setOnClickListener { viewModel.onRemoveClick() }
    }

    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.boxes.layoutManager = layoutManager
        binding.boxes.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )
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