package com.wb.logistics.ui.unloadingreturnboxes

import android.app.AlertDialog
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.wb.logistics.R
import com.wb.logistics.databinding.UnloadingReturnBoxesFragmentBinding
import com.wb.logistics.ui.dialogs.InformationDialogFragment
import com.wb.logistics.views.ProgressImageButtonMode
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class UnloadingReturnBoxesFragment : Fragment() {

    private val viewModel by viewModel<UnloadingReturnBoxesViewModel> {
        parametersOf(requireArguments().getParcelable<UnloadingReturnParameters>(
            UNLOADING_RETURN_KEY))
    }

    private var _binding: UnloadingReturnBoxesFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UnloadingReturnBoxesAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: RecyclerView.SmoothScroller

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = UnloadingReturnBoxesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecyclerView()
        initObserver()
        initListener()
    }

    private fun initView() {
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.unloading_return_boxes_label)
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

    private fun initListener() {
        binding.toolbarLayout.back.setOnClickListener { findNavController().popBackStack() }
        binding.remove.setOnClickListener { showDialogReturnBalance() }
    }

    private fun initObserver() {

        viewModel.navigateToMessage.observe(viewLifecycleOwner) {
            InformationDialogFragment.newInstance(it.title, it.message, it.button)
                .show(parentFragmentManager, "INFO_MESSAGE_TAG")
        }

        viewModel.boxes.observe(viewLifecycleOwner) {
            when (it) {
                is UnloadingReturnBoxesUIState.ReceptionBoxesItem -> {
                    binding.emptyList.visibility = GONE
                    binding.boxes.visibility = VISIBLE
                    binding.remove.setState(ProgressImageButtonMode.DISABLED)
                    val callback = object : UnloadingReturnBoxesAdapter.OnItemClickCallBack {
                        override fun onItemClick(index: Int, isChecked: Boolean) {
                            viewModel.onItemClick(index, isChecked)
                        }
                    }
                    adapter = UnloadingReturnBoxesAdapter(requireContext(), it.items, callback)
                    binding.boxes.adapter = adapter
                }
                UnloadingReturnBoxesUIState.Empty -> {
                    binding.emptyList.visibility = VISIBLE
                    binding.boxes.visibility = GONE
                    binding.remove.setState(ProgressImageButtonMode.DISABLED)
                }
                UnloadingReturnBoxesUIState.Progress -> {
                    binding.overlayBoxes.visibility = VISIBLE
                    binding.remove.setState(ProgressImageButtonMode.PROGRESS)
                }
                UnloadingReturnBoxesUIState.ProgressComplete -> {
                    binding.overlayBoxes.visibility = GONE
                    binding.remove.setState(ProgressImageButtonMode.DISABLED)
                }
                is UnloadingReturnBoxesUIState.ReceptionBoxItem -> {
                    adapter.setItem(it.index, it.item)
                    adapter.notifyItemChanged(it.index, it.item)
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

    private fun showDialogReturnBalance() {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val viewGroup = binding.loginLayout
        val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.custom_layout_dialog, viewGroup, false)
        val title: TextView = dialogView.findViewById(R.id.title)
        val message: TextView = dialogView.findViewById(R.id.message)
        val negative: Button = dialogView.findViewById(R.id.negative)
        val positive: Button = dialogView.findViewById(R.id.positive)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        title.text = getString(R.string.unloading_return_dialog_title)
        message.text = getString(R.string.unloading_return_dialog_description)
        negative.setOnClickListener { alertDialog.dismiss() }
        negative.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        negative.text = getString(R.string.unloading_return_dialog_negative_button)
        positive.setOnClickListener {
            alertDialog.dismiss()
            viewModel.onRemoveClick()
        }
        positive.setTextColor(ContextCompat.getColor(requireContext(), R.color.accept))
        positive.text = getString(R.string.unloading_return_dialog_positive_button)
        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val UNLOADING_RETURN_KEY = "unloading_return_key"
    }

}

@Parcelize
data class UnloadingReturnParameters(val dstOfficeId: Int) : Parcelable