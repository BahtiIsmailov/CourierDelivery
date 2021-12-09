package ru.wb.go.ui.auth

import android.os.Bundle
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.go.R
import ru.wb.go.databinding.AuthCheckSmsFragmentBinding
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogStyle
import ru.wb.go.ui.splash.NavDrawerListener
import ru.wb.go.ui.splash.NavToolbarListener

class CheckSmsFragment : Fragment(R.layout.auth_check_sms_fragment) {

    private var _binding: AuthCheckSmsFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<CheckSmsViewModel> {
        parametersOf(requireArguments().getParcelable<CheckSmsParameters>(CHECK_SMS_KEY))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = AuthCheckSmsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
        initObserve()
    }

    private fun initViews() {
        (activity as NavDrawerListener).lock()
    }

    private fun initListener() {
        binding.toolbarLayout.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.toolbarLayout.noInternetImage.setOnClickListener {
            (activity as NavToolbarListener).showNetworkDialog()
        }
        viewModel.onNumberObservableClicked(binding.viewKeyboard.observableListener)
        binding.repeatSms.setOnClickListener { viewModel.onRepeatPassword() }
    }

    private fun initObserve() {

        viewModel.stateTitleUI.observe(viewLifecycleOwner, { state ->
            binding.numberPhoneTitle.setText(phoneSpannable(state), TextView.BufferType.SPANNABLE)
            binding.numberPhoneTitle.visibility = VISIBLE
        })

        viewModel.navigationEvent.observe(viewLifecycleOwner, { state ->
            when (state) {
                CheckSmsNavigationState.NavigateToAppLoader ->
                    findNavController().navigate(R.id.load_navigation)
            }
        })

        viewModel.stateBackspaceUI.observe(viewLifecycleOwner) {
            when (it) {
                CheckSmsBackspaceUIState.Active -> binding.viewKeyboard.active()
                CheckSmsBackspaceUIState.Inactive -> binding.viewKeyboard.inactive()
            }
        }

        viewModel.toolbarNetworkState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Failed ->
                    binding.toolbarLayout.noInternetImage.visibility = VISIBLE
                is NetworkState.Complete ->
                    binding.toolbarLayout.noInternetImage.visibility = INVISIBLE
            }
        }

        viewModel.checkSmsUIState.observe(viewLifecycleOwner, { state ->
            when (state) {
                CheckSmsUIState.Progress -> {
                    binding.smsCodeProgress.visibility = VISIBLE
                    binding.viewKeyboard.lock()
                    binding.viewKeyboard.inactive()
                    binding.bottomInfo.visibility = INVISIBLE
                    binding.repeatSms.isEnabled = false
                }
                CheckSmsUIState.Complete -> {
                    binding.smsCodeProgress.visibility = INVISIBLE
                    binding.viewKeyboard.unlock()
                    binding.viewKeyboard.active()
                    binding.bottomInfo.visibility = INVISIBLE
                    binding.repeatSms.isEnabled = false
                }
                is CheckSmsUIState.Error -> {
                    binding.viewPinCode.text?.clear()
                    binding.smsCodeProgress.visibility = INVISIBLE
                    binding.viewKeyboard.unlock()
                    binding.viewKeyboard.active()
                    binding.viewKeyboard.clear()
                    binding.bottomInfo.visibility = VISIBLE
                    binding.bottomInfo.text = state.title
                    binding.repeatSms.isEnabled = true
                }
                is CheckSmsUIState.MessageError -> {
                    binding.viewPinCode.text?.clear()
                    showDialog(
                        DialogStyle.WARNING.ordinal, state.title,
                        state.message,
                        state.button
                    )
                    binding.smsCodeProgress.visibility = INVISIBLE
                    binding.viewKeyboard.unlock()
                    binding.viewKeyboard.active()
                    binding.viewKeyboard.clear()
                    binding.bottomInfo.visibility = INVISIBLE
                    binding.repeatSms.isEnabled = true
                }
                is CheckSmsUIState.CodeFormat -> {
                    binding.viewPinCode.setText(state.code)
                }
            }
        })

        viewModel.repeatStateUI.observe(viewLifecycleOwner, { state ->
            when (state) {
                CheckSmsUIRepeatState.RepeatPasswordComplete -> {
                    binding.repeatSmsTimer.visibility = View.GONE
                    binding.repeatSms.isEnabled = true
                    binding.repeatSms.visibility = VISIBLE
                    binding.bottomInfo.visibility = INVISIBLE
                    binding.repeatPasswordProgress.visibility = View.GONE
                }
                is CheckSmsUIRepeatState.RepeatPasswordTimer -> {
                    binding.repeatSmsTimer.visibility = VISIBLE
                    binding.repeatSmsTimer.setText(
                        timeSpannable(state),
                        TextView.BufferType.SPANNABLE
                    )
                    binding.repeatSms.isEnabled = true
                    binding.repeatSms.visibility = View.GONE
                    binding.repeatPasswordProgress.visibility = View.GONE
                }
                CheckSmsUIRepeatState.RepeatPasswordProgress -> {
                    binding.repeatSmsTimer.visibility = View.GONE
                    binding.repeatSms.isEnabled = false
                    binding.repeatSms.visibility = VISIBLE
                    binding.repeatPasswordProgress.visibility = VISIBLE
                }
                is CheckSmsUIRepeatState.ErrorPassword -> {
                    binding.repeatSmsTimer.visibility = View.GONE
                    binding.repeatSms.isEnabled = true
                    binding.repeatSms.visibility = VISIBLE
                    binding.repeatPasswordProgress.visibility = View.GONE
                    showDialog(
                        DialogStyle.WARNING.ordinal,
                        state.title,
                        state.message,
                        state.button
                    )
                }
            }
        })
    }

    private fun timeSpannable(state: CheckSmsUIRepeatState.RepeatPasswordTimer): Spannable {
        val title = state.text
        val spannable: Spannable = SpannableString(title)
        val first = title.indexOf(state.timeStart)
        val last = title.length
        spannable.setSpan(
            ForegroundColorSpan(
                ResourcesCompat.getColor(
                    resources,
                    R.color.text_spannable_time,
                    null
                )
            ),
            first,
            last,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    private fun phoneSpannable(state: CheckSmsViewModel.InitTitle): Spannable {
        val title = state.title
        val spannable: Spannable = SpannableString(title)
        val first = title.indexOf(state.phone)
        val last = first + state.phone.length
        spannable.setSpan(
            ForegroundColorSpan(
                ResourcesCompat.getColor(
                    resources,
                    R.color.text_spannable_phone,
                    null
                )
            ),
            first,
            last,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDialog(style: Int, title: String, message: String, positiveButtonName: String) {
        DialogInfoFragment.newInstance(style, title, message, positiveButtonName)
            .show(parentFragmentManager, DialogInfoFragment.DIALOG_INFO_TAG)
    }

    companion object {
        const val CHECK_SMS_KEY = "check_sms_key"
    }

}

@Parcelize
data class CheckSmsParameters(val phone: String, val ttl: Int) : Parcelable


