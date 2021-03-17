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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.widget.textChanges
import com.wb.logistics.R
import com.wb.logistics.databinding.AuthTemporaryPasswordFragmentBinding
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
        savedInstanceState: Bundle?
    ): View {
        _binding = AuthTemporaryPasswordFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
        initActionBar()
        initInputMethod()
        initStateObserve()
    }

    private fun initViews() {
        binding.password.isEnabled = false
        binding.repeatPasswordTimer.visibility = GONE
        binding.repeatPassword.visibility = GONE
        binding.next.setState(ProgressImageButtonMode.PROGRESS)
    }

    private fun initListener() {
        val password = binding.password
        viewModel.action(TemporaryPasswordUIAction.PasswordChanges(password.textChanges()))
        binding.repeatPassword.setOnClickListener { viewModel.action(TemporaryPasswordUIAction.RepeatPassword) }
        binding.next.setOnClickListener {
            viewModel.action(
                TemporaryPasswordUIAction.CheckPassword(
                    password.text.toString()
                )
            )
        }
    }

    private fun initActionBar() {
        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    private fun initInputMethod() {
        inputMethod =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private fun initStateObserve() {
        viewModel.stateUI.observe(viewLifecycleOwner, { state ->
            when (state) {
                TemporaryPasswordUIState.FetchingTmpPassword -> {
                    binding.password.isEnabled = false
                    binding.password.text?.clear()
                    binding.countAttempt.visibility = GONE
                    binding.repeatPasswordTimer.visibility = GONE
                    binding.repeatPassword.visibility = GONE
                    binding.next.setState(ProgressImageButtonMode.PROGRESS)
                }
                is TemporaryPasswordUIState.RemainingAttempts -> {
                    binding.password.isEnabled = true
                    binding.countAttempt.text = state.remainingAttempts
                    binding.countAttempt.visibility = VISIBLE
                    binding.repeatPasswordTimer.visibility = GONE
                    binding.repeatPassword.visibility = GONE
                    binding.next.setState(ProgressImageButtonMode.DISABLED)
                }
                is TemporaryPasswordUIState.NavigateToCreatePassword ->
                    findNavController().navigate(
                        TemporaryPasswordFragmentDirections.actionTemporaryPasswordFragmentToCreatePasswordFragment(
                            CreatePasswordParameters(state.phone, state.tmpPassword)
                        )
                    )
                is TemporaryPasswordUIState.Error -> {
                    showBarMessage(state.message)
                    binding.password.isEnabled = true
                    binding.repeatPasswordTimer.visibility = GONE
                    binding.repeatPassword.visibility = GONE
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                }
                is TemporaryPasswordUIState.InitTitle -> {
                    binding.numberPhoneTitle.setText(
                        phoneSpannable(state),
                        TextView.BufferType.SPANNABLE
                    )
                    binding.numberPhoneTitle.visibility = VISIBLE
                }
                TemporaryPasswordUIState.NextDisable -> {
                    binding.next.setState(ProgressImageButtonMode.DISABLED)
                }
                TemporaryPasswordUIState.NextEnable -> {
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                }
                is TemporaryPasswordUIState.RepeatPasswordTimer -> {
                    binding.repeatPasswordTimer.visibility = VISIBLE
                    binding.repeatPasswordTimer.setText(
                        timeSpannable(state),
                        TextView.BufferType.SPANNABLE
                    )
                    binding.repeatPassword.visibility = GONE
                }
                TemporaryPasswordUIState.RepeatPassword -> {
                    binding.repeatPasswordTimer.visibility = GONE
                    binding.repeatPassword.visibility = VISIBLE
                    binding.next.setState(ProgressImageButtonMode.DISABLED)
                }
                is TemporaryPasswordUIState.Update -> {
                    showBarMessage(state.message)
                    binding.password.isEnabled = true
                    binding.password.text?.clear()
                    binding.repeatPasswordTimer.visibility = GONE
                    binding.repeatPassword.visibility = VISIBLE
                    binding.countAttempt.visibility = VISIBLE
                    binding.next.setState(ProgressImageButtonMode.DISABLED)
                }
            }
        })
    }

    private fun phoneSpannable(state: TemporaryPasswordUIState.InitTitle): Spannable {
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

    private fun showBarMessage(state: String) {
        Snackbar.make(binding.next, state, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        const val PHONE_KEY = "phone_key"
    }

}

@Parcelize
data class TemporaryPasswordParameters(
    val phone: String
) : Parcelable


