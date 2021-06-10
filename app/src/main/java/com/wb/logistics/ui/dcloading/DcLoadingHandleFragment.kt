package com.wb.logistics.ui.dcloading

import android.R
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding3.widget.textChanges
import com.wb.logistics.databinding.DcLoadingHandleFragmentBinding
import com.wb.logistics.utils.SoftKeyboard
import com.wb.logistics.views.ProgressImageButtonMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class DcLoadingHandleFragment : BottomSheetDialogFragment() {

    private val viewModel by viewModel<DcLoadingHandleViewModel>()

    private var _binding: DcLoadingHandleFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(): DcLoadingHandleFragment {
            return DcLoadingHandleFragment()
        }

        const val HANDLE_BARCODE_RESULT = "HANDLE_INPUT_RESULT"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = DcLoadingHandleFragmentBinding.inflate(inflater, container, false)
        dialog?.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheetInternal =
                bottomSheetDialog.findViewById<View>(com.wb.logistics.R.id.design_bottom_sheet)
            if (bottomSheetInternal != null) {
                BottomSheetBehavior.from(bottomSheetInternal).state =
                    BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onResult(RESULT_CANCELED, "")
        initListener()
        initStateObserve()
        initKeyboard()
    }

    private fun initKeyboard() {
        activity?.let { SoftKeyboard.showKeyboard(it, binding.codeBox) }
    }

    private fun initStateObserve() {
        viewModel.stateUI.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DcLoadingHandleUIState.BoxFormatted -> {
                    binding.accept.setState(ProgressImageButtonMode.ENABLED)
                    setFormatCodeBox(state.number)
                }
                is DcLoadingHandleUIState.BoxAcceptDisabled -> {
                    setFormatCodeBox(state.number)
                    binding.accept.setState(ProgressImageButtonMode.DISABLED)
                }
            }
        }
    }

    private fun setFormatCodeBox(number: String) {
        binding.codeBox.setText(number)
        binding.codeBox.setSelection(number.length)
    }

    private fun initListener() {
        binding.close.setOnClickListener {
            onResult(RESULT_CANCELED, binding.codeBox.text.toString())
            this.dismiss()
        }
        viewModel.action(DcLoadingHandleUIAction.BoxChanges(binding.codeBox.textChanges()))
        binding.accept.setOnClickListener {
            onResult(RESULT_OK, binding.codeBox.text.toString())
            this.dismiss()
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

    private fun onResult(resultCode: Int, codeBox: String) {
        val intent = Intent().putExtra(HANDLE_BARCODE_RESULT, codeBox)
        targetFragment?.onActivityResult(targetRequestCode, resultCode, intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme(): Int {
        return com.wb.logistics.R.style.HandleInputBottomSheetDialog
    }

}