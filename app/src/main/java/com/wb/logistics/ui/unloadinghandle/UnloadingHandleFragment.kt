package com.wb.logistics.ui.unloadinghandle

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding3.widget.textChanges
import com.wb.logistics.databinding.UnloadingHandleFragmentBinding
import com.wb.logistics.ui.splash.KeyboardListener
import com.wb.logistics.ui.splash.NavToolbarListener
import com.wb.logistics.utils.SoftKeyboard
import com.wb.logistics.views.ProgressImageButtonMode
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class UnloadingHandleFragment : Fragment() {

    private val viewModel by viewModel<UnloadingHandleViewModel> {
        parametersOf(requireArguments().getParcelable<UnloadingHandleParameters>(
            UNLOADING_HANDLE_KEY))
    }

    private var _binding: UnloadingHandleFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(): UnloadingHandleFragment {
            return UnloadingHandleFragment()
        }

        const val UNLOADING_HANDLE_BARCODE_RESULT = "UNLOADING_HANDLE_BARCODE_RESULT"
        const val HANDLE_BARCODE_COMPLETE_KEY = "HANDLE_BARCODE_COMPLETE_KEY"

        const val UNLOADING_HANDLE_KEY = "unloading_handle_key"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = UnloadingHandleFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        initStateObserve()
        initKeyboard()
    }

    private fun initView() {
        //(activity as NavToolbarListener).backButtonIcon(R.drawable.ic_close_dialog)
        (activity as NavToolbarListener).hideToolbar()
        (activity as KeyboardListener).panMode()
    }

    private fun initBoxes(routeItems: List<String>) {
        val chartLegendAdapter = UnloadingHandleAdapter(requireContext(), routeItems)
        binding.boxes.adapter = chartLegendAdapter
    }

    private fun initStateObserve() {
        viewModel.stateUI.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UnloadingHandleUIState.BoxFormatted -> {
                    binding.accept.setState(ProgressImageButtonMode.ENABLED)
                    setFormatCodeBox(state.number)
                }
                is UnloadingHandleUIState.BoxAcceptDisabled -> {
                    setFormatCodeBox(state.number)
                    binding.accept.setState(ProgressImageButtonMode.DISABLED)
                }
                is UnloadingHandleUIState.BoxesComplete -> {
                    initBoxes(state.boxes)
                    binding.listEmpty.visibility = GONE
                }
                UnloadingHandleUIState.BoxesEmpty -> {
                    binding.listEmpty.visibility = VISIBLE
                }
            }
        }
    }

    private fun initKeyboard() {
        activity?.let { SoftKeyboard.showKeyboard(it, binding.codeBox) }
    }

    private fun setFormatCodeBox(number: String) {
        binding.codeBox.setText(number)
        binding.codeBox.setSelection(number.length)
    }

    private fun initListener() {
        binding.close.setOnClickListener { findNavController().navigateUp() }
        viewModel.action(UnloadingHandleUIAction.BoxChanges(binding.codeBox.textChanges()))
        binding.accept.setOnClickListener {
            setFragmentResult(UNLOADING_HANDLE_BARCODE_RESULT,
                bundleOf(HANDLE_BARCODE_COMPLETE_KEY to binding.codeBox.text.toString()))
            findNavController().navigateUp()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireView().parent as View).setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

@Parcelize
data class UnloadingHandleParameters(val dstOfficeId: Int) : Parcelable