package ru.wb.perevozka.ui.auth

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
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.widget.textChanges
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.AuthCheckSmsFragmentBinding
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.ui.userdata.couriers.CouriersCompleteRegistrationParameters
import ru.wb.perevozka.ui.userdata.userform.UserFormParameters
import ru.wb.perevozka.utils.SoftKeyboard
import ru.wb.perevozka.views.ProgressImageButtonMode

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
        initKeyboard()
        initListener()
        initStateObserve()
    }

    private fun initViews() {
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.auth_check_sms_toolbar_label)
        binding.toolbarLayout.back.visibility = View.VISIBLE
        binding.sms.isEnabled = true
        binding.next.setState(ProgressImageButtonMode.ENABLED)
    }

    private fun initKeyboard() {
        activity?.let { SoftKeyboard.showKeyboard(it, binding.sms) }
    }

    private fun initListener() {
        binding.toolbarLayout.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.toolbarLayout.noInternetImage.setOnClickListener {
            (activity as NavToolbarListener).showNetworkDialog()
        }
        val sms = binding.sms
        viewModel.passwordChanges(sms.textChanges())
        binding.repeatSms.setOnClickListener { viewModel.onRepeatPassword() }
        binding.next.setOnClickListener {
            viewModel.authClick(sms.text.toString())
        }
    }

    private fun initStateObserve() {

        viewModel.stateTitleUI.observe(viewLifecycleOwner, { state ->
            binding.numberPhoneTitle.setText(phoneSpannable(state), TextView.BufferType.SPANNABLE)
            binding.numberPhoneTitle.visibility = VISIBLE
        })

        viewModel.navigationEvent.observe(viewLifecycleOwner, { state ->
            when (state) {
                CheckSmsNavAction.NavigateToApplication ->
                    findNavController().navigate(R.id.load_navigation)
                is CheckSmsNavAction.NavigateToCompletionRegistration -> findNavController().navigate(
                    CheckSmsFragmentDirections.actionCheckSmsFragmentToCouriersCompleteRegistrationFragment(
                        CouriersCompleteRegistrationParameters(state.phone)
                    )
                )
                is CheckSmsNavAction.NavigateToUserForm -> findNavController().navigate(
                    CheckSmsFragmentDirections.actionCheckSmsFragmentToUserFormFragment(
                        UserFormParameters(state.phone)
                    )
                )
            }
        })

        viewModel.toolbarNetworkState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Failed ->
                    binding.toolbarLayout.noInternetImage.visibility = VISIBLE
                is NetworkState.Complete ->
                    binding.toolbarLayout.noInternetImage.visibility = INVISIBLE
            }
        }

        viewModel.stateUI.observe(viewLifecycleOwner, { state ->
            when (state) {
                CheckSmsUIState.SaveAndNextDisable -> {
                    binding.next.setState(ProgressImageButtonMode.DISABLED)
                    binding.bottomInfo.visibility = INVISIBLE
                }
                CheckSmsUIState.SaveAndNextEnable ->
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                CheckSmsUIState.Progress -> {
                    binding.sms.isEnabled = false
                    binding.repeatSms.isEnabled = false
                    binding.next.setState(ProgressImageButtonMode.PROGRESS)
                }
                CheckSmsUIState.Complete -> {
                    binding.sms.isEnabled = false
                    binding.bottomInfo.visibility = INVISIBLE
                    binding.repeatSms.isEnabled = false
                    binding.next.setState(ProgressImageButtonMode.DISABLED)
                }
                CheckSmsUIState.Error -> {
                    binding.sms.isEnabled = true
                    binding.bottomInfo.visibility = VISIBLE
                    binding.repeatSms.isEnabled = true
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                }
                is CheckSmsUIState.MessageError -> {
                    showBarMessage(state.message)
                    binding.sms.isEnabled = true
                    binding.bottomInfo.visibility = INVISIBLE
                    binding.repeatSms.isEnabled = true
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                }
            }
        })

        viewModel.repeatStateUI.observe(viewLifecycleOwner, { state ->
            when (state) {
                CheckSmsUIRepeatState.RepeatPasswordComplete -> {
                    binding.repeatSmsTimer.visibility = View.GONE
                    binding.repeatSms.isEnabled = true
                    binding.repeatSms.visibility = VISIBLE
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
                    showBarMessage(state.message)
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

    private fun showBarMessage(state: String) {
        Snackbar.make(binding.next, state, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        const val CHECK_SMS_KEY = "check_sms_key"
    }

}

@Parcelize
data class CheckSmsParameters(val phone: String, val ttl: Int) : Parcelable


