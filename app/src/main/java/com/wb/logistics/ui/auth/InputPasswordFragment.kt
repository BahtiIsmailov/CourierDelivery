package com.wb.logistics.ui.auth

import android.os.Bundle
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.widget.textChanges
import com.wb.logistics.R
import com.wb.logistics.databinding.AuthInputPasswordFragmentBinding
import com.wb.logistics.views.ProgressImageButtonMode
import kotlinx.android.parcel.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class InputPasswordFragment : Fragment(R.layout.auth_input_password_fragment) {

    private var _binding: AuthInputPasswordFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<InputPasswordViewModel> {
        parametersOf(requireArguments().getParcelable<InputPasswordParameters>(PHONE_KEY))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AuthInputPasswordFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
        initStateObserve()
    }

    private fun initViews() {
        binding.password.isEnabled = true
        binding.remindPassword.isEnabled = true
        binding.next.setState(ProgressImageButtonMode.ENABLED)
    }

    private fun initListener() {
        val password = binding.password
        viewModel.action(InputPasswordUIAction.PasswordChanges(password.textChanges()))
        binding.remindPassword.setOnClickListener { viewModel.action(InputPasswordUIAction.RemindPassword) }
        binding.next.setOnClickListener {
            viewModel.action(
                InputPasswordUIAction.Auth(
                    password.text.toString()
                )
            )
        }
    }

    private fun initStateObserve() {
        viewModel.stateUI.observe(viewLifecycleOwner, { state ->
            when (state) {
                is InputPasswordUIState.NavigateToTemporaryPassword ->
                    findNavController().navigate(
                        InputPasswordFragmentDirections.actionInputPasswordFragmentToTemporaryPasswordFragment(
                            TemporaryPasswordParameters(state.phone)
                        )
                    )
                InputPasswordUIState.NavigateToApplication ->
                    findNavController().navigate(
                        InputPasswordFragmentDirections.actionInputPasswordFragmentToNavigationActivity()
                    )
                InputPasswordUIState.NextDisable -> binding.next.setState(
                    ProgressImageButtonMode.DISABLED
                )
                is InputPasswordUIState.InitTitle -> {
                    binding.numberPhoneTitle.setText(
                        phoneSpannable(state),
                        TextView.BufferType.SPANNABLE
                    )
                    binding.numberPhoneTitle.visibility = View.VISIBLE
                }
                InputPasswordUIState.NextEnable -> binding.next.setState(ProgressImageButtonMode.ENABLED)
                InputPasswordUIState.AuthProcess -> {
                    binding.password.isEnabled = false
                    binding.remindPassword.isEnabled = false
                    binding.next.setState(ProgressImageButtonMode.PROGRESS)
                }
                InputPasswordUIState.AuthComplete -> {
                    binding.password.isEnabled = false
                    binding.remindPassword.isEnabled = false
                    binding.next.setState(ProgressImageButtonMode.DISABLED)
                }
                is InputPasswordUIState.Error -> {
                    showBarMessage(state.message)
                    binding.password.isEnabled = true
                    binding.remindPassword.isEnabled = true
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                }
            }
        })
    }

    private fun phoneSpannable(state: InputPasswordUIState.InitTitle): Spannable {
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
        const val PHONE_KEY = "phone_key"
    }

}

@Parcelize
data class InputPasswordParameters(
    val phone: String
) : Parcelable


