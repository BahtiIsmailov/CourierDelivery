package com.wb.logistics.ui.unloading

import android.R
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding3.widget.textChanges
import com.wb.logistics.databinding.UnloadingHandleFragmentBinding
import com.wb.logistics.views.ProgressImageButtonMode
import kotlinx.android.parcel.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class UnloadingHandleFragment : BottomSheetDialogFragment() {

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

        const val UNLOADING_HANDLE_BARCODE_CANCEL = "UNLOADING_HANDLE_BARCODE_RESULT1"
        const val HANDLE_BARCODE_CANCEL_KEY = "HANDLE_BARCODE_CANCEL_KEY"

        const val UNLOADING_HANDLE_BARCODE_COMPLETE = "UNLOADING_HANDLE_BARCODE_RESULT2"
        const val HANDLE_BARCODE_COMPLETE_KEY = "HANDLE_BARCODE_COMPLETE_KEY"

        const val UNLOADING_HANDLE_KEY = "unloading_handle_key"
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
        _binding = UnloadingHandleFragmentBinding.inflate(inflater, container, false)
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
        //onResult(RESULT_CANCELED, "")
        initListener()
        initStateObserve()
    }

    private fun initBoxes(routeItems: List<String>) {
        val chartLegendAdapter = UnloadingBoxesAdapter(requireContext(), routeItems)
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
                    binding.boxesLayout.visibility = VISIBLE
                }
                UnloadingHandleUIState.BoxesEmpty -> {
                    binding.boxesLayout.visibility = GONE
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
            setFragmentResult(UNLOADING_HANDLE_BARCODE_CANCEL,
                bundleOf(HANDLE_BARCODE_CANCEL_KEY to ""))
            findNavController().navigateUp()
        }
        viewModel.action(UnloadingHandleUIAction.BoxChanges(binding.codeBox.textChanges()))
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

@Parcelize
data class UnloadingHandleParameters(val dstOfficeId: Int) : Parcelable