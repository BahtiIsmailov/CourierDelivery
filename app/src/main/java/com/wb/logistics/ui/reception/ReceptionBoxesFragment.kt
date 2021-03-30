package com.wb.logistics.ui.reception

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.databinding.ReceptionBoxesFragmentBinding
import com.wb.logistics.views.ProgressImageButtonMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReceptionBoxesFragment : Fragment() {

    private val viewModel by viewModel<ReceptionBoxesViewModel>()

    private var _binding: ReceptionBoxesFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ReceptionBoxesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.boxes.observe(viewLifecycleOwner) {
            when (it) {
                is ReceptionBoxesUIState.ReceptionBoxesItem -> {
                    binding.emptyList.visibility = GONE
                    binding.boxes.visibility = VISIBLE
                    binding.remove.setState(ProgressImageButtonMode.DISABLED)
                    val adapter = ReceptionBoxesAdapter(
                        requireContext(), it.items
                    ) { index, isChecked -> viewModel.onItemClick(index, isChecked) }
                    binding.boxes.adapter = adapter
                }
                ReceptionBoxesUIState.Empty -> {
                    binding.emptyList.visibility = VISIBLE
                    binding.boxes.visibility = GONE
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

        binding.remove.setOnClickListener {
            viewModel.onRemoveClick()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}