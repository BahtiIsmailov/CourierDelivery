package ru.wb.go.ui.courierdataexpects

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.go.R
import ru.wb.go.databinding.CourierDataExpectsFragmentBinding
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.courierdata.CourierDataParameters
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.utils.SoftKeyboard
import ru.wb.go.utils.managers.ErrorDialogData

class CourierDataExpectsFragment : Fragment() {

    private val viewModel by viewModel<CouriersCompleteRegistrationViewModel> {
        parametersOf(
            requireArguments().getParcelable<CourierDataExpectsParameters>(
                PHONE_KEY
            )
        )
    }

    private var _binding: CourierDataExpectsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierDataExpectsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
        initListener()
        initKeyboard()
        initReturnDialogResult()
    }

    private fun initReturnDialogResult() {
        setFragmentResultListener(DIALOG_EXPECTS_ERROR_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DialogInfoFragment.DIALOG_INFO_BACK_KEY)) {
                viewModel.onErrorDialogConfirmClick()
            }
        }
    }

    private fun initKeyboard() {
        SoftKeyboard.hideKeyBoard(requireActivity())
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
    }

    private fun initObserver() {

        viewModel.showDialogInfo.observe(viewLifecycleOwner) {
            showDialogInfo(it)
        }

        viewModel.navigateToErrorDialog.observe(viewLifecycleOwner) {
            showDialogError(it)
        }

        viewModel.navigationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierDataExpectsNavAction.NavigateToCouriers ->
                    findNavController().navigate(
                        CourierDataExpectsFragmentDirections
                            .actionCouriersCompleteRegistrationFragmentToCourierWarehouseFragment()
                    )
                is CourierDataExpectsNavAction.NavigateToDataType ->
                    findNavController().navigate(
                        CourierDataExpectsFragmentDirections
                            .actionCouriersCompleteRegistrationFragmentToCourierDataTypeFragment(
                                CourierDataParameters(
                                    phone = state.phone,
                                    docs = state.docs
                                )
                            )
                    )

            }
        }

        viewModel.progressStateData.observe(viewLifecycleOwner) {
            when (it) {
                CourierDataExpectsProgressState.Complete -> {
                    binding.progress.visibility = GONE
                    binding.updateStatus.isEnabled = true
                }
                CourierDataExpectsProgressState.ProgressData -> {
                    binding.progress.visibility = VISIBLE
                    binding.updateStatus.isEnabled = false
                }
            }
        }
    }

    private fun showDialogInfo(
        errorDialogData: ErrorDialogData
    ) {
        DialogInfoFragment.newInstance(
            resultTag = errorDialogData.dlgTag,
            type = errorDialogData.type,
            title = errorDialogData.title,
            message = errorDialogData.message,
            positiveButtonName = requireContext().getString(R.string.ok_button_title)
        ).show(parentFragmentManager, DIALOG_INFO_TAG)
    }

    private fun showDialogError(
        errorDialogData: ErrorDialogData
    ) {
        DialogInfoFragment.newInstance(
            resultTag = errorDialogData.dlgTag,
            type = errorDialogData.type,
            title = errorDialogData.title,
            message = errorDialogData.message,
            positiveButtonName = "Исправить"
        ).show(parentFragmentManager, DIALOG_EXPECTS_ERROR_RESULT_TAG)
    }

    private fun initListener() {
        binding.updateStatus.setOnClickListener { viewModel.onUpdateStatusClick() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val PHONE_KEY = "phone_key"
        const val DIALOG_EXPECTS_ERROR_RESULT_TAG = "DIALOG_EXPECTS_ERROR_RESULT_TAG"
    }

}

@Parcelize
data class CourierDataExpectsParameters(val phone: String) : Parcelable