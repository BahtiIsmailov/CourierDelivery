package ru.wb.go.ui.courierexpects

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.go.databinding.AuthCourierExpectsFragmentBinding
import ru.wb.go.ui.courierdata.CourierDataParameters
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.splash.NavToolbarListener
import ru.wb.go.utils.SoftKeyboard
import ru.wb.go.views.ProgressButtonMode

class CourierExpectsFragment : Fragment() {

    private val viewModel by viewModel<CouriersCompleteRegistrationViewModel> {
        parametersOf(
            requireArguments().getParcelable<CourierExpectsParameters>(
                PHONE_KEY
            )
        )
    }

    private var _binding: AuthCourierExpectsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = AuthCourierExpectsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
        initListener()
        initKeyboard()
    }

    private fun initKeyboard() {
        SoftKeyboard.hideKeyBoard(requireActivity())
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
    }

    private fun initObserver() {

        viewModel.navigateToMessageState.observe(viewLifecycleOwner) {
            showDialogInfo(it.type, it.title, it.message, it.button)
        }

        viewModel.navigationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierExpectsNavAction.NavigateToCouriers ->
                    findNavController().navigate(
                        CourierExpectsFragmentDirections
                            .actionCouriersCompleteRegistrationFragmentToCourierWarehouseFragment()
                    )
               is CourierExpectsNavAction.NavigateToRegistrationCouriers->
                    findNavController().navigate(
                        CourierExpectsFragmentDirections
                            .actionCouriersCompleteRegistrationFragmentToUserFormFragment(
                                CourierDataParameters(state.phone, state.docs)
                            )
                    )
            }
        }

        viewModel.progressState.observe(viewLifecycleOwner) {
            when (it) {
                CourierExpectsProgressState.Complete ->
                    binding.updateStatus.setState(ProgressButtonMode.ENABLE)
                CourierExpectsProgressState.Progress ->
                    binding.updateStatus.setState(ProgressButtonMode.PROGRESS)
            }
        }
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

    private fun initListener() {
        binding.updateStatus.setOnClickListener { viewModel.onUpdateStatusClick() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val PHONE_KEY = "phone_key"
    }

}

@Parcelize
data class CourierExpectsParameters(val phone: String) : Parcelable