package ru.wb.perevozka.ui.auth.courierexpects

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
import ru.wb.perevozka.R
import ru.wb.perevozka.app.DIALOG_INFO_MESSAGE_TAG
import ru.wb.perevozka.databinding.AuthCourierExpectsFragmentBinding
import ru.wb.perevozka.ui.dialogs.DialogInfoFragment
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.utils.SoftKeyboard
import ru.wb.perevozka.views.ProgressButtonMode

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
            showDialog(it.style, it.title, it.message, it.button)
        }

        viewModel.navAction.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierExpectsNavAction.NavigateToApplication ->
                    findNavController().navigate(R.id.load_navigation)
                is CourierExpectsNavAction.NavigateToCouriersDialog -> {
                    showDialog(state.style, state.title, state.message, state.button)
                    binding.updateStatus.setState(ProgressButtonMode.ENABLE)
                }
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

    private fun showDialog(style: Int, title: String, message: String, positiveButtonName: String) {
        DialogInfoFragment.newInstance(style, title, message, positiveButtonName)
            .show(parentFragmentManager, DIALOG_INFO_MESSAGE_TAG)
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