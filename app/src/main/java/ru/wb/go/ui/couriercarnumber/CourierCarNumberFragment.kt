package ru.wb.go.ui.couriercarnumber

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.CourierCarNumberFragmentBinding
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.ProgressDialogFragment
import ru.wb.go.views.ProgressButtonMode

class CourierCarNumberFragment : Fragment(R.layout.courier_car_number_fragment) {

    private lateinit var _binding: CourierCarNumberFragmentBinding
    private val binding get() = _binding

    private val viewModel by viewModel<CourierCarNumberViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierCarNumberFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initStateObserve()
        initReturnResult()
    }

    private fun initReturnResult() {
        setFragmentResultListener(ProgressDialogFragment.PROGRESS_DIALOG_RESULT) { _, bundle ->
            if (bundle.containsKey(ProgressDialogFragment.PROGRESS_DIALOG_BACK_KEY)) {
                viewModel.onCancelLoadClick()
            }
        }
    }

    private fun showProgressDialog() {
        val progressDialog = ProgressDialogFragment.newInstance()
        progressDialog.show(parentFragmentManager, ProgressDialogFragment.PROGRESS_DIALOG_TAG)
    }

    private fun closeProgressDialog() {
        parentFragmentManager.findFragmentByTag(ProgressDialogFragment.PROGRESS_DIALOG_TAG)?.let {
            if (it is ProgressDialogFragment) it.dismiss()
        }
    }

    private fun initViews() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lockNavDrawer()
    }

    private fun initListeners() {
        with(binding.carNumber) {
            binding.confirm.setOnClickListener { viewModel.onCheckCarNumberClick() }
        }
        binding.cancel.setOnClickListener { findNavController().popBackStack() }
        viewModel.onNumberObservableClicked(binding.viewKeyboard.observableListener)
    }

    private fun initStateObserve() {
        viewModel.navigationState.observe(viewLifecycleOwner, { state ->
            when (state) {
                is CourierCarNumberNavigationState.NavigateToTimer -> {
                    findNavController().navigate(
                        CourierCarNumberFragmentDirections.actionCourierCarNumberFragmentToCourierOrderConfirmFragment()
                    )
                }
            }
        })

        viewModel.navigateToDialogInfo.observe(viewLifecycleOwner) {
            showDialogInfo(it.type, it.title, it.message, it.button)
        }

        viewModel.stateBackspaceUI.observe(viewLifecycleOwner) {
            when (it) {
                CourierCarNumberBackspaceUIState.Active -> binding.viewKeyboard.active()
                CourierCarNumberBackspaceUIState.Inactive -> binding.viewKeyboard.inactive()
            }
        }

        viewModel.stateUI.observe(viewLifecycleOwner, { state ->
            when (state) {
                CourierCarNumberUIState.NumberFormatComplete -> {
                    binding.confirm.setState(ProgressButtonMode.ENABLE)
                    binding.viewKeyboard.unlock()
                    binding.viewKeyboard.active()
                }
                CourierCarNumberUIState.NumberNotFilled -> {
                    binding.confirm.setState(ProgressButtonMode.DISABLE)
                    binding.numberNotFound.visibility = INVISIBLE
                }
                is CourierCarNumberUIState.NumberNotFound -> {
                    showDialogInfo(
                        DialogInfoStyle.WARNING.ordinal,
                        state.title,
                        state.message,
                        state.button
                    )
                    binding.viewKeyboard.unlock()
                    binding.viewKeyboard.active()
                }
                is CourierCarNumberUIState.Error -> {
                    showDialogInfo(
                        DialogInfoStyle.WARNING.ordinal,
                        state.title,
                        state.message,
                        state.button
                    )
                    binding.viewKeyboard.unlock()
                    binding.viewKeyboard.active()
                }
                is CourierCarNumberUIState.NumberSpanFormat -> {
                    binding.carNumber.setText(
                        phoneSpannable(state),
                        TextView.BufferType.SPANNABLE
                    )
                    binding.viewKeyboard.setKeyboardMode(state.mode)
                }
            }

        })

        viewModel.progressState.observe(viewLifecycleOwner) {
            when (it) {
                CourierCarNumberProgressState.Progress -> showProgressDialog()
                CourierCarNumberProgressState.ProgressComplete -> closeProgressDialog()
            }
        }
    }

    private fun phoneSpannable(state: CourierCarNumberUIState.NumberSpanFormat): Spannable {
        val phone = state.numberFormat
        val spannable: Spannable = SpannableString(phone)
        val first = 0
        val last = state.count
        val span = ForegroundColorSpan(
            ResourcesCompat.getColor(resources, R.color.text_spannable_phone, null)
        )
        spannable.setSpan(span, first, last, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
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
        ).show(parentFragmentManager, DIALOG_INFO_TAG)
    }

}