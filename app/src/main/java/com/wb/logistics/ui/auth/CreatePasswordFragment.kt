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
import com.wb.logistics.databinding.AuthCreatePasswordFragmentBinding
import com.wb.logistics.utils.SoftKeyboard
import com.wb.logistics.views.ProgressImageButtonMode
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CreatePasswordFragment : Fragment(R.layout.auth_create_password_fragment) {

    private var _binding: AuthCreatePasswordFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<CreatePasswordViewModel> {
        parametersOf(requireArguments().getParcelable<CreatePasswordParameters>(PHONE_KEY))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = AuthCreatePasswordFragmentBinding.inflate(inflater, container, false)
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
        binding.password.isEnabled = true
        binding.next.setState(ProgressImageButtonMode.ENABLED)
    }

    private fun initKeyboard() {
        activity?.let { SoftKeyboard.showKeyboard(it, binding.password) }
    }

    private fun initListener() {
        val password = binding.password
        viewModel.action(CreatePasswordUIAction.PasswordChanges(password.textChanges()))
        binding.next.setOnClickListener {
            viewModel.action(
                CreatePasswordUIAction.Auth(
                    password.text.toString()
                )
            )
        }
    }

    private fun initStateObserve() {

        viewModel.navigationEvent.observe(viewLifecycleOwner, { state ->
            when (state) {
                CreatePasswordNavAction.NavigateToApplication ->
                    findNavController().navigate(R.id.load_navigation)
            }
        })

        viewModel.stateUI.observe(viewLifecycleOwner, { state ->
            when (state) {
                CreatePasswordUIState.SaveAndNextDisable -> binding.next.setState(
                    ProgressImageButtonMode.DISABLED
                )
                is CreatePasswordUIState.InitTitle -> {
                    binding.numberPhoneTitle.setText(
                        phoneSpannable(state),
                        TextView.BufferType.SPANNABLE
                    )
                    binding.numberPhoneTitle.visibility = View.VISIBLE
                }
                CreatePasswordUIState.SaveAndNextEnable -> binding.next.setState(
                    ProgressImageButtonMode.ENABLED)
                CreatePasswordUIState.AuthProcess -> {
                    binding.password.isEnabled = false
                    binding.next.setState(ProgressImageButtonMode.PROGRESS)
                }
                CreatePasswordUIState.AuthComplete -> {
                    binding.password.isEnabled = false
                    binding.next.setState(ProgressImageButtonMode.DISABLED)
                }
                is CreatePasswordUIState.Error -> {
                    showBarMessage(state.message)
                    binding.password.isEnabled = true
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                }
            }
        })
    }

    private fun phoneSpannable(state: CreatePasswordUIState.InitTitle): Spannable {
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
data class CreatePasswordParameters(val phone: String, val tmpPassword: String) : Parcelable


