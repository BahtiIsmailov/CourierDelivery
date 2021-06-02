package com.wb.logistics.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding3.widget.textChanges
import com.wb.logistics.R
import com.wb.logistics.databinding.AuthPhoneFragmentBinding
import com.wb.logistics.ui.splash.NavToolbarTitleListener
import com.wb.logistics.utils.LogUtils
import com.wb.logistics.views.ProgressImageButtonMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class NumberPhoneFragment : Fragment(R.layout.auth_phone_fragment) {

    private var _binding: AuthPhoneFragmentBinding? = null
    private val binding get() = _binding!!

    //private lateinit var inputMethod: InputMethodManager
    private val viewModel by viewModel<NumberPhoneViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = AuthPhoneFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListener()
        //initInputMethod()
        initStateObserve()

        (activity as NavToolbarTitleListener).hideBackButton()
    }

//    private fun initInputMethod() {
//        inputMethod =
//            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
//    }

    private fun initStateObserve() {
        viewModel.navigationEvent.observe(viewLifecycleOwner, { state ->
            binding.phoneNumber.isEnabled = true
            when (state) {
                is NumberPhoneNavAction.NavigateToInputPassword -> {
                    findNavController().navigate(
                        NumberPhoneFragmentDirections.actionNumberPhoneFragmentToInputPasswordFragment(
                            InputPasswordParameters(state.number)
                        )
                    )
                    binding.next.setState(ProgressImageButtonMode.DISABLED)
                }
                is NumberPhoneNavAction.NavigateToTemporaryPassword -> {
                    findNavController().navigate(
                        NumberPhoneFragmentDirections.actionNumberPhoneFragmentToTemporaryPasswordFragment(
                            TemporaryPasswordParameters(state.number)
                        )
                    )
                    binding.next.setState(ProgressImageButtonMode.DISABLED)
                }
                NumberPhoneNavAction.NavigateToConfig ->
                    findNavController().navigate(R.id.authConfigActivity)
            }
        })

        viewModel.stateUI.observe(viewLifecycleOwner, { state ->
            LogUtils{logDebugApp(state.toString())}
            when (state) {
                NumberPhoneUIState.PhoneCheck -> {
                    binding.phoneNumber.isEnabled = false
                    binding.numberNotFound.visibility = GONE
                    binding.next.setState(ProgressImageButtonMode.PROGRESS)
                    //inputMethod.hideSoftInputFromWindow(binding.next.windowToken, 0)
                }
                is NumberPhoneUIState.NumberFormat -> {
                    val phoneNumber = state.number
                    if (phoneNumber.length == 2) {
                        binding.phoneLayout.endIconMode = TextInputLayout.END_ICON_NONE
                    } else {
                        binding.phoneLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
                        binding.phoneLayout.setEndIconDrawable(R.drawable.ic_clear_text)
                        binding.phoneLayout.setEndIconOnClickListener {
                            binding.phoneNumber.setText(R.string.auth_number_phone_phone_default)
                        }
                    }
                    setNormalBorderInput()
                    binding.phoneNumber.isEnabled = true
                    binding.phoneNumber.setText(phoneNumber)
                    binding.phoneNumber.setSelection(phoneNumber.length)
                    binding.numberNotFound.visibility = GONE
                    binding.next.setState(ProgressImageButtonMode.DISABLED)
                }
                NumberPhoneUIState.NumberFormatComplete -> {
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                }
                is NumberPhoneUIState.NumberNotFound -> {
                    setErrorBorderInput()
                    binding.phoneNumber.isEnabled = true
                    binding.numberNotFound.text = state.message
                    binding.numberNotFound.visibility = View.VISIBLE
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                }
                is NumberPhoneUIState.Error -> {
                    showBarMessage(state.message)
                    setNormalBorderInput()
                    binding.phoneNumber.isEnabled = true
                    binding.numberNotFound.visibility = GONE
                    binding.next.setState(ProgressImageButtonMode.ENABLED)
                }
            }

        })
    }

    private fun setErrorBorderInput() {
        binding.phoneLayout.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.border_input_error)
    }

    private fun setNormalBorderInput() {
        binding.phoneLayout.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.border_input_normal)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showBarMessage(state: String) {
        Snackbar.make(binding.next, state, Snackbar.LENGTH_LONG).show()
    }

    private fun initListener() {
        binding.loginLayout.setOnLongClickListener {
            viewModel.action(NumberPhoneUIAction.LongTitle)
            true
        }


        with(binding.phoneNumber) {
//            val phone = binding.phoneNumber
//            phone.isFocusableInTouchMode = true
//            phone.requestFocus()
            viewModel.action(NumberPhoneUIAction.NumberChanges(textChanges()))
            binding.next.setOnClickListener {
                viewModel.action(NumberPhoneUIAction.CheckPhone(text.toString()))
            }
        }

    }

    private fun initViews() {
        binding.next.setState(ProgressImageButtonMode.DISABLED)
    }

}