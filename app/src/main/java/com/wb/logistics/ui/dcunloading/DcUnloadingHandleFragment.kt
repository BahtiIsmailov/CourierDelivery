package com.wb.logistics.ui.dcunloading

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding3.widget.textChanges
import com.wb.logistics.databinding.DcUnloadingHandleFragmentBinding
import com.wb.logistics.ui.splash.KeyboardListener
import com.wb.logistics.ui.splash.NavToolbarListener
import com.wb.logistics.utils.SoftKeyboard
import com.wb.logistics.views.ProgressImageButtonMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class DcUnloadingHandleFragment : Fragment() {

    private val viewModel by viewModel<DcUnloadingHandleViewModel>()

    private var _binding: DcUnloadingHandleFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(): DcUnloadingHandleFragment {
            return DcUnloadingHandleFragment()
        }

        const val UNLOADING_HANDLE_BARCODE_CANCEL = "UNLOADING_HANDLE_BARCODE_RESULT1"
        const val HANDLE_BARCODE_CANCEL_KEY = "HANDLE_BARCODE_CANCEL_KEY"

        const val UNLOADING_HANDLE_BARCODE_COMPLETE = "UNLOADING_HANDLE_BARCODE_RESULT2"
        const val HANDLE_BARCODE_COMPLETE_KEY = "HANDLE_BARCODE_COMPLETE_KEY"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DcUnloadingHandleFragmentBinding.inflate(inflater, container, false)
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
        (activity as NavToolbarListener).backButtonIcon(com.wb.logistics.R.drawable.ic_close_dialog)
        (activity as KeyboardListener).panMode()
    }

    private fun initKeyboard() {
        activity?.let { SoftKeyboard.showKeyboard(it, binding.codeBox) }
    }

    private fun initBoxes(routeItems: List<String>) {
        val chartLegendAdapter = DcUnloadingHandleAdapter(requireContext(), routeItems)
        binding.boxes.adapter = chartLegendAdapter
    }

    private fun initStateObserve() {
        viewModel.stateUI.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DcUnloadingHandleUIState.BoxFormatted -> {
                    binding.accept.setState(ProgressImageButtonMode.ENABLED)
                    setFormatCodeBox(state.number)
                }
                is DcUnloadingHandleUIState.BoxAcceptDisabled -> {
                    setFormatCodeBox(state.number)
                    binding.accept.setState(ProgressImageButtonMode.DISABLED)
                }
                is DcUnloadingHandleUIState.BoxesComplete -> {
                    initBoxes(state.boxes)
                }
                DcUnloadingHandleUIState.BoxesEmpty -> {
                }
            }
        }
    }

    private fun setFormatCodeBox(number: String) {
        binding.codeBox.setText(number)
        binding.codeBox.setSelection(number.length)
    }

    private fun initListener() {
        viewModel.action(DcUnloadingHandleUIAction.BoxChanges(binding.codeBox.textChanges()))
        binding.accept.setOnClickListener {
            setFragmentResult(UNLOADING_HANDLE_BARCODE_COMPLETE,
                bundleOf(HANDLE_BARCODE_COMPLETE_KEY to binding.codeBox.text.toString()))
            findNavController().navigateUp()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireView().parent as View).setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.transparent
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}