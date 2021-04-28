package com.wb.logistics.ui.reception

import android.R
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding3.widget.textChanges
import com.wb.logistics.databinding.ReceptionHandleFragmentBinding
import com.wb.logistics.views.ProgressImageButtonMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReceptionHandleFragment : BottomSheetDialogFragment() {

    private val viewModel by viewModel<ReceptionHandleViewModel>()

    private var _binding: ReceptionHandleFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(): ReceptionHandleFragment {
            return ReceptionHandleFragment()
        }

        const val HANDLE_BARCODE_RESULT = "HANDLE_INPUT_RESULT"
    }


//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = super.onCreateDialog(savedInstanceState)
//
//        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
////        dialog.setOnShowListener {
////            Handler().post {
////                val bottomSheet =
////                    (dialog as? BottomSheetDialog)?.findViewById<View>(R.id.design_bottom_sheet) as? FrameLayout
////                bottomSheet?.let {
////                    BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
////                }
////            }
////        }
//
//        return dialog
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ReceptionHandleFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: 30.03.2021 реализовать отображение view на весь экран
//        dialog?.setOnShowListener {
//            val dialog = it as BottomSheetDialog
//            val bottomSheet = dialog.findViewById<View>(android.support.design.R.id.design_bottom_sheet)
//            bottomSheet?.let { sheet ->
//                dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
//                sheet.parent.parent.requestLayout()
//            }
//        }
        onResult(RESULT_CANCELED, "")

        initListener()
        initStateObserve()
    }

    private fun initStateObserve() {
        viewModel.stateUI.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ReceptionHandleUIState.BoxFormatted -> {
                    binding.accept.setState(ProgressImageButtonMode.ENABLED)
                    setFormatCodeBox(state.number)
                }
                is ReceptionHandleUIState.BoxAcceptDisabled -> {
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
        viewModel.action(ReceptionHandleUIAction.BoxChanges(binding.codeBox.textChanges()))
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

}