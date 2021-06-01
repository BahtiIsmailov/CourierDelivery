package com.wb.logistics.ui.auth

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding3.widget.textChanges
import com.wb.logistics.R
import com.wb.logistics.databinding.AuthTemporaryPasswordFragmentBinding
import com.wb.logistics.utils.LogUtils
import com.wb.logistics.views.ProgressImageButtonMode
import kotlinx.android.parcel.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TemporaryPasswordFragment : Fragment(R.layout.auth_temporary_password_fragment) {

    private var _binding: AuthTemporaryPasswordFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var inputMethod: InputMethodManager
    private val viewModel by viewModel<TemporaryPasswordViewModel> {
        parametersOf(requireArguments().getParcelable<TemporaryPasswordParameters>(PHONE_KEY))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = AuthTemporaryPasswordFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
        initInputMethod()
        initStateObserver()
    }

    private fun initViews() {
        binding.repeatPasswordTimer.visibility = GONE
        binding.repeatPassword.visibility = GONE
        binding.next.setState(ProgressImageButtonMode.PROGRESS)
    }

    private fun initListener() {
        val password = binding.password
        viewModel.action(TemporaryPasswordUIAction.PasswordChanges(password.textChanges()))
        binding.repeatPassword.setOnClickListener { viewModel.action(TemporaryPasswordUIAction.RepeatTmpPassword) }
        binding.next.setOnClickListener {
            viewModel.action(
                TemporaryPasswordUIAction.CheckPassword(
                    password.text.toString()
                )
            )
        }
    }

    private fun initInputMethod() {
        inputMethod =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private fun initStateObserver() {

        viewModel.stateTitleUI.observe(viewLifecycleOwner, { state ->
            binding.numberPhoneTitle.setText(phoneSpannable(state), TextView.BufferType.SPANNABLE)
            binding.numberPhoneTitle.visibility = VISIBLE
        })

        viewModel.navigationEvent.observe(viewLifecycleOwner, { state ->
            when (state) {
                is TemporaryPasswordNavAction.NavigateToCreatePassword -> {
                    findNavController().navigate(
                        TemporaryPasswordFragmentDirections.actionTemporaryPasswordFragmentToCreatePasswordFragment(
                            CreatePasswordParameters(state.phone, state.tmpPassword)
                        )
                    )
                    binding.password.text?.clear()
                    binding.password.isEnabled = true
                    binding.password.text?.clear()
                    binding.bottomInfo.visibility = GONE
                    binding.repeatPasswordTimer.visibility = GONE
                    binding.repeatPassword.visibility = GONE
                    binding.next.setState(ProgressImageButtonMode.DISABLED)
                }
            }
        })

        viewModel.stateUI.observe(viewLifecycleOwner, { state ->
            LogUtils {logDebugApp(state.toString())}
            when (state) {
                TemporaryPasswordUIState.FetchingTmpPassword -> {
                    binding.password.isEnabled = false
                    binding.password.text?.clear()
                    binding.bottomInfo.visibility = GONE
                    binding.repeatPasswordTimer.visibility = GONE
                    binding.repeatPassword.visibility = GONE
                    binding.next.setState(ProgressImageButtonMode.DISABLED)
                }
                TemporaryPasswordUIState.NextDisable -> {
                    binding.passwordLayout.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.border_input_normal)
                    binding.bottomInfo.visibility = GONE
                    binding.next.setState(ProgressImageButtonMode.DISABLED)
                }
                TemporaryPasswordUIState.NextEnable -> {
                    binding.passwordLayout.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.border_input_normal)
                    binding.bottomInfo.visibility = GONE
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                }
                is TemporaryPasswordUIState.RepeatPasswordTimer -> {
                    binding.password.isEnabled = true
                    binding.repeatPasswordTimer.visibility = VISIBLE
                    binding.repeatPasswordTimer.setText(
                        timeSpannable(state),
                        TextView.BufferType.SPANNABLE
                    )
                    binding.repeatPassword.visibility = GONE
                }
                TemporaryPasswordUIState.RepeatPassword -> {
                    binding.password.isEnabled = true
                    binding.repeatPasswordTimer.visibility = GONE
                    binding.repeatPassword.visibility = VISIBLE
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                }
                is TemporaryPasswordUIState.Update -> {
                    binding.password.isEnabled = true
                    binding.password.text?.clear()
                    binding.repeatPasswordTimer.visibility = GONE
                    binding.repeatPassword.visibility = VISIBLE
                    binding.bottomInfo.visibility = VISIBLE
                    binding.bottomInfo.text = state.message
                    binding.next.setState(ProgressImageButtonMode.DISABLED)
                }
                is TemporaryPasswordUIState.Error -> {
                    binding.bottomInfo.visibility = VISIBLE
                    binding.passwordLayout.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.border_input_error)
                    binding.password.isEnabled = true
                    binding.repeatPasswordTimer.visibility = GONE
                    binding.repeatPassword.visibility = GONE
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                }
                TemporaryPasswordUIState.Progress -> {
                    binding.password.isEnabled = false
                    binding.repeatPasswordTimer.visibility = GONE
                    binding.repeatPassword.visibility = GONE
                    binding.next.setState(ProgressImageButtonMode.PROGRESS)
                }
            }
        })
    }

    private fun phoneSpannable(state: InitTitle): Spannable {
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

    private fun timeSpannable(state: TemporaryPasswordUIState.RepeatPasswordTimer): Spannable {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val PHONE_KEY = "phone_key"
    }

}

@Parcelize
data class TemporaryPasswordParameters(val phone: String) : Parcelable


