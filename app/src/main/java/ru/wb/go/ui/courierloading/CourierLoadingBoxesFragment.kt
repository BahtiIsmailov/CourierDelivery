package ru.wb.go.ui.courierloading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.CourierLoadingBoxesFragmentBinding
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.splash.NavToolbarListener
import ru.wb.go.views.ProgressButtonMode

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

    private fun showDialogInfo(
        type: Int,
        title: String,
        message: String,
        positiveButtonName: String
    ) {
        DialogInfoFragment.newInstance(
            type = type,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName
        ).show(parentFragmentManager, DialogInfoFragment.DIALOG_INFO_TAG)
    }

    private fun initObservable() {

        viewModel.navigateToMessageInfo.observe(viewLifecycleOwner) {
            showDialogInfo(it.type, it.title, it.message, it.button)
        }

        viewModel.toolbarNetworkState.observe(viewLifecycleOwner) {
            val ic = when (it) {
                is NetworkState.Complete -> R.drawable.ic_inet_complete
                else -> R.drawable.ic_inet_failed
            }
            binding.toolbarLayout.noInternetImage.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), ic)
            )
        }

        viewModel.versionApp.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarVersion.text = it
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