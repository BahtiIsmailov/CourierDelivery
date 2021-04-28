package com.wb.logistics.ui.unloading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.databinding.UnloadingReturnBoxesFragmentBinding
import com.wb.logistics.ui.dialogs.InformationDialogFragment
import com.wb.logistics.ui.nav.NavToolbarTitleListener
import com.wb.logistics.views.ProgressImageButtonMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class UnloadingReturnBoxesFragment : Fragment() {

    private val viewModel by viewModel<UnloadingReturnBoxesViewModel>()

    private var _binding: UnloadingReturnBoxesFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = UnloadingReturnBoxesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigateToMessage.observe(viewLifecycleOwner) {
            val dialog = InformationDialogFragment.newInstance(
                "Log remove boxing",
                it.message,
                "Ok"
            )
            dialog.setTargetFragment(this, 10101)
            dialog.show(parentFragmentManager, "START_DELIVERY_TAG")

            (activity as NavToolbarTitleListener).updateTitle("Возврат")
        }

        viewModel.boxes.observe(viewLifecycleOwner) {
            when (it) {
                is UnloadingReturnBoxesUIState.ReceptionBoxesItem -> {
                    binding.emptyList.visibility = GONE
                    binding.boxes.visibility = VISIBLE
                    binding.remove.setState(ProgressImageButtonMode.DISABLED)
                    val adapter =
                        UnloadingReturnBoxesAdapter(
                            requireContext(), it.items
                        ) { index, isChecked -> viewModel.onItemClick(index, isChecked) }
                    binding.boxes.adapter = adapter
                }
                UnloadingReturnBoxesUIState.Empty -> {
                    binding.emptyList.visibility = VISIBLE
                    binding.boxes.visibility = GONE
                    binding.remove.setState(ProgressImageButtonMode.DISABLED)
                }
                UnloadingReturnBoxesUIState.Progress ->
                    binding.remove.setState(ProgressImageButtonMode.PROGRESS)
                UnloadingReturnBoxesUIState.ProgressComplete ->
                    binding.remove.setState(ProgressImageButtonMode.DISABLED)
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