package com.wb.logistics.ui.unloading

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.R
import com.wb.logistics.databinding.UnloadingReturnBoxesFragmentBinding
import com.wb.logistics.ui.dialogs.InformationDialogFragment
import com.wb.logistics.ui.dialogs.SimpleResultDialogFragment
import com.wb.logistics.ui.splash.NavToolbarListener
import com.wb.logistics.views.ProgressImageButtonMode
import kotlinx.android.parcel.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class UnloadingReturnBoxesFragment : Fragment() {

    private val viewModel by viewModel<UnloadingReturnBoxesViewModel> {
        parametersOf(requireArguments().getParcelable<UnloadingReturnParameters>(
            UNLOADING_RETURN_KEY))
    }

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
        initObserver()
        initListener()
    }

    private fun initListener() {
        binding.remove.setOnClickListener { showDialogReturnBalance() }
    }

    private fun initObserver() {

        // TODO: 29.04.2021 отладочный код
        viewModel.navigateToMessage.observe(viewLifecycleOwner) {
            val dialog = InformationDialogFragment.newInstance(
                "Log remove boxing",
                it.message,
                "Ok"
            )
            dialog.setTargetFragment(this, 10101)
            dialog.show(parentFragmentManager, "START_DELIVERY_TAG")

            (activity as NavToolbarListener).updateTitle("Возврат")
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
    }

    private fun showDialogReturnBalance() {
        val dialog = SimpleResultDialogFragment.newInstance(
            getString(R.string.unloading_return_dialog_title),
            getString(R.string.unloading_return_dialog_description),
            getString(R.string.unloading_return_dialog_positive_button),
            getString(R.string.unloading_return_dialog_negative_button)
        )
        dialog.setTargetFragment(this, UNLOADING_RETURN_REQUEST_CODE)
        dialog.show(parentFragmentManager, UNLOADING_RETURN_TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == UNLOADING_RETURN_REQUEST_CODE) {
            viewModel.onRemoveClick()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val UNLOADING_RETURN_KEY = "unloading_return_key"
        private const val UNLOADING_RETURN_REQUEST_CODE = 100
        private const val UNLOADING_RETURN_TAG = "UNLOADING_RETURN_TAG"
    }

}

@Parcelize
data class UnloadingReturnParameters(val dstOfficeId: Int) : Parcelable